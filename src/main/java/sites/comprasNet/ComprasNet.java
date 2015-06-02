package sites.comprasNet;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.openqa.selenium.firefox.FirefoxDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sites.Site;
import sites.SitePage;
import sites.comprasNet.AuctionPage.MonitorListener;
import auction.Auction.AuctionMode;
import config.Config;

public class ComprasNet extends Site implements MonitorListener {

	static Logger logger = LoggerFactory.getLogger(ComprasNet.class);

    SitePage currentPage = null;
    LoginPage			pageLogin			= new LoginPage();
    MainPage			pageMain			= new MainPage();
    AuctionActionsPage	pageAuctionActions	= new AuctionActionsPage();
    AuctionsListPage	pageAuctionsList	= new AuctionsListPage();
    AuctionPage			pageAuction			= new AuctionPage();

    private Thread monitorThread = null;
    private boolean keepMonitorAlive = false;
    private AuctionMode auctionMode = AuctionMode.Idle;
    
    private List<Runnable> pendingCommands = new LinkedList<>();
    private Map<String, Item> items = new HashMap<String, Item>();

	private boolean updated;
    
    private class Item {
    	private boolean winning;
    	private String id;
    	private String description;
    	private boolean opened;
    	private boolean randomFinish;
    	private long myBid;
    	private long bestBid;
    	private long limitLower = -1;
    	private long limitUpper = -1;

    	Item (boolean winning, String id, String description, boolean opened, boolean randomFinish, long myBid, long bestBid) {
    		this.winning = winning;
    		this.id = id;
    		this.description = description;
    		this.opened = opened;
    		this.randomFinish = randomFinish;
    		this.myBid = myBid;
    		this.bestBid = bestBid;

//    		logger.debug("New Item:\n" + "    WINNING:     {}\n" + "    ID:          {}\n" + "    DESCRIPTION: \"{}\"\n" +
//    				"    OPENED:      {}\n" + "    RANDOM:      {}\n" + "    MINE:        {}\n" + "    BEST:        {}",
//    				winning, id, description, opened, randomFinish, myBid, bestBid);
    	}

		public String getId() {
			return id;
		}
		
		public void setLowerLimit(long value) {
			limitLower = value;
			logger.debug("Lower limit for {} set to {}", id, limitLower);
		}

		public long getLowerLimit() {
			return limitLower;
		}
		
		public void setUpperLimit(long value) {
			limitUpper = value;
			logger.debug("Upper limit for {} set to {}", id, limitUpper);
		}

		public long getUpperLimit() {
			return limitUpper;
		}
		
		public boolean isBidValid(long value) {
			return (value >= limitLower && value <= limitUpper);			
		}

		public boolean update(boolean winning, boolean opened, boolean randomFinish, long myBid, long bestBid) {
			boolean changed = false;
			if (this.winning != winning)			{	this.winning = winning;				changed = true; }
			if (this.opened != opened)				{	this.opened = opened;				changed = true;	}
			if (this.randomFinish != randomFinish)	{	this.randomFinish = randomFinish;	changed = true;	}
			if (this.myBid != myBid)				{	this.myBid = myBid;					changed = true;	}
			if (this.bestBid != bestBid)			{	this.bestBid = bestBid;				changed = true;	}
			return changed;
		}
		
		public String toString() {
			return String.format("%-8s    %-8s    %-30s    %-8s    %-8s    %-8s    %-8s",
					id, winning, description, opened, randomFinish, myBid, bestBid);
		}
		
    }
    
	public ComprasNet(String baseURL, String projectId, Listener listener) {
		super(new FirefoxDriver(), baseURL, projectId, listener);
		pageAuction.setListener(this);
	}

	@Override
	public Logger getLogger() {
		return logger;
	}

	private synchronized void addCommand(boolean cancelAll, Runnable command) {
		if (cancelAll == true) pendingCommands.clear();
		pendingCommands.add(command);		
		logger.debug("Notifying monitor thread");
		notifyAll();
	}

	//==============================================================================================
	// Site commands
	//==============================================================================================

	@Override
	public synchronized void load() {
    	try {
	        logger.debug("Loading: {}", baseURL);
	    
	        boolean success = false;
	        for (int retry = 0; retry < 3 && success == false; ++retry) {
				logger.debug("Loading. Try: " + retry);
	        	driver.get(baseURL);
	        	success = pageLogin.isLoaded(driver);
	        }
	        
	        if (success) {
	            logger.debug("Page load successfully");
	            currentPage = pageLogin;
	        	listener.onPageLoadSuccess();
	        } else {
				logger.error("Load error: Could not load page.");
	            currentPage = null;
	        	listener.onPageLoadFail("Could not load page.");
	        }
		} catch (Exception e) {
			listener.onLoginFail("Unknown error: " + e.getMessage());
		}
	}
	
	public synchronized void login() {
		try {
			if (currentPage != pageLogin || pageLogin.isLoaded(driver) == false) {
	    		String errorMessage = "Login page is not loaded.";
	    		logger.error("Login error: {}", errorMessage);
	    		listener.onLoginFail(errorMessage);
	    		return;
			}
			
	        String mainWindowId = driver.getWindowHandle();

	        if (pageLogin.doLogin(driver, username, password) == false) {
	    		String errorMessage = pageLogin.getErrorMessage(driver);
	    		logger.error("Login error: {}", errorMessage);
	    		listener.onLoginFail(errorMessage);
	    		return;
	        }

	        currentPage = pageMain;

	        logger.debug("Login succeeded! Closing any pop-ups.");
	        Set<String> windowSet = driver.getWindowHandles();
	        for (String windowId : windowSet) {
	        	if (windowId.equals(mainWindowId) == false) {
	        		driver.switchTo().window(windowId);
	        		logger.debug("  Window ID: {}: {}: pop-up closed!", windowId, driver.getTitle());
	        		driver.close();
	        	}
	        }
	        driver.switchTo().window(mainWindowId);
	        
	        listener.onLoginSuccess();
		} catch (Exception e) {
			listener.onLoginFail("Unknown error: " + e.getMessage());
		}
	}

	@Override
	public synchronized void openProject(String projectId) {
		try {
			if (Config.TestMode) {
		        boolean success = false;

		        projectId = "362015";
		        logger.debug("Overridding Project ID to {}", projectId);

		        logger.debug("Going to FAKE Auctions List page");
        		success = false;
		        for (int retry = 0; retry < 3 && success == false; ++retry) {
					logger.debug("Loading. Try: " + retry);
		        	driver.get("file:///D:/Arquivos/Desktop/Lances/X_Proj/ComprasNet.html");
		        	success = pageAuctionsList.isLoaded(driver);
		        }
				if (success == false) {
		    		String errorMessage = "Could not navigate FAKE Auctions List page.";
		    		logger.error("Open Project error: {}", errorMessage);
		    		listener.onOpenProjectFail(errorMessage);
		    		return;
		        }

		        currentPage = pageAuctionsList;
		        
		        logger.debug("Going to Auction {} page", projectId);
		        pageAuction.setExpectedAuctionId(projectId);
				if (pageAuctionsList.gotoAuction(driver, projectId) == false) {
		    		String errorMessage = "Could not navigate do Auction \"" + projectId + "\" page.";
		    		logger.error("Open Project error: {}", errorMessage);
		    		listener.onOpenProjectFail(errorMessage);
		    		return;
				}
				
		        logger.debug("Checking new window is correct");
        		success = false;
	    		String expectedURL = "file:///D:/Arquivos/Desktop/Lances/X_Proj/ComprasNet_files/gerencia_lance.asp?prgcod=543968&numprp=362015&indSRP=N%E3o&indICMS=N%E3o&cns=true";
    	        logger.debug("Auction page opened! Switching window.");
    	        Set<String> windowSet = driver.getWindowHandles();
    	        for (String windowId : windowSet) {
            		driver.switchTo().window(windowId);
            		if (driver.getCurrentUrl().equals(expectedURL)) {
   		        		logger.debug("  Found desired BID window.");
   		        		success = true;
   		        		break;
            		}
    	        }
				if (success == false) {
		    		String errorMessage = "Could not navigate FAKE Auction page.";
		    		logger.error("Open Project error: {}", errorMessage);
		    		listener.onOpenProjectFail(errorMessage);
		    		return;
		        }
				
		        logger.debug("Going to FAKE Auction page");
        		success = false;
		        for (int retry = 0; retry < 3 && success == false; ++retry) {
					logger.debug("Loading. Try: " + retry);
//		        	driver.get("file:///D:/Arquivos/Desktop/Lances/1_Entrei/Preg%C3%A3o%20Eletr%C3%B4nico.html");
		        	driver.get("file:///D:/Arquivos/Desktop/Lances/7_OutrosAbertos/Preg%C3%A3o%20Eletr%C3%B4nico.html");
		        	success = pageAuction.isLoaded(driver);
		        }
		        if (success == false) {
		    		String errorMessage = "Could not navigate FAKE Auction page.";
		    		logger.error("Open Project error: {}", errorMessage);
		    		listener.onOpenProjectFail(errorMessage);
		    		return;
		        }

			} else {
				if (currentPage != pageMain || pageMain.isLoaded(driver) == false) {
		    		String errorMessage = "Main page is not loaded.";
		    		logger.error("Open Project error: {}", errorMessage);
		    		listener.onOpenProjectFail(errorMessage);
		    		return;
				}
				
				logger.debug("Opening project");
		
				logger.debug("Going to Auction Actions");
		        if (pageMain.gotoAuctionActions(driver) == false || pageAuctionActions.isLoaded(driver) == false) {
		    		String errorMessage = "Could not navigate do Auction Actions page.";
		    		logger.error("Open Project error: {}", errorMessage);
		    		listener.onOpenProjectFail(errorMessage);
		    		return;
		        }
		        	        
				logger.debug("Going to Auctions list");
		        currentPage = pageAuctionActions;
		        if (pageAuctionActions.gotoAuctionsList(driver) == false || pageAuctionsList.isLoaded(driver) == false) {
		    		String errorMessage = "Could not navigate do Auctions List page.";
		    		logger.error("Open Project error: {}", errorMessage);
		    		listener.onOpenProjectFail(errorMessage);
		    		return;
		        }
	
		        currentPage = pageAuctionsList;
		        
		        logger.debug("Going to Auction {} page", projectId);
		        pageAuction.setExpectedAuctionId(projectId);
				if (pageAuctionsList.gotoAuction(driver, projectId) == false || pageAuction.isLoaded(driver) == false) {
		    		String errorMessage = "Could not navigate do Auction \"" + projectId + "\" page.";
		    		logger.error("Open Project error: {}", errorMessage);
		    		listener.onOpenProjectFail(errorMessage);
		    		return;
				}
	        }
			
			currentPage = pageAuction;
			listener.onOpenProjectSuccess();
		} catch (Exception e) {
			logger.error("Error Opening Project:", e);
			listener.onOpenProjectFail("Error message: " + e.getMessage());
		}
	}

	//==============================================================================================
	// Monitor thread commands
	//==============================================================================================

	@Override
	public synchronized void startMonitor() {
		logger.debug("Start monitor requested");
		if (monitorThread != null) {
			listener.onStartMonitorFail("Already being monitored.");
			return;
		}

		logger.debug("Creating monitor thread");
		keepMonitorAlive = true;
		monitorThread = new Thread(new MonitorThread());
		monitorThread.start();
	}

	public void stopMonitor() {
		synchronized (this) {
			logger.debug("Stop monitor requested");
			if (monitorThread == null) {
				listener.onStartMonitorFail("Not being monitored.");
				return;
			}
		}

		while (true) {
			try {
				synchronized (this) {
					addCommand(true, new Runnable() { @Override public void run() { executeStopMonitor(); }});
				}
				
				logger.debug("Waiting monitor thread to finish");
				monitorThread.join();
				
				break;
			} catch (InterruptedException e) {
				logger.debug("Thread interrupted. Retrying...");
			}
		}

		synchronized (this) {
			logger.debug("Cleaning up notifying thread");
			monitorThread = null;
		}
		
		listener.onStopMonitorSuccess();
	}

	//==============================================================================================
	// Action commands
	//==============================================================================================

	@Override
	public void refresh() {
		synchronized (this) {
			logger.debug("Notifying monitor thread");
			notifyAll();
		}
	}
	
	@Override
	public synchronized void placeBid(final String id, long value) {
		if (value < 0) throw new InvalidParameterException("Value must be positive.");

		final Item item = items.get(id);
		if (item == null) {
			throw new RuntimeException("No element with such ID.");
		}
		
		if (item.isBidValid(value) == false) {
			throw new RuntimeException("Bid is not within the acceptable range: [" + item.getLowerLimit() + ", " + item.getUpperLimit());
		}
		
		addCommand(false, new Runnable() { @Override public void run() { executePlaceBid(item, value); }});
	}
	
	//==============================================================================================
	// Command executors
	//==============================================================================================
	
	private void executeStopMonitor() {
		keepMonitorAlive = false;							
	}

	private void executePlaceBid(Item item, long value) {
		if (currentPage != pageAuction) {
			listener.onPlaceBidFail(item.getId(), value, "Error message!!");
			return;
		}
		
		try {
			pageAuction.placeBid(driver, item.getId(), value);
			listener.onPlaceBidSuccess(item.getId(), value);
		} catch (Exception e) {
			listener.onPlaceBidFail(item.getId(), value, e.getMessage());
		}
	}

	//==============================================================================================
	// Monitor thread events
	//==============================================================================================

	private synchronized void onMonitorStarted() {
		logger.debug("Monitor thread has started");
		auctionMode = AuctionMode.Waiting;
		listener.onAuctionModeChanged("", auctionMode);
		listener.onStartMonitorSuccess();
	}
	
	private synchronized void onMonitorFail(String message, boolean fatal) {
		logger.error("Error monitoring:", message);
		listener.onMonitorFail(message);
		if (fatal) {
			stopMonitor();
		}
	}

	private synchronized void onMonitorStopped() {
		logger.debug("Monitor thread has stopped");
		auctionMode = AuctionMode.Idle;
		listener.onAuctionModeChanged("", auctionMode);
		listener.onStopMonitorSuccess();
	}

	private synchronized void onMonitor() {
		if (currentPage != pageAuction) {
			listener.onMonitorFail("It is not in the right page.");
		}

		pageAuction.monitor(driver);
	}

	@Override
	public synchronized void onItemInfo(boolean winning, String id, String description, boolean opened, boolean randomFinish, long myBid, long bestBid) {
		Item item = items.get(id);
		if (item == null) {
			item = new Item(winning, id, description, opened, randomFinish, myBid, bestBid);
			items.put(id, item);
			updated = true;
		} else {
			updated |= item.update(winning, opened, randomFinish, myBid, bestBid);
		}

		if (opened) {
			AuctionMode lastMode = auctionMode;
			if (randomFinish) {
				auctionMode = AuctionMode.RandomClosingTime;
			} else {
				auctionMode = AuctionMode.Normal;
			}
			if (auctionMode != lastMode) {
				listener.onAuctionModeChanged(id, auctionMode);
			}
		}
	}

	@Override
	public synchronized void onNewMessage(String sender, String message) {
//		listener.onAuctionModeChanged(mode);
		logger.debug("New message: FROM: {} - MESSAGE: {}", sender, message);
	}

	@Override
	public void onMonitorDone() {
		if (updated) {
			logger.info("Items (updated): {}", items.size());
			String header1 = String.format("    %-8s    %-8s    %-30s    %-8s    %-8s    %-8s    %-8s", "ID", "WINNING", "DESCRIPTION", "OPENED", "RANDOM", "MY BID", "BEST BID");
			String header2 = "    " + String.format("%" + (header1.length() - 4) + "s", "").replace(' ', '=');
			logger.info(header1);
			logger.info(header2);
			for(Entry<String, Item> entry : items.entrySet()) {
				logger.info("    {}", entry.getValue().toString());
				
			}
		}
		updated = false;
	}

	//==============================================================================================
	// Monitor thread
	//==============================================================================================

	private class MonitorThread implements Runnable {
		@Override
		public void run() {
			logger.debug("Monitor: Start up");
			onMonitorStarted();
			
			try {
				while (keepMonitorAlive) {
					synchronized (ComprasNet.this) {
						while (keepMonitorAlive) {
							try {
								logger.debug("Monitor: sleeping (timer)");
								if (refreshRate == -1) {
									ComprasNet.this.wait();
								} else {
									ComprasNet.this.wait(refreshRate);
								}
								logger.debug("Monitor: Woke up.");
								break;
							} catch (InterruptedException e) {
								logger.debug("Monitor Thread interrupted. Retrying...");
							}
						}

						if (keepMonitorAlive == false) break;

						if (pendingCommands.isEmpty() == false) {
							logger.debug("Executing {} pending commands...", pendingCommands.size());
							for (Runnable command : pendingCommands) {
								command.run();
								if (keepMonitorAlive == false) break;
							}
							pendingCommands.clear();
						}

						if (keepMonitorAlive == false) break;

						try {
							onMonitor();
						} catch (Exception e) {
							onMonitorFail(e.getMessage(), false);
						}
					}
				}
			} catch (Exception e) {
				onMonitorFail(e.getMessage(), true);
			}
			
			logger.debug("Monitor: Finishing up");
			onMonitorStopped();			
		}
	}

	@Override
	public synchronized void setLowerLimit(String id, long value) {
		if (value < 0) throw new InvalidParameterException("Lower limit must be positive.");

		Item item = items.get(id);
		if (item == null) {
			throw new RuntimeException("No element with such ID.");
		}

		item.setLowerLimit(value);
		this.refresh();
	}

	@Override
	public synchronized void setUpperLimit(String id, long value) {
		if (value < 0) throw new InvalidParameterException("Higher limit must be positive.");

		Item item = items.get(id);
		if (item == null) {
			throw new RuntimeException("No element with such ID.");
		}

		item.setUpperLimit(value);
		this.refresh();
	}

}

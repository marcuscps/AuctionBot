package sites;
import java.security.InvalidParameterException;

import org.slf4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.UnreachableBrowserException;

import auction.Auction;
import auction.Auction.AuctionMode;
import auction.Bid;
	
public abstract class Site {

	public abstract Logger getLogger();
	
	public interface Listener {
		void onPageLoadSuccess();
		void onPageLoadFail(String message);
		void onLoginSuccess();
		void onLoginFail(String message);
		void onOpenProjectSuccess();
		void onOpenProjectFail(String message);
		void onStartMonitorSuccess();
		void onStartMonitorFail(String message);
		void onMonitorFail(String message);
		void onStopMonitorSuccess();
		void onStopMonitorFail(String message);
		void onAuctionStarted();
		void onAuctionEnded();
		void onAuctionModeChanged(String id, AuctionMode mode);
		void onLeadingBidChanged(boolean ours, long value);
		void onPlaceBidSuccess(String id, long value);
		void onPlaceBidFail(String id, long value, String message);
	}

	public Site(WebDriver driver, String baseURL, String projectId, Listener listener) {
		this.driver = driver;
		this.baseURL = baseURL;
		this.projectId = projectId;
		this.listener = listener;
		this.mode = AuctionMode.Idle;
		this.leadingBid = null;
	}

	public String getBaseURL() {
		return baseURL;
	}

	public String getProjectId() {
		return projectId;
	}

	public Listener getListener() {
		return listener;
	}

	public Bid getLeadingBid() {
		return leadingBid;
	}

	public Auction getNewAuction() {
		return new Auction(this);
	}

	public abstract void load();

	public void close() {
    	this.stopMonitor();

    	getLogger().debug("Closing WebDriver");
    	try {
    		driver.quit();
    	} catch (UnreachableBrowserException e) {
    		getLogger().info("Looks like the browser window was closed manually.");
    	}
	}

	public void setLogin(String username, String password) {
		this.username = username;
		this.password = password;
	}
	
	public abstract void login();

	public abstract void openProject(String projectId);

	public abstract void startMonitor();

	public abstract void stopMonitor();

	public abstract void refresh();

	public void setRefreshRate(int value) {
		if (value < 0) throw new InvalidParameterException("Refresh Rate must be positive.");
		this.refreshRate = value;
	}

	public abstract void setLowerLimit(String id, long value);

	public abstract void setUpperLimit(String id, long value);

	public abstract void placeBid(String id, long value);

    protected WebDriver driver;
    
	protected String baseURL;
	protected String username;
	protected String password;
	protected String projectId;
	protected Listener listener;
	protected AuctionMode mode;
	protected Bid leadingBid;
	protected int refreshRate = 5000;
}


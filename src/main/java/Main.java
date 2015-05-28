import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import auction.Auction.AuctionMode;
import sites.Site;
import sites.Site.Listener;
import sites.comprasNet.ComprasNet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import config.Config;

class Main {
	private Site site;
	
	static Logger logger = LoggerFactory.getLogger(Main.class);
	
	public static void main(String args[]) {
		logger.info("Auction Bot started!");
		Main main = new Main();
		main.run();
	}
	
	void run() {
		Listener listener = new Listener() {
			@Override
			public void onPageLoadSuccess() {
				logger.info("Page loaded");
				site.login();
			}

			@Override
			public void onPageLoadFail(String message) {
				logger.error("Page Load error: {}", message);
			}

			@Override
			public void onLoginSuccess() {
				logger.info("Loged in");
				site.openProject("project1");
			}
			
			@Override
			public void onLoginFail(String message) {
				logger.error("Login error: {}", message);
			}
			
			@Override
			public void onOpenProjectSuccess() {
				logger.info("Project opened");
				site.startMonitor();
			}
			
			@Override
			public void onOpenProjectFail(String message) {
				logger.error("Open Project error: {}", message);
			}
			
			@Override
			public void onStartMonitorSuccess() {
				logger.info("Project being monitored from now on");
			}
			
			@Override
			public void onStartMonitorFail(String message) {
				logger.error("Start Monitor error: {}", message);
			}

			@Override
			public void onStopMonitorSuccess() {
				logger.info("Project not being monitored anymore");
			}
			
			@Override
			public void onStopMonitorFail(String message) {
				logger.error("Stop Monitor error: {}", message);
			}
		
			@Override
			public void onMonitorFail(String message) {
				logger.error("Monitor error: {}", message);
			}
			
			@Override
			public void onAuctionStarted() {
				logger.info("Auction started");
			}
			
			@Override
			public void onPlaceBidSuccess(long value) {
				logger.info("BID placed successfully: {}", value);
			}
			
			@Override
			public void onPlaceBidFail(long value, String message) {
				logger.error("BID place error: VALUE: {} - REASON: {}", value, message);
			}
			
			@Override
			public void onLeadingBidChanged(boolean ours, long value) {
				logger.info("Leading bid changed: OURS: {} - VALUE: {}", ours, value);
			}
			
			@Override
			public void onAuctionModeChanged(String id, AuctionMode mode) {
				logger.info("Auction mode changed: ID: {} - MODE: {}", id, mode);
			}
			
			@Override
			public void onAuctionEnded() {
				logger.info("Auction ended");
			}
		};
		
		site = new ComprasNet("https://www.comprasnet.gov.br/seguro/loginPortal.asp", "1", listener);

		if (Config.UseValidUserInfo) {
			site.setLogin("ALSTOMTED", "SALESFORCE2015");
		} else {
			site.setLogin("BLA", "BLE");
		}
				
		if (Config.TestMode) {
			site.openProject("FAKE");
		} else {
			site.load();
		}

		logger.info("Back to main...");
		
		String input = "";
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			System.out.println("Press enter to quit");
			input = reader.readLine();
		} catch (IOException e) {
			logger.error("Error reading from console.", e);
		}

		site.close();

		System.out.println("You entered: \"" + input + "\". Exiting...");
	}
}

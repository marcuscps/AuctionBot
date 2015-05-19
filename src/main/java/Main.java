import auction.Auction.AuctionMode;
import sites.ComprasNet;
import sites.Site;
import sites.Site.Listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
				logger.info("Loged in!");
//				site.close();
			}
			
			@Override
			public void onLoginFail(String message) {
				logger.error("Login error: {}", message);
//				site.close();
			}
			
			@Override
			public void onStopMonitorSuccess() {
			}
			
			@Override
			public void onStopMonitorFail(String message) {
			}
			
			@Override
			public void onStartMonitorSuccess() {
			}
			
			@Override
			public void onStartMonitorFail(String message) {
			}
			
			@Override
			public void onPlaceBidSuccess(long value) {
			}
			
			@Override
			public void onPlaceBidFail(long value, String message) {
			}
			
			@Override
			public void onOpenProjectSuccess() {
			}
			
			@Override
			public void onOpenProjectFail(String message) {
			}
			
			@Override
			public void onLeadingBidChanged(boolean ours, long value) {
			}
			
			@Override
			public void onAuctionStarted() {
			}
			
			@Override
			public void onAuctionModeChanged(AuctionMode mode) {
			}
			
			@Override
			public void onAuctionEnded() {
			}
		};

		
		site = new ComprasNet("https://www.comprasnet.gov.br/seguro/loginPortal.asp", "1", listener);
		site.setLogin("ALSTOMTED", "SALESFORCE2015");
//		site.setLogin("BLA", "BLE");
		site.load();
	}
}

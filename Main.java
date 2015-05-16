import auction.Auction.AuctionMode;
import sites.ComprasNet;
import sites.Site;
import sites.Site.Listener;

class Main {
	private Site site;
	
	public static void main(String args[]) {
		System.out.println("Started!");
		Main main = new Main();
		main.run();
	}
	
	void run() {
		Listener listener = new Listener() {
			@Override
			public void onPageLoadSuccess() {
				System.out.println("Page loaded!");
				site.login();
			}

			@Override
			public void onPageLoadFail(String message) {
				System.out.println("Error: " + message);
			}

			@Override
			public void onLoginSuccess() {
				System.out.println("Loged in!");
				site.close();
			}
			
			@Override
			public void onLoginFail(String message) {
				System.out.println("Error: " + message);
				site.close();
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
		site.load();
	}
}

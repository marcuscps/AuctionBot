class Auction {

	public class Listener {
		void onLoginSuccess();
		void onLoginFail(String message);
		void onOpenProjectSuccess();
		void onOpenProjectFail(String message);
		void onStartMonitorSuccess();
		void onStartMonitorFail(String message);
		void onStopMonitorSuccess();
		void onStopMonitorFail(String message);
		void onAuctionStarted();
		void onAuctionEnded();
		void onAuctionModeChanged(AuctionMode mode);
		void onLeadingBidChanged(boolean ours, long value);
		void onPlaceBidSuccess(long value);
		void onPlaceBidFail(long value, String message);
	}

	public Auction(Site site) {
		site = site;
	}

	public Listener getListener() {
		return listener;
	}

	public Bid getLeadingBid() {
		return site.getLeadingBid();
	}

	public void startUp() {
	}

	private site;
	private normalBehavior;
	private randomClosingTimeBehavior;
}

package auction;
import sites.Site;

public class Auction {

	public class Listener {
		void onLoginSuccess() {}
		void onLoginFail(String message) {}
		void onOpenProjectSuccess() {}
		void onOpenProjectFail(String message) {}
		void onStartMonitorSuccess() {}
		void onStartMonitorFail(String message) {}
		void onStopMonitorSuccess() {}
		void onStopMonitorFail(String message) {}
		void onAuctionStarted() {}
		void onAuctionEnded() {}
		void onAuctionModeChanged(AuctionMode mode) {}
		void onLeadingBidChanged(boolean ours, long value) {}
		void onPlaceBidSuccess(long value) {}
		void onPlaceBidFail(long value, String message) {}
	}

	public enum AuctionMode {
		Idle,
		Normal,
		RandomClosingTime,
		Closed
	};

	public Auction(Site site) {
		this.site = site;
	}

	public Listener getListener() {
		return listener;
	}

	public Bid getLeadingBid() {
		return site.getLeadingBid();
	}

	public void startUp() {
	}

	private Site site;
	private Listener listener;
	//private normalBehavior;
	//private randomClosingTimeBehavior;
}

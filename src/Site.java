class Site {

	public enum AuctionMode {
		Idle,
		Normal,
		RandomClosingTime,
		Closed
	};

	public interface Listener {
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

	public Site(String baseURL, String projectId, Listener listener) {
		baseURL = baseURL;
		projectId = projectId;
		listener = listener;
		mode = AuctionMode.Idle;
		leadingBid = null;
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
		return new Auction();
	}

	public void login() {
		// TODO
		listener.onLoginSuccess();
		listener.onLoginFail("Error message!!");
	}

	public void openProject() {
		// TODO
		listener.onOpenProjectSuccess();
		listener.onOpenProjectFail("Error message!!");
		listener.onAuctionModeChanged(mode);
	}

	public void startMonitor() {
		// TODO
		listener.onStartMonitorSuccess();
		listener.onStartMonitorFail("Error message!!");
		mode = AuctionMode.Normal;
		listener.onAuctionStarted();
		listener.onAuctionModeChanged(mode);
		mode = AuctionMode.RandomClosingTime;
		listener.onAuctionModeChanged(mode);
		listener.onLeadingBidChanged(true, 100);
		listener.onLeadingBidChanged(false, 100);
		mode = AuctionMode.Closed;
		listener.onAuctionModeChanged(mode);
		listener.onAuctionEnded();
	}

	public void stopMonitor() {
		// TODO
		listener.onStopMonitorSuccess();
		listener.onStopMonitorFail("Error message!!");
	}

	public void placeBid(long value) {
		// TODO
		listener.onPlaceBidSuccess(value);
		listener.onPlaceBidFail(value, "Error message!!");
	}

	private String baseURL;
	private String projectId;
	private Listener listener;
	private AuctionMode mode;
	private Bid leadingBid;
}
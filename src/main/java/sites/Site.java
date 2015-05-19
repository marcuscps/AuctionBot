package sites;
import org.openqa.selenium.WebDriver;

import auction.Auction;
import auction.Auction.AuctionMode;
import auction.Bid;

public abstract class Site {

	public interface Listener {
		void onPageLoadSuccess();
		void onPageLoadFail(String message);
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

	public abstract void close();

	public void setLogin(String username, String password) {
		this.username = username;
		this.password = password;
	}
	
	public abstract void login();

	public abstract void openProject();

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

    WebDriver driver;
    
	protected String baseURL;
	protected String username;
	protected String password;
	protected String projectId;
	protected Listener listener;
	protected AuctionMode mode;
	protected Bid leadingBid;
}

package auction;

public class Bid {

	public Bid(boolean ours, long value) {
		this.ours = ours;
		this.value = value;
	}

	public boolean isOurs() {
		return ours;
	}

	public long getValue() {
		return value;
	}

	private boolean ours;
	private long value;
}

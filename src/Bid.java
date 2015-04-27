public class Bid {

	public Bid(boolean ours, long value) {
		ours = ours;
		value = value;
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

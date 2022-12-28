package bitmap_implementations;

public abstract class Bitmap {
	
	int bitPerEntry;
	Bitmap(int bits) {
		bitPerEntry = bits;
	}
	
	public abstract long size();
	public abstract void set(long bit_index, boolean value);
	public abstract void setFromTo(long from, long to, long value);
	public abstract boolean get(long bit_index);
	public abstract long getFromTo(long from, long to);
	
	
	public void print_important_bits() {	
		for (long i = 0; i < size(); i++) {
			long remainder = i % bitPerEntry;
			if (remainder == 0) {
				System.out.print(" ");
			}
			if (remainder == 0 || remainder == 1 || remainder == 2) {
				System.out.print(get(i) ? "1" : "0");
			}
		}
		System.out.println();
	}
	
	

	
	public static boolean get_fingerprint_bit(long index, long fingerprint) {
		long mask = 1 << index;
		long and = fingerprint & mask;
		return and != 0;
	}
	
	public static long set_fingerprint_bit(long index, long fingerprint, boolean val) {
		if (val) {
			fingerprint |= 1 << index;   
		}
		else {
			fingerprint &= ~(1 << index);   
		}
		return fingerprint;
	}
	
	
	public void print() {	
		for (long i = 0; i < size(); i++) {
			System.out.print(get(i) ? "1" : "0");
		}
		System.out.println();
	}
}

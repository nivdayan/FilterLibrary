package bitmap_implementations;

import java.util.BitSet;

public abstract class Bitmap {
	
	int bitPerEntry;
	Bitmap(int bits) {
		bitPerEntry = bits;
	}
	
	public abstract int size();
	public abstract void set(int bit_index, boolean value);
	public abstract void setFromTo(int from, int to, long value);
	public abstract boolean get(int bit_index);
	public abstract long getFromTo(int from, int to);
	
	
	public void print_important_bits() {	
		for (int i = 0; i < size(); i++) {
			int remainder = i % bitPerEntry;
			if (remainder == 0) {
				System.out.print(" ");
			}
			if (remainder == 0 || remainder == 1 || remainder == 2) {
				System.out.print(get(i) ? "1" : "0");
			}
		}
		System.out.println();
	}
	
	

	
	public static boolean get_fingerprint_bit(int index, long fingerprint) {
		long mask = 1 << index;
		long and = fingerprint & mask;
		return and != 0;
	}
	
	public static long set_fingerprint_bit(int index, long fingerprint, boolean val) {
		if (val) {
			fingerprint |= 1 << index;   
		}
		else {
			fingerprint &= ~(1 << index);   
		}
		return fingerprint;
	}
	
	
	public void print() {	
		for (int i = 0; i < size(); i++) {
			System.out.print(get(i) ? "1" : "0");
		}
		System.out.println();
	}
}

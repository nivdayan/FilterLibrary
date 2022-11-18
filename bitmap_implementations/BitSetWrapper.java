package bitmap_implementations;

import java.util.BitSet;

import filters.QuotientFilter;

public class BitSetWrapper extends Bitmap {

	public BitSet bs;
	
	public BitSetWrapper(int bits_per_entry, int num_entries) {
		super(bits_per_entry);
		bs = new BitSet(bits_per_entry * num_entries);
	}
	
	@Override
	public int size() {
		return bs.size();
	}

	@Override
	public void set(int bit_index, boolean value) {
		bs.set(bit_index, value);
	}

	@Override
	public void setFromTo(int from, int to, long value) {
		for (int i = from, j = 0; i < to; i++, j++) {						
			bs.set(i, get_fingerprint_bit(j, value));
		}		
	}

	@Override
	public boolean get(int bit_index) {
		return bs.get(bit_index);
	}

	@Override
	public long getFromTo(int from, int to) {
		long val = 0;
		for (int i = from, j = 0; i < to; i++, j++) {
			val = set_fingerprint_bit(j, val, bs.get(i));
		}
		return val;
	}

}

package bitmap_implementations;

import java.util.BitSet;

public class QuickBitVectorWrapper extends Bitmap {

	long[] bs;
	
	
	public QuickBitVectorWrapper(int bits_per_entry, int num_entries) {
		super(bits_per_entry);
		bs = QuickBitVector.makeBitVector(num_entries, bits_per_entry);
	}

	@Override
	public long size() {
		return bs.length * Long.BYTES * 8;
	}

	@Override
	public void set(long bit_index, boolean value) {
		if (value) {
			QuickBitVector.set(bs, bit_index);
		}
		else {
			QuickBitVector.clear(bs, bit_index);
		}
	}

	@Override
	public void setFromTo(long from, long to, long value) {
		QuickBitVector.putLongFromTo(bs, value, from, to - 1);
	}

	@Override
	public boolean get(long bit_index) {
		return QuickBitVector.get(bs, bit_index);
	}

	@Override
	public long getFromTo(long from, long to) {
		return QuickBitVector.getLongFromTo(bs, from, to - 1);
	}
	

}

package filters;

import bitmap_implementations.Bitmap;
import bitmap_implementations.QuickBitVectorWrapper;

public class BloomFilter extends Filter {

	Bitmap filter;
	long num_bits; 
	long max_num_entries;
	long current_num_entries;
	long bits_per_entry;
	int num_hash_functions;

	public BloomFilter(int new_num_entries, int new_bits_per_entry) {
		max_num_entries = new_num_entries;
		filter = new QuickBitVectorWrapper(new_bits_per_entry,  (int)max_num_entries);
		num_bits = new_bits_per_entry * max_num_entries;
		bits_per_entry = new_bits_per_entry;
		num_hash_functions = (int) Math.round( bits_per_entry * Math.log(2) );
		ht = HashType.xxh;		
		current_num_entries = 0;
	}
	
	@Override
	boolean rejuvenate(long key) {
		return false;
	}

	@Override
	void expand() {
	}

	@Override
	protected boolean _delete(long large_hash) {
		return false;
	}
	
	long get_target_bit(long large_hash, int hash_num) {
		long this_hash = HashFunctions.xxhash(large_hash, hash_num);
		return Math.abs(this_hash % num_bits);
	}
	
	long get_target_bit2(long large_hash, int hash_num) {
		long this_hash = HashFunctions.xxhash(large_hash, hash_num);
		long mask = num_bits - 1;
		long target_bit = this_hash & mask;
		return target_bit;
	}
	
	@Override
	protected boolean _insert(long large_hash, boolean insert_only_if_no_match) {
		
		//long target_bit = Math.abs(large_hash % num_bits);
		
		long mask = num_bits - 1;
		long target_bit = large_hash & mask;
		
		filter.set(target_bit, true);
		
		for (int i = 1; i < num_hash_functions; i++) {
			target_bit = get_target_bit2(large_hash, i);
			//System.out.println(target_bit);
			filter.set(target_bit, true);
		}
		current_num_entries++;
		return true;
	}

	/*static long reduce(int x, int N) {
		long res = ((long) x * (long) N) >> 32;
		return res;
	}*/
	
	@Override
	protected boolean _search(long large_hash) {
		
		//long target_bit1 = reduce((int)large_hash, (int)num_bits);
		//System.out.println(target_bit1);
		
		//long power = (long)(Math.log(num_bits) / Math.log(2));

		/*QuotientFilter.print_long_in_binary(large_hash, 64);
		System.out.println();
		QuotientFilter.print_long_in_binary(num_bits, 64);
		System.out.println();		
		QuotientFilter.print_long_in_binary(mask, 64);
		System.out.println();
		//QuotientFilter.print_long_in_binary(power, 64);
		//System.out.println();
		QuotientFilter.print_long_in_binary(slot, 64);
		System.out.println();*/
		
		//large_hash = large_hash | 1;
		/*System.out.println();
		QuotientFilter.print_long_in_binary(-2, 64);
		QuotientFilter.print_long_in_binary(2, 64);
		QuotientFilter.print_long_in_binary(large_hash, 64);*/
		//large_hash = Math.abs(large_hash);
		//System.out.println();
		//QuotientFilter.print_long_in_binary(large_hash, 64);
		long mask = num_bits - 1;
		long target_bit = large_hash & mask;
		//QuotientFilter.print_long_in_binary(target_bit, 64);
		
		long target_bit2 = Math.abs(large_hash % num_bits);
		//QuotientFilter.print_long_in_binary(target_bit2, 64);
		
		if (! filter.get(target_bit) ) {
			return false;
		}
		
		for (int i = 1; i < num_hash_functions; i++) {
			target_bit = get_target_bit2(large_hash, i);
			if (! filter.get(target_bit) ) {
				return false;
			}
		}
		return true;
	}

	@Override
	public long get_num_entries(boolean include_all_internal_filters) {
		return current_num_entries;
	}
	
	public double get_utilization() {
		return current_num_entries / max_num_entries;
	}
	
	public double measure_num_bits_per_entry() {
		return (max_num_entries * bits_per_entry) / (double)current_num_entries;
	}
}

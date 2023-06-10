package filters;

import java.util.ArrayList;

import filters.FingerprintGrowthStrategy.FalsePositiveRateExpansion;

/*
 * The following example assumes we begin with an InfiniFilter with 2^3, or 8 cells and 4 bits per fingerprint
 * The example assumes decreasing the FPR polynomially, or in other words
 * the fingerprint size for new entries is increasing at a rate of  2(log2(X)), where X is the number of expansions
 * that has taken place. 
 * This example shows us how to adjust the capacity of the secondary InfiniFilter in response, 
 * and how many bits / entry to assign its fingerprints 
 * This is based on the intuition that it takes longer for fingerprints   
 * 
 * expansions	size	bits / entry	Sec size 	sec bits /entry
 * 0			3		4			
 * 1			4		6			
 * 2			5		7			
 * 3			6		8			
 * 4			7		8				3			4
 * 5			8		9			
 * 6			9		9			
 * 7			10		10				4			6
 * 8			11		10			
 * 9			12		10				5			7
 * 10			13		10			
 * 11			14		11				6			8
 * 12			15		11				7			8
 * 13			16		11			
 * 14			17		11				8			9
 * 15			18		12				9			9
 * 16			19		12			
 * 17			20		12				10			10
 * 18			21		12				11			10
 * 19			22		12				12			10
 * 20			23		12				13			10
 * 21			24		12			
 * 22			25		13				14			11
*/

public class ChainedInfiniFilter extends BasicInfiniFilter {

	ArrayList<BasicInfiniFilter> chain;
	BasicInfiniFilter secondary_IF = null;
	//int count_until_replacing_former = 0;
	//int count_until_expanding_former = 0;
	//int former_phase = 0;
	
	public ChainedInfiniFilter(int power_of_two, int bits_per_entry) {
		super(power_of_two, bits_per_entry);
		chain = new ArrayList<BasicInfiniFilter>();
		//num_expansions_left = Integer.MAX_VALUE;
	}
	
	boolean is_full() {
		return false;
	}
	
	long slot_mask = 0;
	long fingerprint_mask = 0;
	long unary_mask = 0;
	
	void prep_masks() {
		if (secondary_IF == null) {
			return;
		}

		prep_masks(power_of_two_size, secondary_IF.power_of_two_size, secondary_IF.fingerprintLength);

	}
	
	void prep_masks(long active_IF_power_of_two, long secondary_IF_power_of_two, long secondary_FP_length) {

		long _slot_mask = (1L << secondary_IF_power_of_two) - 1L;

		long actual_FP_length = active_IF_power_of_two - secondary_IF_power_of_two;
		long FP_mask_num_bits = Math.min(secondary_FP_length - 1, actual_FP_length);
		
		long _fingerprint_mask = (1L << FP_mask_num_bits ) - 1L;
		
		long num_padding_bits =  secondary_FP_length - FP_mask_num_bits;
		long _unary_mask = 0;
		long unary_mask1 = 0;

		if (num_padding_bits > 0) {
			unary_mask1 = (1L << num_padding_bits - 1) - 1L;
			_unary_mask = unary_mask1 << (actual_FP_length + 1);			
		}

		/*QuotientFilter.print_long_in_binary(_slot_mask, 32);
		QuotientFilter.print_long_in_binary(_fingerprint_mask, 32);
		QuotientFilter.print_long_in_binary(unary_mask1, 32);
		QuotientFilter.print_long_in_binary(_unary_mask, 32);*/
		
		
		unary_mask = _unary_mask;
		slot_mask = _slot_mask;
		fingerprint_mask = _fingerprint_mask;
		
		//System.out.println();
	}
	
	void handle_empty_fingerprint(long bucket_index, QuotientFilter current) {
		long bucket1 = bucket_index;
		long fingerprint = bucket_index >> secondary_IF.power_of_two_size;
		long slot = bucket1 & slot_mask;
		
		// In case the fingerprint is too long, we must chop it. This is here just for safety though, 
		// as the slot width of the secondary IF should generally be large enough
		long adjusted_fingerprint = fingerprint & fingerprint_mask; 
		
		// In case the fingerprint is too short, we must add unary padding
		adjusted_fingerprint = adjusted_fingerprint | unary_mask;
		
		//unary_mask = unary_mask <<
		//if (bucket_index == 16269) {
			
			/*print_long_in_binary( bucket1, power_of_two_size + 1);
			print_long_in_binary( slot_mask, 32);
			print_long_in_binary( slot, secondary_IF.power_of_two_size + 2);
			print_long_in_binary( fingerprint_mask, 32);
			print_long_in_binary( unary_mask, 32);
			print_long_in_binary( fingerprint, 32);
			print_long_in_binary( adjusted_fingerprint, 32);
			System.out.println();*/
		
		num_existing_entries--;
		//secondary_IF.num_existing_entries++;
		secondary_IF.insert(adjusted_fingerprint, slot, false);
		
	}
	
	// The hash function is being computed here for each filter 
	// However, it's not such an expensive function, so it's probably not a performance issue. 
	public boolean search(long input) {
		
		if (super.search(input)) {
			return true;
		}
		
		if (secondary_IF != null && secondary_IF.search(input)) {
			return true;
		}
		
		for (QuotientFilter qf : chain) {
			if (qf.search(input)) {
				return true;
			}
		}
		return false;
	}
	
	void create_secondary(int power, int FP_size) {
		power = Math.max(power, 3);
		secondary_IF = new BasicInfiniFilter(power, FP_size + 3);
		secondary_IF.hash_type = this.hash_type;
		secondary_IF.fprStyle = fprStyle;
		secondary_IF.original_fingerprint_size = original_fingerprint_size;
	}
	
	boolean expand() {	
		//print_filter_summary();
		// creating secondary IF for the first time 
		
		if (secondary_IF == null && num_void_entries > 0) { // first time we create a former filter
			int power = (int) Math.ceil( Math.log(num_void_entries) / Math.log(2) );
			int FP_size = power_of_two_size - power + 1; 
			create_secondary(power, FP_size);
		}
		// the secondary infinifilter is full, so we add it to the chain
		else if (secondary_IF != null && secondary_IF.num_void_entries > 0) { // our former filter is full 
			chain.add(secondary_IF);
			int orig_FP = secondary_IF.fingerprintLength;
			secondary_IF = new BasicInfiniFilter(secondary_IF.power_of_two_size + 1, secondary_IF.fingerprintLength + 3);
			secondary_IF.hash_type = this.hash_type;
			secondary_IF.original_fingerprint_size = orig_FP;
			secondary_IF.fprStyle = fprStyle;
		}
		// we expand the secondary infinifilter
		else if (secondary_IF != null) {  // standard procedure
			expand_secondary_IF();
		}
		prep_masks();
		super.expand();
		//System.out.println(num_expansions + "\t" + num_distinct_void_entries + "\t" + fingerprintLength + "\t" + num_existing_entries);
		return true;
	}
	

	
	void expand_secondary_IF() {
		int num_entries = secondary_IF.num_existing_entries + num_void_entries;
		long logical_slots = secondary_IF.get_logical_num_slots();
		double secondary_fullness = num_entries / (double)logical_slots;
		// sometimes we may also want to widen the fingerprint bits, not just expand when we reach capacity
		// need to consider this 
		do  {
			secondary_IF.num_expansions++;
			secondary_IF.expand();
			logical_slots = secondary_IF.get_logical_num_slots();
			secondary_fullness = num_entries / (double)logical_slots;
		} while (secondary_fullness > expansion_threshold / 2.0);
	}
	
	// TODO if we rejuvenate a void entry, we should subtract from num_void_entries 
	// as if this count reaches zero, we can have shorter chains
	public boolean rejuvenate(long key) {
		boolean success = super.rejuvenate(key);
		if (success) {
			return true;
		}
		if (secondary_IF == null) {
			System.out.println("Warning: it seems the key to be rejuvenrated does not exist. We must only ever call rejuvenrate on keys that exist.");
			return false;
		}
		success = secondary_IF.delete(key);
		if (success) {
			success = insert(key, false);
			if (!success) {
				System.out.println("failed at rejuvenation");
				System.exit(1);
			}
			return true;
		}
		for (int i = chain.size() - 1; i >= 0; i--) {						
			success = chain.get(i).delete(key);
			if (success) {
				success = insert(key, false);
				if (!success) {
					System.out.println("failed at rejuvenation");
					System.exit(1);
				}
				return true;
			}
		}
		return false;
	}
	
	
	public boolean delete(long input) {
		long large_hash = get_hash(input);
		long slot_index = get_slot_index(large_hash);
		long fp_long = gen_fingerprint(large_hash);
		//System.out.println("deleting  " + input + "\t b " + slot_index + " \t" + get_fingerprint_str(fp_long, fingerprintLength));
		boolean success = delete(fp_long, slot_index);
		if (success) {
			num_existing_entries--;
			return true;
		}
		
		slot_index = secondary_IF.get_slot_index(large_hash);
		fp_long = secondary_IF.gen_fingerprint(large_hash);
		success = secondary_IF.delete(fp_long, slot_index);
		if (success) {
			num_existing_entries--;
			return true;
		}
		
		for (int i = chain.size() - 1; i >= 0; i--) {			
			slot_index = chain.get(i).get_slot_index(large_hash);
			fp_long = chain.get(i).gen_fingerprint(large_hash);
			success = chain.get(i).delete(fp_long, slot_index);
			if (success) {
				return true;
			}
		}
		
		return success; 
	}
	
	public double measure_num_bits_per_entry() {
		ArrayList<QuotientFilter> filters = new ArrayList<QuotientFilter>(chain);
		if (secondary_IF != null) {
			filters.add(secondary_IF);
		}
		return measure_num_bits_per_entry(this, filters);
	}
	
	public void print_filter_summary() {
		System.out.println("----------------------------------------------------");
		super.print_filter_summary();
		System.out.println();
		if (secondary_IF != null) {
			secondary_IF.print_filter_summary();
		}
		System.out.println();
		for (BasicInfiniFilter f : chain) {
			f.print_filter_summary();
			System.out.println();
		}
	}
	
	public void pretty_print() {	
		System.out.println();
		System.out.println("Active IF");
		System.out.print(get_pretty_str(true));
		System.out.println();
		if (secondary_IF != null) {
			System.out.println("Secondary IF");
			System.out.print(secondary_IF.get_pretty_str(true));
			System.out.println();
		}
		for (int i = 0; i < chain.size(); i++) {
			System.out.println("Chain #" + i);
			System.out.print(chain.get(i).get_pretty_str(true));
			System.out.println();	
		}
		
	}
	
	public long get_num_entries(boolean include_all_internal_filters) {
		long num_entries = super.get_num_entries(false);
		if (!include_all_internal_filters) {
			return num_entries;
		}
		for (QuotientFilter q : chain) {
			num_entries += q.get_num_entries(false);
		}
		if (secondary_IF != null) {
			long former_num_entries = secondary_IF.get_num_entries(false);
			num_entries += former_num_entries;
		}
		return num_entries; 
	}
	
	
}


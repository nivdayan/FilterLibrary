package filters;

import java.util.ArrayList;

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

public class ChainedInfiniFilter extends InfiniFilter {

	ArrayList<InfiniFilter> older_filters;
	InfiniFilter former = null;
	int count_until_replacing_former = 0;
	int count_until_expanding_former = 0;
	int former_phase = 0;
	
	ChainedInfiniFilter(int power_of_two, int bits_per_entry) {
		super(power_of_two, bits_per_entry);
		older_filters = new ArrayList<InfiniFilter>();
	}
	
	void handle_empty_fingerprint(int bucket_index, QuotientFilter current) {
		int bucket1 = bucket_index;
		int fingerprint = bucket_index >> former.power_of_two_size;
		int slot_mask = (1 << former.power_of_two_size) - 1;
		int slot = bucket1 & slot_mask;
		
		//System.out.println("migrating");
		
		/*print_int_in_binary( bucket1, power_of_two_size + 1);
		print_int_in_binary( slot, former.power_of_two_size);
		print_int_in_binary( fingerprint, former.fingerprintLength);
		System.out.println();*/
		
		//System.out.println("moving void entry from bucket " + bucket_index + " to bucket " + slot + " with fingerprint " + fingerprint + " in secondary IF");
		//print_int_in_binary( fingerprint, former.fingerprintLength);
		
		
		num_existing_entries--;
		former.num_existing_entries++;
		former.insert(fingerprint, slot, false);
		
	}
	
	// The hash function is being computed here for each filter 
	// However, it's not such an expensive function, so it's probably not a performance issue. 
	public boolean search(int input) {
		if (super.search(input)) {
			return true;
		}
		if (former != null && former.search(input)) {
			return true;
		}
		
		for (QuotientFilter qf : older_filters) {
			if (qf.search(input)) {
				return true;
			}
		}
		return false;
	}
	
	void expand() {
		
		count_until_expanding_former--; 
		
		//System.out.println("starting expansion " + num_expansions);
		if (num_expansions == original_fingerprint_size ) { // first time we create a former filter
			former = new InfiniFilter(power_of_two_size - original_fingerprint_size + 1, original_fingerprint_size + 3);
			former.fprStyle = fprStyle;
			count_until_replacing_former = original_fingerprint_size;
			
			int prev_FP_size = FingerprintGrowthStrategy.get_new_fingerprint_size(original_fingerprint_size, 0, fprStyle);
			int new_FP_size = FingerprintGrowthStrategy.get_new_fingerprint_size(original_fingerprint_size, 1, fprStyle);
			int FP_diff = new_FP_size - prev_FP_size;
			count_until_expanding_former = FP_diff + 1;
			former_phase = 1;
			//System.out.println(former.power_of_two_size +  " " + former.fingerprintLength);
			//former.pretty_print();
		}
		else if (former != null && count_until_replacing_former == 0 && count_until_expanding_former == 0) { // our former filter is full 
			//former.pretty_print();
			older_filters.add(former);
			
			int new_FP_size = FingerprintGrowthStrategy.get_new_fingerprint_size(original_fingerprint_size, former_phase, fprStyle);
			int prev_FP_size = FingerprintGrowthStrategy.get_new_fingerprint_size(original_fingerprint_size, former_phase + 1, fprStyle);
			former_phase++;
			int FP_diff = prev_FP_size - new_FP_size;
			count_until_expanding_former = FP_diff + 1;
			
			former = new InfiniFilter(former.power_of_two_size + 1, new_FP_size + 3);
			former.original_fingerprint_size = original_fingerprint_size;
			former.fprStyle = fprStyle;
			count_until_replacing_former = former.fingerprintLength; 
			//System.out.println(former.power_of_two_size +  " " + former.fingerprintLength);
			
		}
		else if (former != null && count_until_expanding_former == 0) {  // standard procedure
			/*pretty_print();
			former.pretty_print();
			
			print_filter_summary();
			former.print_filter_summary();*/
			former.num_expansions = former_phase;
			former.expand();
			
			count_until_replacing_former--;
			
			int new_FP_size = FingerprintGrowthStrategy.get_new_fingerprint_size(original_fingerprint_size, former_phase, fprStyle);
			int prev_FP_size = FingerprintGrowthStrategy.get_new_fingerprint_size(original_fingerprint_size, former_phase + 1, fprStyle);
			former_phase++;
			int FP_diff = prev_FP_size - new_FP_size;
			count_until_expanding_former = FP_diff + 1;
			//System.out.println(former.power_of_two_size +  " " + former.fingerprintLength);
		}
		super.expand();
		//System.out.println("finished expanding ------------");
	}
	
	boolean rejuvenate(int key) {
		boolean success = super.rejuvenate(key);
		if (success) {
			return true;
		}
		success = former.delete(key);
		if (success) {
			success = insert(key, false);
			if (!success) {
				System.out.println("failed at rejuvenation");
				System.exit(1);
			}
			return true;
		}
		for (int i = older_filters.size() - 1; i >= 0; i--) {						
			success = older_filters.get(i).delete(key);
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
	
	
	public boolean delete(int input) {
		int large_hash = HashFunctions.normal_hash(input);
		int slot_index = get_slot_index(large_hash);
		long fp_long = gen_fingerprint(large_hash);
		//System.out.println("deleting  " + input + "\t b " + slot_index + " \t" + get_fingerprint_str(fp_long, fingerprintLength));
		boolean success = delete(fp_long, slot_index);
		if (success) {
			num_existing_entries--;
			return true;
		}
		
		slot_index = former.get_slot_index(large_hash);
		fp_long = former.gen_fingerprint(large_hash);
		success = former.delete(fp_long, slot_index);
		if (success) {
			num_existing_entries--;
			return true;
		}
		
		for (int i = older_filters.size() - 1; i >= 0; i--) {			
			slot_index = older_filters.get(i).get_slot_index(large_hash);
			fp_long = older_filters.get(i).gen_fingerprint(large_hash);
			success = older_filters.get(i).delete(fp_long, slot_index);
			if (success) {
				return true;
			}
		}
		
		return success; 

	}
	
	public double measure_num_bits_per_entry() {
		return measure_num_bits_per_entry(this, new ArrayList<QuotientFilter>(older_filters));
	}
	
	public void print_filter_summary() {	
		super.print_filter_summary();
		System.out.println();
		former.print_filter_summary();
		System.out.println();
		for (InfiniFilter f : older_filters) {
			f.print_filter_summary();
			System.out.println();
		}
	}

}


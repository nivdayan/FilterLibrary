package testing_project;

import java.util.ArrayList;

public class ChainedInfiniFilter extends InfiniFilter {

	ArrayList<InfiniFilter> older_filters;
	InfiniFilter former = null;
	int num_expansions = 0;
	int expansions_of_former = 0;
	
	ChainedInfiniFilter(int power_of_two, int bits_per_entry) {
		super(power_of_two, bits_per_entry);
		older_filters = new ArrayList<InfiniFilter>();
	}
	
	void handle_empty_fingerprint(int bucket_index, QuotientFilter current) {
		int bucket1 = bucket_index;
		int fingerprint = bucket_index >> former.power_of_two_size;
		int slot_mask = (1 << former.power_of_two_size) - 1;
		int slot = bucket1 & slot_mask;
		
		/*print_int_in_binary( bucket1, power_of_two_size + 1);
		print_int_in_binary( slot, former.power_of_two_size);
		print_int_in_binary( fingerprint, former.fingerprintLength);
		System.out.println();*/
		
		num_existing_entries--;
		former.num_existing_entries++;
		former.insert(fingerprint, slot, false);
	}
	
	// The hash function is being computed here for each filter 
	// However, it's not such an expensive function, so it's probably not a performance issue. 
	boolean search(int input) {
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
		if (num_expansions == fingerprintLength - 1) { // first time we create a former filter
			former = new InfiniFilter(power_of_two_size - fingerprintLength + 1, bitPerEntry);
		}
		else if (former != null && expansions_of_former == former.fingerprintLength) { // our former filter is full 
			older_filters.add(former);
			former = new InfiniFilter(power_of_two_size - fingerprintLength + 1, bitPerEntry);
			expansions_of_former = 0; 
		}
		else if (former != null) {  // standard procedure
			former.expand();
			expansions_of_former++;
		}
		super.expand();
		//System.out.println("finished expanding ------------");
		num_expansions++;	
	}
	
	double measure_num_bits_per_entry() {
		return measure_num_bits_per_entry(this, new ArrayList<QuotientFilter>(older_filters));
	}

}


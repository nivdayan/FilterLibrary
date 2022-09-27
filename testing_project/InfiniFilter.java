
package testing_project;

public class InfiniFilter extends QuotientFilter {

	final long empty_fingerprint;

	InfiniFilter(int power_of_two, int bits_per_entry) {
		super(power_of_two, bits_per_entry);
		max_entries_before_expansion = (int)(Math.pow(2, power_of_two_size) * expansion_threshold);
		empty_fingerprint = (1 << bits_per_entry - 3) - 2 ;
	}
	
	protected boolean compare(int index, long fingerprint) {
		int generation = parse_unary(index);
		int first_fp_bit = index * bitPerEntry + 3;
		int last_fp_bit = index * bitPerEntry + 3 + fingerprintLength - (generation + 1);
		int actual_fp_length = last_fp_bit - first_fp_bit;
		long existing_fingerprint = filter.getFromTo(first_fp_bit, last_fp_bit);
		long mask = (1 << actual_fp_length) - 1;
		long adjusted_saught_fp = fingerprint & mask;
		return existing_fingerprint == adjusted_saught_fp;
	}
	
	/*public String get_pretty_str(int index) {
		StringBuffer sbr = new StringBuffer();
		
		int age = parse_unary_old(index);
		
		for (int i = index * bitPerEntry; i < (index + 1) * bitPerEntry; i++) {
			int remainder = i % bitPerEntry;

			if (remainder == 3) {
				sbr.append(" ");
			}
			sbr.append(filter.get(i) ? "1" : "0");
			int n = bitPerEntry - i + index * bitPerEntry - 2;
			if (n == age) {
				sbr.append(" ");
			}
		}
		sbr.append("\n");
		return sbr.toString();
	}*/
	
	
	// this is the older emthod for parsing the unary code using a loop
	/*int parse_unary_old(int slot_index) {
		int starting_bit = bitPerEntry * (slot_index + 1) - 1;
		int ending_bit = bitPerEntry * slot_index;		
		int counter = 0;
		for (int i = starting_bit; i >= ending_bit; i--) {
			if (!filter.get(i)) {
				break;
			}
			counter++;
			
		}
		return counter;
	}*/

	
	// this is the newer version of parsing the unary encoding. 
	// it is done using just binary operations and no loop. 
	// however, this optimization didn't yield much performance benefit 
	int parse_unary(int slot_index) {
		int f = (int)get_fingerprint(slot_index);
		//.out.println();
		//System.out.println(get_pretty_str(slot_index));
		//print_int_in_binary(f, 32);
		int inverted_fp = ~f;
		//print_int_in_binary(inverted_fp, 32);
		int mask = (1 << fingerprintLength) - 1;
		//print_int_in_binary(mask, 32);
		int masked = mask & inverted_fp;
		//print_int_in_binary(masked, 32);
		int highest = Integer.highestOneBit(masked);
		//print_int_in_binary(highest, 32);
		int leading_zeros = Integer.numberOfTrailingZeros(highest);
		//System.out.println( leading_zeros );
		int age = fingerprintLength - leading_zeros - 1;
		//System.out.println( age );
		return age;
	}
	
	boolean rejuvenate(int key) {
		int large_hash = HashFunctions.normal_hash(key);
		long fingerprint = gen_fingerprint(large_hash);
		int ideal_index = get_slot_index(large_hash);
		
		boolean does_run_exist = is_occupied(ideal_index);
		if (!does_run_exist) {
			return false;
		}
		
		int run_start_index = find_run_start(ideal_index);
		int smallest_index = find_largest_matching_fingerprint_in_run(run_start_index, fingerprint);
		if (smallest_index == -1) {
			return false;
		}
		swap_fingerprints(smallest_index, fingerprint);
		return true; 
	}

	
	int decide_which_fingerprint_to_delete(int index, long fingerprint) {
		return find_largest_matching_fingerprint_in_run(index, fingerprint);
	}
	
	// returns the index of the entry if found, -1 otherwise
	int find_largest_matching_fingerprint_in_run(int index, long fingerprint) {
		assert(!is_continuation(index));
		int matching_fingerprint_index = -1;
		int lowest_age = Integer.MAX_VALUE;
		do {
			if (compare(index, fingerprint)) {
				//System.out.println("found matching FP at index " + index);
				int age = parse_unary(index);
				if (age < lowest_age) {
					lowest_age = age;
					matching_fingerprint_index = index;
				}
			}
			index++;
		} while (is_continuation(index));
		return matching_fingerprint_index; 
	}
	
	long gen_fingerprint(int large_hash) {
		int fingerprint_mask = (1 << fingerprintLength) - 1;
		fingerprint_mask = fingerprint_mask << power_of_two_size;
		int fingerprint = (large_hash & fingerprint_mask) >> power_of_two_size;
		int unary_mask = ~(1 << (fingerprintLength - 1));
		int updated_fingerprint = fingerprint & unary_mask;
		/*System.out.println(); 
		print_int_in_binary(unary_mask, fingerprintLength);
		print_int_in_binary( fingerprint, fingerprintLength);
		print_int_in_binary( updated_fingerprint, fingerprintLength);*/
		return updated_fingerprint;
	}
	
	void handle_empty_fingerprint(int bucket_index, QuotientFilter insertee) {
		int bucket1 = bucket_index;
		long bucket_mask = 1 << power_of_two_size; 		// setting this bit to the proper offset of the slot address field
		long bucket2 = bucket1 | bucket_mask;	// adding the pivot bit to the slot address field
		insertee.insert(empty_fingerprint, (int)bucket1, false);
		insertee.insert(empty_fingerprint, (int)bucket2, false);
	}
	
	void expand() {
		QuotientFilter new_qf = new QuotientFilter(power_of_two_size + 1, bitPerEntry);
		Iterator it = new Iterator(this);

		while (it.next()) {
			int bucket = it.bucket_index;
			long fingerprint = it.fingerprint;
			if (it.fingerprint != empty_fingerprint) {
				long pivot_bit = (1 & fingerprint);	// getting the bit of the fingerprint we'll be sacrificing 
				long bucket_mask = pivot_bit << power_of_two_size; // setting this bit to the proper offset of the slot address field
				long updated_bucket = bucket | bucket_mask;	// addding the pivot bit to the slot address field
				long chopped_fingerprint = fingerprint >> 1; // getting rid of this pivot bit from the fingerprint 
				int unary_mask = (1 << (fingerprintLength - 1));
				long updated_fingerprint = chopped_fingerprint | unary_mask;				
				new_qf.insert(updated_fingerprint, (int)updated_bucket, false);
			}
			else {
				handle_empty_fingerprint(it.bucket_index, new_qf);
			}
		}
		
		filter = new_qf.filter;
		power_of_two_size++;
		num_extension_slots += 2;
		max_entries_before_expansion = (int)(Math.pow(2, power_of_two_size) * expansion_threshold);

		
	}

}

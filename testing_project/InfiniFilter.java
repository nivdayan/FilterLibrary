
package testing_project;

import java.util.ArrayList;

public class InfiniFilter extends QuotientFilter {

	final long empty_fingerprint;

	/*enum EmptyFingerprintResolutionStyle {
		CHAINING,
		DUPLICATION,
	}*/	

	InfiniFilter(int power_of_two, int bits_per_entry) {
		super(power_of_two, bits_per_entry);
		max_entries_before_expansion = (int)(Math.pow(2, power_of_two_size) * expansion_threshold);
		empty_fingerprint = (1 << bits_per_entry - 3) - 2 ;
	}
	
	protected boolean compare(int index, long fingerprint) {
		int generation = parse_unary(index);
		int first_fp_bit = index * bitPerEntry + 3;
		int last_fp_bit = index * bitPerEntry + 3 + fingerprintLength - (generation + 1);
		for (int i = first_fp_bit, j = 0; i < last_fp_bit; i++, j++) {
			if (filter.get(i) != get_fingerprint_bit(j, fingerprint)) {
				return false;
			}
		}
		return true; 
	}
	
	// There is probably a more efficient implementation of this using rank&select
	// however, most fingerprints will have short unary codes 
	// the expected parsing time is therefore constant amortized time. 
	int parse_unary(int slot_index) {
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
		//this.pretty_print();
		long start = System.nanoTime();

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
				
				/*print_int_in_binary( (int)updated_bucket, power_of_two_size + 1);
				print_int_in_binary( (int)fingerprint, fingerprintLength);
				print_int_in_binary( (int)updated_fingerprint, fingerprintLength);
				System.out.println(); */
				
				new_qf.insert(updated_fingerprint, (int)updated_bucket, false);
			}
			else {
				handle_empty_fingerprint(it.bucket_index, new_qf);
			}
		}
		
		long end = System.nanoTime();
		double time = (end - start) / 1000;
		//System.out.println("time IF  " + time + "   " + new_qf.get_num_entries(false));
		
		filter = new_qf.filter;
		power_of_two_size++;
		num_extension_slots += 2;
		max_entries_before_expansion = (int)(Math.pow(2, power_of_two_size) * expansion_threshold);

	}

}

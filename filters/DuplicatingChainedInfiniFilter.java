package filters;

import java.util.ArrayList;

import filters.FingerprintGrowthStrategy.FalsePositiveRateExpansion;

public class DuplicatingChainedInfiniFilter extends ChainedInfiniFilter {

	final boolean lazy_deletes;
	long deleted_void_fingerprint = 0;
	ArrayList<Long> deleted_void_entries;
	//final long num_expansions_estimate;
	
	public static int derive_init_fingerprint_size(int expected_fp_bits, int expected_expansions) {
		//int new_fingerprint_size = FingerprintGrowthStrategy.get_new_fingerprint_size(expected_fp_bits, 0, expected_expansions, FalsePositiveRateExpansion.POLYNOMIAL_SHRINK);
		double original_FPR = Math.pow(2, -expected_fp_bits);
		double current = expected_expansions + 1;
		double factor = 1.0 / Math.pow(current, 2);
		double new_filter_FPR = factor * original_FPR; 
		double fingerprint_size = Math.ceil( Math.log(1.0/new_filter_FPR) / Math.log(2) );
		int fingerprint_size_int = (int) fingerprint_size;
		return fingerprint_size_int;
	}
	
	public DuplicatingChainedInfiniFilter(int power_of_two, int bits_per_entry, boolean _lazy_updates, int new_num_expansions_estimate) {
		super(power_of_two, bits_per_entry);
		set_deleted_void_fingerprint();
		deleted_void_entries = new ArrayList<>();
		lazy_deletes = _lazy_updates;
		num_expansions_estimate = new_num_expansions_estimate;
		//System.out.println(filter.size());
		if (num_expansions_estimate > -1) {
			fprStyle = FalsePositiveRateExpansion.POLYNOMIAL_SHRINK;
			fingerprintLength = FingerprintGrowthStrategy.get_new_fingerprint_size(fingerprintLength, 0, new_num_expansions_estimate, fprStyle);
			bitPerEntry = fingerprintLength + 3; 
			filter = make_filter(1L << power_of_two, bitPerEntry);
			empty_fingerprint = (1L << fingerprintLength) - 2 ;
			//original_fingerprint_size = fingerprintLength;
			//int f = original_fingerprint_size;
		}
		//System.out.println(filter.size());
		//System.out.println();
	}
	
	void set_deleted_void_fingerprint() {
		deleted_void_fingerprint = (1 << fingerprintLength) - 1;
		//print_long_in_binary(deleted_void_fingerprint, 32);
		//System.out.println();
	}
	
	void handle_empty_fingerprint(long bucket_index, QuotientFilter insertee) {
		//pretty_print();
		//super.handle_empty_fingerprint(bucket_index, insertee);
		long bucket1 = bucket_index;
		long bucket_mask = 1L << power_of_two_size; 		// setting this bit to the proper offset of the slot address field
		long bucket2 = bucket1 | bucket_mask;	// adding the pivot bit to the slot address field
		insertee.insert(empty_fingerprint, bucket1, false);
		insertee.insert(empty_fingerprint, bucket2, false);
		num_existing_entries++;
		num_void_entries += 1;
		//System.out.println("void splitting " + bucket1 + "  " + bucket2 );
		//pretty_print();
	}
	
	void report_void_entry_creation(long slot) {
		//System.out.println("empty FP created " + slot);
		super.report_void_entry_creation(slot);

		if (secondary_IF == null) {
			int power = power_of_two_size - num_expansions + 1;
			//int FP_size_wanted = power_of_two_size - num_expansions + 3; 
			int FP_size_min_size = power_of_two_size + 2 - power;
			//int FP_size = Math.min(FP_size_min_size, FP_size_wanted);
			
			create_secondary(power, FP_size_min_size );
			prep_masks(power_of_two_size + 1, secondary_IF.power_of_two_size, secondary_IF.fingerprintLength);
			set_deleted_void_fingerprint();
		}

		if (exceeding_secondary_threshold()) {
			//pretty_print();
			consider_expanding_secondary();
			prep_masks();
		}
		
		consider_widening();
		//prep_masks();
		
		/*if (slot == 3565) {
			System.out.println("" + secondary_IF.power_of_two_size);
			System.out.println("" + (secondary_IF.fingerprintLength - 1));
			System.out.println();
		}*/
		
		super.handle_empty_fingerprint(slot, this);	

	}
	
	void prep_masks() {
		if (secondary_IF == null) {
			return;
		}
		prep_masks(power_of_two_size + 1, secondary_IF.power_of_two_size, secondary_IF.fingerprintLength);
		set_deleted_void_fingerprint();
	}
	
	void remove_deleted_void_entry_duplicates() {
		for (Long s : deleted_void_entries) {
			boolean success = delete_duplicates(s);
			if (!success) {
				System.out.println("didn't delete duplicates");
				System.exit(1);
			}
		}
		deleted_void_entries.clear();
	}
	
	boolean exceeding_secondary_threshold() {
		int num_entries = secondary_IF.num_existing_entries;
		long logical_slots = secondary_IF.get_logical_num_slots();
		double secondary_fullness = num_entries / (double)logical_slots;
		return secondary_fullness > expansion_threshold;
	}
	
	boolean expand() {
		//if (num_expansions == 10) {
			//print_filter_summary();
			//print_age_histogram();
			//System.out.println();
		//}
		//pretty_print();
		//print_filter_summary();
		//print_age_histogram();
		//double util = get_utilization();
		//System.out.println("before expansion " + num_expansions + "\t" + util + "\t" + num_existing_entries + "\t" + num_void_entries + "\t" + num_distinct_void_entries);
		
		if (!deleted_void_entries.isEmpty()) {
			remove_deleted_void_entry_duplicates();
		}
		
		boolean success = super.expand();	
		/*if (secondary_IF != null) {
			secondary_IF.pretty_print();
			secondary_IF.expand();
			secondary_IF.pretty_print();
			secondary_IF.expand();
			secondary_IF.pretty_print();
		}*/
		
		set_deleted_void_fingerprint();
		
		//pretty_print();
		//print_filter_summary();
		//print_age_histogram();
		//System.out.println("after expansion " + num_expansions + "\t" + num_existing_entries + "\t" + num_void_entries + "\t" + num_distinct_void_entries);
		return success;
		
	}
	
	public boolean search(long input) {
		long hash = get_hash(input);
		return _search(hash);
	}
	
	protected boolean compare(long index, long fingerprint) {
		long f = get_fingerprint(index);	// it's not ideal that we get_fingerprint multiple times within these sub-methods 
		if (f == deleted_void_fingerprint) {
			return false;
		}
		return super.compare(index, fingerprint);
	}
	
	// returns the number of expansions ago that the entry with the longest matching hash turned void within a particular filter along the chain
	long get_void_entry_age(long orig_slot_index, BasicInfiniFilter bi) {
		
		long slot_index = bi.get_slot_index(orig_slot_index);
		long fp_long = bi.gen_fingerprint(orig_slot_index);
		
		long run_start_index = bi.find_run_start(slot_index);
		long matching_fingerprint_index = bi.find_largest_matching_fingerprint_in_run(run_start_index, fp_long);
		
		if (matching_fingerprint_index == -1) {
			// we didn't find a matching fingerprint
			return -1;
		}
		
		long unary_size = bi.parse_unary(matching_fingerprint_index) + 1;
		
		long hash_size = bi.power_of_two_size + bi.fingerprintLength - unary_size;
		
		long hash_diff = power_of_two_size - hash_size;
		//long existing_fp = get_fingerprint_after_unary(matching_fingerprint_index);
			
		if (hash_diff < 0) {
			System.out.println("problem!");
		}
		
		return hash_diff; 
	}
	
	// returns the number of expansions ago that the entry with the longest matching hash turned void 
	long get_void_entry_age(long slot_index) {
		long age = get_void_entry_age(slot_index, secondary_IF); 
		
		if (age != -1) {
			return age;
		}
		
		for (int i = chain.size() - 1; i >= 0; i--) {	
			age = get_void_entry_age(slot_index, chain.get(i)); 
			if (age != -1) {
				return age;
			}
		}
		return -1;
	}
	
	// returns the first void entry encountered in the run
	long find_first_void_entry_in_run(long index, long target_fingerprint) {
		do {
			//print_long_in_binary(get_fingerprint(index - 1), fingerprintLength);
			//print_long_in_binary(get_fingerprint(index), fingerprintLength);
			if (get_fingerprint(index) == target_fingerprint) {
				//System.out.println("found matching FP at index " + index);
				return index;
			}
			index++;
		} while (is_continuation(index));
		return -1; 
	}
	
	public boolean delete_duplicates(long slot_index, long age) {
		
		long num_duplicates = 1 << age;
		
		//System.out.println("num duplicates to remove " + num_duplicates);
		
		//print_long_in_binary(slot_index, power_of_two_size);

		long mask = (1 << (power_of_two_size - age)) - 1;
		//print_long_in_binary(mask, power_of_two_size);

		
		long first_duplicate_address = slot_index & mask;
		long dist_between_duplicates = 1 << (power_of_two_size - age);
		
		//print_long_in_binary(first_duplicate_address, (int)(power_of_two_size - age));
		
		for (int i = 0; i < num_duplicates; i++) {
			
			long canonical_addr = first_duplicate_address + i * dist_between_duplicates;
			long run_start_index = find_run_start(canonical_addr);
			
			long delete_target = canonical_addr == slot_index && lazy_deletes ? deleted_void_fingerprint : empty_fingerprint;
			
			long matching_fingerprint_index = find_first_void_entry_in_run(run_start_index, delete_target);
			if (matching_fingerprint_index == -1) {
				System.out.println("not founding duplicate to delete");
				System.exit(1);
			}
			
			//System.out.println();
			
			//System.out.println(canonical_addr + "  " + run_start_index + "  " +  matching_fingerprint_index);

			//System.out.println("removing duplicate " + canonical_addr + "  " + run_start_index + "  " + matching_fingerprint_index);
			
			boolean success = delete( empty_fingerprint,  canonical_addr,  run_start_index,  matching_fingerprint_index);
			if (!success) {
				System.out.println("there must be another void entry");
				return false;
			}
		}
		num_existing_entries -= num_duplicates;

		num_void_entries -= num_duplicates;
		//System.out.println(num_existing_entries + "  " + num_duplicates);
		//pretty_print();
		return true;
	}
	
	public boolean delete_void_lazily(long slot, long canonical_slot) {
		set_fingerprint(slot, deleted_void_fingerprint);
		deleted_void_entries.add(canonical_slot);
		return true; 
	}
	
	public long delete(long input) {
		
		long large_hash = get_hash(input);
		long slot_index = get_slot_index(large_hash);
		long fp_long = gen_fingerprint(large_hash);
		
		if (slot_index >= get_logical_num_slots()) {
			return -1;
		}
		// if the run doesn't exist, the key can't have possibly been inserted
		boolean does_run_exist = is_occupied(slot_index);
		if (!does_run_exist) {
			return -1;
		}
		long run_start_index = find_run_start(slot_index);
		
		long matching_fingerprint_index = decide_which_fingerprint_to_delete(run_start_index, fp_long);
		
		if (matching_fingerprint_index == -1) {
			// we didn't find a matching fingerprint
			return -1;
		}
		
		long matching_fingerprint = get_fingerprint(matching_fingerprint_index);

		if (matching_fingerprint != empty_fingerprint) {
			long removed_fp = delete(fp_long, slot_index);
			if (removed_fp > -1) {
				num_existing_entries--;
				return removed_fp;
			}			
		}
		
		if (lazy_deletes) {
			delete_void_lazily(matching_fingerprint_index, slot_index);
			return 1;
		}
		
		/*print_long_in_binary(large_hash, 32);
		print_long_in_binary(slot_index, 32);
		print_long_in_binary(fp_long, 32);*/
		
		boolean success = delete_duplicates(slot_index);
		
		return success ? 1 : -1;
	}
	
	
	boolean delete_duplicates(long slot_index) {
		long age = get_void_entry_age(slot_index);
		
		//System.out.println("the key has age " + age);
		
		boolean success = delete_duplicates(slot_index, age);
		if (!success) {
			return false;
		}

		num_distinct_void_entries--;
		
		long secondary_slot_index = secondary_IF.get_slot_index(slot_index);
		long fp_long = secondary_IF.gen_fingerprint(slot_index);
		long removed_fp = secondary_IF.delete(fp_long, secondary_slot_index);
		if (removed_fp > -1) {
			secondary_IF.num_existing_entries--;
			if (removed_fp == empty_fingerprint) {
				secondary_IF.num_void_entries--;
				secondary_IF.num_distinct_void_entries--;
			}
			return true;
		}
		
		for (int i = chain.size() - 1; i >= 0; i--) {			
			long chain_slot_index = chain.get(i).get_slot_index(slot_index);
			fp_long = chain.get(i).gen_fingerprint(slot_index);
			removed_fp = chain.get(i).delete(fp_long, chain_slot_index);
			if (removed_fp > -1) {
				chain.get(i).num_existing_entries--;
				if (removed_fp == empty_fingerprint) {
					secondary_IF.num_void_entries--;
					secondary_IF.num_distinct_void_entries--;
				}
				return true;
			}
		}
		
		return success; 
	}
	
	
	// measures the number of bits per entry for the filter 
	// it takes an array of filters as a parameter since some filter implementations here consist of multiple filter objects
	public double measure_num_bits_per_entry() {
		//System.out.println("--------------------------");
		//current.print_filter_summary();
		//System.out.println();
		//double num_entries = current.get_num_occupied_slots(false);
		double total_count = num_existing_entries;

		double init_size = 1L << power_of_two_size;
		double num_bits = bitPerEntry * init_size + num_extension_slots * bitPerEntry;
		
		if (secondary_IF != null) {
			init_size = 1L << secondary_IF.power_of_two_size;
			num_bits += secondary_IF.bitPerEntry * init_size + secondary_IF.num_extension_slots * secondary_IF.bitPerEntry;
		}
		
		for (QuotientFilter q : chain) {
			init_size = 1L << q.power_of_two_size;
			num_bits += q.bitPerEntry * init_size + q.num_extension_slots * q.bitPerEntry;
		}
		//System.out.println("total entries: \t\t" + num_entries);
		//System.out.println("total bits: \t\t" + num_bits);
		
		//System.out.println("entries:  " + total_count + "  bits:  " + num_bits); 

		
		double bits_per_entry = num_bits / total_count;
		//System.out.println("total bits/entry: \t" + bits_per_entry);
		//System.out.println();
 		return bits_per_entry;
	}
	
}

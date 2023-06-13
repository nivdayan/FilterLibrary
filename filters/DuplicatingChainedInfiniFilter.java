package filters;

import java.util.ArrayList;

public class DuplicatingChainedInfiniFilter extends ChainedInfiniFilter {

	final boolean lazy_deletes;
	long deleted_void_fingerprint = 0;
	ArrayList<Long> deleted_void_entries;
	
	public DuplicatingChainedInfiniFilter(int power_of_two, int bits_per_entry, boolean _lazy_updates) {
		super(power_of_two, bits_per_entry);
		set_deleted_void_fingerprint();
		deleted_void_entries = new ArrayList<>();
		lazy_deletes = _lazy_updates;
	}
	
	void set_deleted_void_fingerprint() {
		deleted_void_fingerprint = (1 << fingerprintLength) - 1;
		//print_long_in_binary(deleted_void_fingerprint, 32);
		//System.out.println();
	}
	
	void handle_empty_fingerprint(long bucket_index, QuotientFilter insertee) {
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
			int FP_size = power_of_two_size - num_expansions + 3; 
			create_secondary(power_of_two_size - num_expansions + 1, FP_size );
			prep_masks(power_of_two_size + 1, secondary_IF.power_of_two_size, secondary_IF.fingerprintLength);
			set_deleted_void_fingerprint();
		}

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
	
	boolean expand() {
		
		if (!deleted_void_entries.isEmpty()) {
			remove_deleted_void_entry_duplicates();
		}
		//System.out.println("expansion " + num_expansions);
		
		boolean success = super.expand();	
		/*if (secondary_IF != null) {
			secondary_IF.pretty_print();
			secondary_IF.expand();
			secondary_IF.pretty_print();
			secondary_IF.expand();
			secondary_IF.pretty_print();
		}*/
		//pretty_print();
		set_deleted_void_fingerprint();
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
				System.exit(1);
			}
			
			//System.out.println(canonical_addr + "  " + run_start_index + "  " +  matching_fingerprint_index);

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
	
	public boolean delete(long input) {
		
		long large_hash = get_hash(input);
		long slot_index = get_slot_index(large_hash);
		long fp_long = gen_fingerprint(large_hash);
		
		if (slot_index >= get_logical_num_slots()) {
			return false;
		}
		// if the run doesn't exist, the key can't have possibly been inserted
		boolean does_run_exist = is_occupied(slot_index);
		if (!does_run_exist) {
			return false;
		}
		long run_start_index = find_run_start(slot_index);
		
		long matching_fingerprint_index = decide_which_fingerprint_to_delete(run_start_index, fp_long);
		
		if (matching_fingerprint_index == -1) {
			// we didn't find a matching fingerprint
			return false;
		}
		
		long matching_fingerprint = get_fingerprint(matching_fingerprint_index);

		if (matching_fingerprint != empty_fingerprint) {
			boolean success = delete(fp_long, slot_index);
			if (success) {
				num_existing_entries--;
				return true;
			}			
		}
		
		if (lazy_deletes) {
			delete_void_lazily(matching_fingerprint_index, slot_index);
			return true;
		}
		
		/*print_long_in_binary(large_hash, 32);
		print_long_in_binary(slot_index, 32);
		print_long_in_binary(fp_long, 32);*/
		
		return delete_duplicates(slot_index);
	}
	
	
	boolean delete_duplicates(long slot_index) {
		long age = get_void_entry_age(slot_index);
		
		boolean success = delete_duplicates(slot_index, age);
		if (!success) {
			return false;
		}

		num_distinct_void_entries--;
		
		long secondary_slot_index = secondary_IF.get_slot_index(slot_index);
		long fp_long = secondary_IF.gen_fingerprint(slot_index);
		success = secondary_IF.delete(fp_long, secondary_slot_index);
		if (success) {
			secondary_IF.num_existing_entries--;
			return true;
		}
		
		for (int i = chain.size() - 1; i >= 0; i--) {			
			long chain_slot_index = chain.get(i).get_slot_index(slot_index);
			fp_long = chain.get(i).gen_fingerprint(slot_index);
			success = chain.get(i).delete(fp_long, chain_slot_index);
			if (success) {
				chain.get(i).num_existing_entries--;
				return true;
			}
		}
		
		return success; 
	}
	

	
	
}

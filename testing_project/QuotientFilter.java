package testing_project;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class QuotientFilter {

	int bitPerEntry;
	int fingerprintLength; 
	int power_of_two_size; 
	int num_extension_slots;
	int num_existing_entries;
	BitSet filter;
	double expansion_threshold;
	int max_entries_before_expansion;
	boolean expand_autonomously;
	boolean is_full;
	
	QuotientFilter(int power_of_two, int bits_per_entry) {
		// we actually allocate a quotient filter twice as needed for now to easily absorb overflows
		// this comes from the constraint that in java, BitSet is always a power of 2 bits
		// in a future C++ implementation, we would just have approx 2*log(N) extra slots
		power_of_two_size = power_of_two;
		bitPerEntry = bits_per_entry; 
		fingerprintLength = bits_per_entry - 3;
		int init_size = 1 << (power_of_two + 1);
		filter = new BitSet(bits_per_entry * init_size);
		num_extension_slots = power_of_two * 2;
		
		expansion_threshold = 0.8;
		max_entries_before_expansion = (int) ((init_size / 2.0) * expansion_threshold);
		expand_autonomously = false;
		is_full = false;
		
		//measure_num_bits_per_entry();
	}
	
	QuotientFilter(int power_of_two, int bits_per_entry, BitSet bitmap) {
		power_of_two_size = power_of_two;
		bitPerEntry = bits_per_entry; 
		fingerprintLength = bits_per_entry - 3;
		filter = bitmap;
		num_extension_slots = power_of_two * 2;
	}
	
	void expand() {
		is_full = true;
	}
	
	double measure_num_bits_per_entry() {
		/*double num_entries = get_num_entries();
		int init_size = 1 << power_of_two_size ;
		int num_bits = bitPerEntry * init_size + num_extension_slots * bitPerEntry;
		double bits_per_entry = num_bits / num_entries ;*/
		return measure_num_bits_per_entry(this, new ArrayList<QuotientFilter>());
	}
	
	protected static double measure_num_bits_per_entry(QuotientFilter current, ArrayList<QuotientFilter> other_filters) {
		//System.out.println("--------------------------");
		//current.print_filter_summary();
		//System.out.println();
		double num_entries = current.get_num_entries(false);
		for (QuotientFilter q : other_filters) {
			//q.print_filter_summary();
			//System.out.println();
			int q_num_entries = q.get_num_entries(false);
			num_entries += q_num_entries;
		}
		int init_size = 1 << current.power_of_two_size;
		int num_bits = current.bitPerEntry * init_size + current.num_extension_slots * current.bitPerEntry;
		for (QuotientFilter q : other_filters) {
			init_size = 1 << q.power_of_two_size;
			num_bits += q.bitPerEntry * init_size + q.num_extension_slots * q.bitPerEntry;
		}
		
		//System.out.println("total entries: \t\t" + num_entries);
		//System.out.println("total bits: \t\t" + num_bits);
		double bits_per_entry = num_bits / num_entries;

		//System.out.println("total bits/entry: \t" + bits_per_entry);
		//System.out.println();

 		return bits_per_entry;
	}
	
	int get_num_entries(boolean include_all_internal_filters) {
		int slots = get_physcial_num_slots();
		int num_entries = 0;
		for (int i = 0; i < slots; i++) {
			if (is_occupied(i) || is_continuation(i) || is_shifted(i)) {
				num_entries++;
			}
		}
		return num_entries;
	}
	
	double get_utilization() {
		int num_logical_slots = 1 << power_of_two_size;
		int num_entries = get_num_entries(false);
		double util = num_entries / (double) num_logical_slots;
		return util;
	}
	
	public int get_physcial_num_slots() {
		return filter.size() / bitPerEntry;
	}
	
	public int get_logical_num_slots() {
		return filter.size() / (bitPerEntry ) + num_extension_slots;
	}
	
	void modify_slot(boolean is_occupied, boolean is_continuation, boolean is_shifted, 
			int index) {
		set_occupied(index, is_occupied);
		set_continuation(index, is_continuation);
		set_shifted(index, is_shifted);
	}
	
	void set_fingerprint(int index, long fingerprint) {
		for (int i = index * bitPerEntry + 3, j = 0; i < index * bitPerEntry + 3 + fingerprintLength; i++, j++) {
			filter.set(i, get_fingerprint_bit(j, fingerprint));
		}
	}
	
	long get_fingerprint(int index) {
		long fingerprint = 0;
		for (int i = index * bitPerEntry + 3, j = 0; i < index * bitPerEntry + 3 + fingerprintLength; i++, j++) {
			fingerprint = set_fingerprint_bit(j, fingerprint, filter.get(i));
		}
		return fingerprint;
	}
	
	void print_fingerprint(BitSet fp) {
		for (int i = 0; i < fingerprintLength; i++) {
			System.out.print(fp.get(i) ? "1" : "0");
		}
		System.out.println();
	}
	
	protected boolean compare(int index, long fingerprint) {
		for (int i = index * bitPerEntry + 3, j = 0; i < index * bitPerEntry + 3 + fingerprintLength; i++, j++) {
			if (filter.get(i) != get_fingerprint_bit(j, fingerprint)) {
				return false;
			}
		}
		return true; 
	}
	
	
	void modify_slot(boolean is_occupied, boolean is_continuation, boolean is_shifted, 
			int index, long fingerprint) {
		modify_slot(is_occupied, is_continuation, is_shifted, index);
		set_fingerprint(index, fingerprint);
	}
	
	
	public void print() {	
		for (int i = 0; i < filter.size(); i++) {
			System.out.print(filter.get(i) ? "1" : "0");
		}
		System.out.println();
	}
	
	public void print_important_bits() {	
		for (int i = 0; i < filter.size(); i++) {
			int remainder = i % bitPerEntry;
			if (remainder == 0) {
				System.out.print(" ");
			}
			if (remainder == 0 || remainder == 1 || remainder == 2) {
				System.out.print(filter.get(i) ? "1" : "0");
			}
		}
		System.out.println();
	}
	
	public String get_pretty_str(boolean vertical) {
		StringBuffer sbr = new StringBuffer();
		
		for (int i = 0; i < filter.size(); i++) {
			int remainder = i % bitPerEntry;
			if (remainder == 0) {
				sbr.append(" ");
				if (vertical) {
					sbr.append("\n" + (i/bitPerEntry) + " ");
				}
			}
			if (remainder == 3) {
				sbr.append(" ");
			}
			sbr.append(filter.get(i) ? "1" : "0");
		}
		sbr.append("\n");
		return sbr.toString();
	}
	
	public void pretty_print() {	
		System.out.print(get_pretty_str(true));
	}
	
	public void print_filter_summary() {	
		int num_entries = get_num_entries(false);
		
		int slots = (1 << power_of_two_size) + num_extension_slots;
		int num_bits = slots * bitPerEntry;
		
		System.out.println("slots:\t" + slots);
		System.out.println("entries:\t" + num_entries);
		System.out.println("bits\t:" + num_bits);
		System.out.println("bits/entry\t:" + num_bits / (double)num_entries);
	}
	
	boolean is_occupied(int index) {
		return filter.get(index * bitPerEntry);
	}
	
	boolean is_continuation(int index) {
		return filter.get(index * bitPerEntry + 1);
	}
	
	boolean is_shifted(int index) {
		return filter.get(index * bitPerEntry + 2);
	}
	
	void set_occupied(int index, boolean val) {
		filter.set(index * bitPerEntry, val);
	}
	
	void set_continuation(int index, boolean val) {
		filter.set(index * bitPerEntry + 1, val);
	}
	
	void set_shifted(int index, boolean val) {
		filter.set(index * bitPerEntry + 2, val);
	}
	
	boolean is_slot_empty(int index) {
		return !is_occupied(index) && !is_continuation(index) && !is_shifted(index);
	}
	
	int find_cluster_start(int index) {
		int current_index = index;
		while (is_shifted(current_index)) {
			current_index--;
		}
		return current_index;
	}
	
	int find_run_start(int index) {
		int current_index = index;
		int num_runs = 1;
		while (is_shifted(current_index)) {
			if (is_occupied(current_index)) {
				num_runs++;
			}
			current_index--;
			//System.out.println("current_index  " + current_index);
		}
		
		while (true) {
			if (!is_continuation(current_index)) {
				num_runs--;
				if (num_runs == 0) {
					return current_index;
				}
			}
			current_index++;
			//System.out.println("current_index  " + current_index);
		}
		//System.out.println("current_index  " + current_index);
	}
	
	// returns the index of the entry if found, -1 otherwise
	int find_first_fingerprint_in_run(int index, long fingerprint) {
		assert(!is_continuation(index));
		do {
			if (compare(index, fingerprint)) {
				//System.out.println("found matching FP at index " + index);
				return index; 
			}
			index++;
		} while (is_continuation(index));
		return -1; 
	}
	
	// returns the index of the entry if found, -1 otherwise
	int find_last_matching_fingerprint_in_run(int index, long fingerprint) {
		assert(!is_continuation(index));
		int matching_fingerprint_index = -1;
		do {
			if (compare(index, fingerprint)) {
				//System.out.println("found matching FP at index " + index);
				matching_fingerprint_index = index;
			}
			index++;
		} while (is_continuation(index));
		return matching_fingerprint_index; 
	}
	
	// find end of run index
	int find_run_end(int index) {
		//assert(!is_continuation(index));
		do {
			index++;
		} while (is_continuation(index));
		return index - 1; 
	}
		
	boolean search(long fingerprint, int index) {
		boolean does_run_exist = is_occupied(index);
		if (!does_run_exist) {
			return false;
		}
		int run_start_index = find_run_start(index);
		//long long_fp = convert(fingerprint);
		int found_index = find_first_fingerprint_in_run(run_start_index, fingerprint);
		return found_index > -1;
	}
	
	// Used for testing
	Set<Long> get_all_fingerprints(int bucket_index) {
		boolean does_run_exist = is_occupied(bucket_index);
		HashSet<Long> set = new HashSet<Long>();
		if (!does_run_exist) {
			return set;
		}
		int run_index = find_run_start(bucket_index);
		do {
			set.add(get_fingerprint(run_index));
			run_index++;
		} while (is_continuation(run_index));		
		return set;
	}
	
	long swap_fingerprints(int index, long new_fingerprint) {
		long existing = get_fingerprint(index);
		set_fingerprint(index, new_fingerprint);
		return existing;
	}
	
	int find_first_empty_slot(int index) {
		while (!is_slot_empty(index)) {
			index++;
		}
		return index;
	}
	
	int find_new_run_location(int index) {
		if (!is_slot_empty(index)) {
			index++;
		}
		while (is_continuation(index)) {
			index++;
		}
		return index;
	}
	
	boolean insert_new_run(int index, long long_fp) {
		int first_empty_slot = find_first_empty_slot(index);
		int run_start_index = find_run_start(index);
		int start_of_this_new_run = find_new_run_location(run_start_index);
		//System.out.println(first_empty_slot + "  " + start_of_this_new_run);

		//print_important_bits();
		
		boolean slot_initially_empty = is_slot_empty(start_of_this_new_run);
		set_occupied(index, true);
		if (first_empty_slot != index) {
			set_shifted(start_of_this_new_run, true);
		}
		set_continuation(start_of_this_new_run, false);
		if (slot_initially_empty) {
			set_fingerprint(start_of_this_new_run, long_fp);
			num_existing_entries++;
			return true; 
		}
		
		int current_index = start_of_this_new_run;
		boolean is_this_slot_empty;
		boolean finished_first_run = false;
		boolean temp_continuation = false;
		
		//long long_fp = convert(fingerprint);
		
		do {
			
			if (current_index >= get_logical_num_slots()) {
				return false;
			}
			
			is_this_slot_empty = is_slot_empty(current_index);
			long_fp = swap_fingerprints(current_index, long_fp);
			//System.out.println();

			if (current_index > start_of_this_new_run) {
				set_shifted(current_index, true);
			}
			
			/*if (current_index > start_of_this_new_run && !finished_first_run && !is_continuation(current_index)) {
				finished_first_run = true;
				set_continuation(current_index, false);
			}
			else if (finished_first_run) { */
			if (current_index > start_of_this_new_run) {
				boolean current_continuation = is_continuation(current_index);
				set_continuation(current_index, temp_continuation);
				temp_continuation = current_continuation;
			}

			current_index++;
			//is_new_run = !is_continuation(current_index);
		} while (!is_this_slot_empty);
		num_existing_entries++;
		return true; 
	}
	
	boolean insert(BitSet fingerprint, int index, boolean insert_only_if_no_match) {
		long long_fp = convert(fingerprint);
		return insert(long_fp, index, insert_only_if_no_match);
	}

	
	boolean insert(long long_fp, int index, boolean insert_only_if_no_match) {
		if (index >= get_logical_num_slots()) {
			return false;
		}
		boolean does_run_exist = is_occupied(index);
		if (!does_run_exist) {
			boolean val = insert_new_run(index, long_fp);
			return val;
		}
		
		int run_start_index = find_run_start(index);
		if (does_run_exist && insert_only_if_no_match) {
			int found_index = find_first_fingerprint_in_run(run_start_index, long_fp);
			if (found_index > -1) {
				return false; 
			}
		} 
				
		int current_index = run_start_index;
		boolean is_this_slot_empty;
		boolean finished_first_run = false;
		boolean temp_continuation = false;
				
		do {
			
			if (current_index >= get_logical_num_slots()) {
				return false;
			}
			
			is_this_slot_empty = is_slot_empty(current_index);
			long_fp = swap_fingerprints(current_index, long_fp);

			if (current_index > run_start_index) {
				set_shifted(current_index, true);
			}
			
			if (current_index > run_start_index && !finished_first_run && !is_continuation(current_index)) {
				finished_first_run = true;
				set_continuation(current_index, true);
			}
			else if (finished_first_run) {
				boolean current_continuation = is_continuation(current_index);
				set_continuation(current_index, temp_continuation);
				temp_continuation = current_continuation;
			}

			current_index++;
		} while (!is_this_slot_empty);
		//System.out.println("filter size: " + filter.size() + " bits");
		num_existing_entries++;
		return true;
	}
	
	boolean delete(BitSet fingerprint, int index) {
		boolean deleted = delete(convert(fingerprint), index);
		if (deleted) {
			num_existing_entries--;
		}
		return deleted;
	}
	
	boolean delete(long fingerprint, int index) {
		if (index >= get_logical_num_slots()) {
			return false;
		}
		// if the run doesn't exist, the key can't have possibly been inserted
		boolean does_run_exist = is_occupied(index);
		if (!does_run_exist) {
			return false;
		}
		int run_start_index = find_run_start(index);
		
		int matching_fingerprint_index = find_last_matching_fingerprint_in_run(run_start_index, fingerprint);
		int last_entry_index = find_run_end(matching_fingerprint_index);
		//run_scan_result res = scan_run_fully(run_start_index, fingerprint);
		
		
		if (matching_fingerprint_index == -1) {
			return false;
		}
		
		// the run has only one entry, so we disable its is_occupied flag
		if (run_start_index == matching_fingerprint_index) {
			set_occupied(index, false);
		}
		else {
			set_shifted(last_entry_index, false);
			set_continuation(last_entry_index, false);
		}
		
		int occupied_stack = 0;
		for (int i = find_cluster_start(index); i <= last_entry_index; i++) {
			if (is_occupied(i)) {
				occupied_stack++;
			}
		}
		
		// First thing to do is move everything else in the run back by one slot
		for (int i = matching_fingerprint_index; i < last_entry_index; i++) {
			long f = get_fingerprint(index + 1);
			set_fingerprint(index, f);
		}
		set_fingerprint(last_entry_index, 0); // can be commented out later 
		
		// we now need to decide about shifting the next run
		// we assume current_index is always empty, and we consider shifting the next slot back to it
		//int num_runs = 0;
		int num_runs_seen = 1;
		do {
			num_runs_seen++;
			boolean does_next_run_exist = !is_slot_empty(last_entry_index + 1);
			boolean is_next_run_shifted = is_shifted(last_entry_index + 1);
			if (!does_next_run_exist || !is_next_run_shifted) {
				return true;
			}
			int next_run_start_index = last_entry_index + 1;
			matching_fingerprint_index = find_last_matching_fingerprint_in_run(next_run_start_index, fingerprint);
			last_entry_index = find_run_end(next_run_start_index);
			
			//boolean is_blank_slot_occupied = is_occupied(next_run_start_index - 1);
			//num_occupied_slots_encountered += is_blank_slot_occupied ? 1 : 0;
			
			//if (occupied_stack > 1) {
			if ( is_occupied(next_run_start_index - 1) && occupied_stack == num_runs_seen ) {
				set_shifted(next_run_start_index - 1, false);
			}
			else {
				set_shifted(next_run_start_index - 1, true);
			}
			
			//set_shifted(next_run_start_index - 1, occupied_stack > 1);
			//}
			//occupied_stack--;

			for (int i = next_run_start_index; i <= last_entry_index; i++) {
				long f = get_fingerprint(i);
				set_fingerprint(i - 1, f);
				if (is_continuation(i)) {
					set_continuation(i-1, true);
				}
				if (is_occupied(i)) {
					occupied_stack++;
				}
			}
			set_fingerprint(last_entry_index, 0);
			set_shifted(last_entry_index, false);
			set_continuation(last_entry_index, false);
			
		} while (true);
		
	}
	
	void print_int_in_binary(int num, int length) {
		String str = "";
		for (int i = 0; i < length; i++) {
			int mask = (int)Math.pow(2, i);
			int masked = num & mask;
			str += masked > 0 ? "1" : "0";
		}
		System.out.println(str);
	}
	
	String get_fingerprint_str(long fp, int length) {
		String str = "";
		for (int i = 0; i < length; i++) {
			str += get_fingerprint_bit(i, fp) ? "1" : "0";
		}
		return str;
	}
	
	int get_slot_index(int large_hash) {
		int slot_index_mask = (1 << power_of_two_size) - 1;
		int slot_index = large_hash & slot_index_mask;
		return slot_index;
	}
	
	long gen_fingerprint(int large_hash) {
		int fingerprint_mask = (1 << fingerprintLength) - 1;
		fingerprint_mask = fingerprint_mask << power_of_two_size;
		int fingerprint = (large_hash & fingerprint_mask) >> power_of_two_size;
		/*BitSet fingerprint_bs = new BitSet(fingerprintLength);
		for (int i = 0; i < fingerprintLength; i++) {
			int mask = (int)Math.pow(2, i);
			int masked = fingerprint & mask;
			boolean val = masked > 0;
			fingerprint_bs.set(i, val);
			//System.out.println(val);
		}*/
		return fingerprint;
	}
	
	void print_key(int input) {
		int large_hash = HashFunctions.normal_hash(input);
		int slot_index = get_slot_index(large_hash);
		long fingerprint = gen_fingerprint(large_hash);
		
		System.out.println("num   :  " + input);
		System.out.print("hash  :  ");
		print_int_in_binary(large_hash, fingerprintLength + power_of_two_size);
		//print_int_in_binary(slot_index_mask, 31);
		System.out.print("bucket:  ");
		print_int_in_binary(slot_index, power_of_two_size);
		System.out.print("FP    :  ");
		//print_int_in_binary(fingerprint_mask, 31);
		print_int_in_binary((int)fingerprint, fingerprintLength);
		System.out.println();

	}
	
	boolean insert(int input, boolean insert_only_if_no_match) {
		if (is_full) {
			return false;
		}
		int large_hash = HashFunctions.normal_hash(input);
		int slot_index = get_slot_index(large_hash);
		long fingerprint = gen_fingerprint(large_hash);
		
		/*if (input == 1) {
			print_int_in_binary( large_hash, power_of_two_size + fingerprintLength);
			print_int_in_binary( slot_index, power_of_two_size);
			print_int_in_binary( (int)fingerprint, fingerprintLength);
		}*/
		
		boolean success = insert(fingerprint, slot_index, false);
		//if (!success) {
		if (!success) {
			System.out.println(input + "\t" + slot_index + "\t" + get_fingerprint_str(fingerprint, fingerprintLength));
			pretty_print();
			System.exit(1);
		}
		
		if (expand_autonomously && num_existing_entries >= max_entries_before_expansion) {
			expand();
		}
		
		return success; 
	}
	
	void set_expansion_threshold(double thresh) {
		expansion_threshold = thresh;
		max_entries_before_expansion = (int)(Math.pow(2, power_of_two_size) * expansion_threshold);
	}
	
	boolean delete(int input, boolean insert_only_if_no_match) {
		int large_hash = HashFunctions.normal_hash(input);
		int slot_index = get_slot_index(large_hash);
		long fp_long = gen_fingerprint(large_hash);
		boolean success = delete(fp_long, slot_index);
		if (!success) {
			System.out.println(input + "\t" + slot_index + "\t" + get_fingerprint_str(fp_long, fingerprintLength));
			pretty_print();
			System.exit(1);
		}
		return success; 
	}
	
	boolean search(int input) {
		int large_hash = HashFunctions.normal_hash(input);
		int slot_index = get_slot_index(large_hash);
		long fingerprint = gen_fingerprint(large_hash);
		
		return search(fingerprint, slot_index);
	}
	
	public boolean get_bit_at_offset(int offset) {
		return filter.get(offset);
	}
	
	public static boolean get_fingerprint_bit(int index, long fingerprint) {
		long mask = 1 << index;
		long and = fingerprint & mask;
		return and != 0;
	}
	
	public static long set_fingerprint_bit(int index, long fingerprint, boolean val) {
		if (val) {
			fingerprint |= 1 << index;   
		}
		else {
			fingerprint &= ~(1 << index);   
		}
		return fingerprint;
	}
	
	public long convert(BitSet fp) {
		long fp_long = 0;
		for (int i = 0; i < fingerprintLength; i++) {
			fp_long = set_fingerprint_bit(i, fp_long, fp.get(i));
		}
		return fp_long;
	}
	
	public BitSet convert(long fp) {
		BitSet bs = new BitSet(fingerprintLength);
		for (int i = 0; i < fingerprintLength; i++) {
			bs.set(i, get_fingerprint_bit(i, fp));
			//fp_long = set_fingerprint_bit(i, fp_long, fp.get(i));
		}
		return bs;
	}
	




	

	




}





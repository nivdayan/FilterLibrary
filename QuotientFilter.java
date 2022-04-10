import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Random;

public class QuotientFilter {

	int bitPerEntry;
	int fingerprintLength; 
	int power_of_two_size; 
	int num_extension_slots;
	BitSet filter;
	
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
	}
	
	public int get_physcial_num_slots() {
		return filter.size() / bitPerEntry;
	}
	
	public int get_logical_num_slots() {
		return filter.size() / bitPerEntry + num_extension_slots;
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
	
	boolean compare(int index, BitSet fingerprint) {
		for (int i = index * bitPerEntry + 3, j = 0; i < index * bitPerEntry + 3 + fingerprintLength; i++, j++) {
			if (filter.get(i) != fingerprint.get(j)) {
				return false;
			}
		}
		return true; 
	}
	
	boolean compare(int index, long fingerprint) {
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
	
	public String get_pretty_str() {
		StringBuffer sbr = new StringBuffer();
		
		for (int i = 0; i < filter.size(); i++) {
			int remainder = i % bitPerEntry;
			if (remainder == 0) {
				sbr.append(" ");
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
		System.out.print(get_pretty_str());
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
		return true;
	}
	
	boolean delete(BitSet fingerprint, int index) {
		return delete(convert(fingerprint), index);
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
	
	boolean insert(int input, boolean insert_only_if_no_match) {
		int large_hash = HashFunctions.normal_hash(input);
		int slot_index = get_slot_index(large_hash);
		long fingerprint = gen_fingerprint(large_hash);

		/*print_int_in_binary(input, 31);
		print_int_in_binary(large_hash, 31);
		print_int_in_binary(slot_index_mask, 31);
		print_int_in_binary(slot_index, 31);
		print_int_in_binary(fingerprint_mask, 31);
		print_int_in_binary(fingerprint, 31);
		System.out.println(slot_index);*/
		boolean success = insert(fingerprint, slot_index, false);
		//if (!success) {
		if (!success) {
			System.out.println(input + "\t" + slot_index + "\t" + get_fingerprint_str(fingerprint, fingerprintLength));
			pretty_print();
			System.exit(1);
		}
		return success; 
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
	
	static public boolean check_equality(QuotientFilter qf, BitSet bs, boolean check_also_fingerprints) {
		for (int i = 0; i < bs.size(); i++) {
			if (check_also_fingerprints || (i % qf.bitPerEntry == 0 || i % qf.bitPerEntry == 1 || i % qf.bitPerEntry == 2)) {
				if (qf.get_bit_at_offset(i) != bs.get(i)) {
					System.out.println("failed test: bit " + i);
					System.exit(1);
				}
			}
		}
		return true;
	}

	
	// This test is based on the example from https://en.wikipedia.org/wiki/Quotient_filter
	// it performs the same insertions and query as the example and verifies that it gets the same results. 
	static public void test1() {
		int bits_per_entry = 8;
		int num_entries_power = 3;
		int num_entries = (int)Math.pow(2, num_entries_power);
		QuotientFilter qf = new QuotientFilter(num_entries_power, bits_per_entry);

		long fingerprint0 = 0;
		long fingerprint1 = (1 << bits_per_entry) - 1;
		//System.out.println(fingerprint1);
	
		qf.insert(fingerprint0, 1, false);
		qf.insert(fingerprint1, 4, false);
		qf.insert(fingerprint0, 7, false);
		//qf.pretty_print();
		qf.insert(fingerprint0, 1, false);
		qf.insert(fingerprint0, 2, false);
		//qf.pretty_print();
		qf.insert(fingerprint0, 1, false);
		
		// these are the expecting resulting is_occupied, is_continuation, and is_shifted bits 
		// for all slots contigously. We do not store the fingerprints here
		BitSet result = new BitSet(num_entries * bits_per_entry);		
		result = set_slot_in_test(result, bits_per_entry, 0, false, false, false, fingerprint0);
		result = set_slot_in_test(result, bits_per_entry, 1, true, false, false, fingerprint0);
		result = set_slot_in_test(result, bits_per_entry, 2, true, true, true, fingerprint0);
		result = set_slot_in_test(result, bits_per_entry, 3, false, true, true, fingerprint0);
		result = set_slot_in_test(result, bits_per_entry, 4, true, false, true, fingerprint0);
		result = set_slot_in_test(result, bits_per_entry, 5, false, false, true, fingerprint1);
		result = set_slot_in_test(result, bits_per_entry, 6, false, false, false, fingerprint0);
		result = set_slot_in_test(result, bits_per_entry, 7, true, false, false, fingerprint0);
		//qf.pretty_print();
		check_equality(qf, result, true);
	}
	
	
	// This test is based on the example from the quotient filter paper 
	// it performs the same insertions as in Figure 2 and checks for the same result
	static public void test2() {
		int bits_per_entry = 8;
		int num_entries_power = 4;
		int num_entries = (int)Math.pow(2, num_entries_power);
		QuotientFilter qf = new QuotientFilter(4, 8);
		
		qf.insert(0, 1, false);
		qf.insert(0, 1, false);
		qf.insert(0, 3, false);
		qf.insert(0, 3, false);
		qf.insert(0, 3, false);
		qf.insert(0, 4, false);
		qf.insert(0, 6, false);
		qf.insert(0, 6, false);
			
		BitSet result = new BitSet(num_entries * bits_per_entry);		
		result = set_slot_in_test(result, bits_per_entry, 0, false, false, false, 0);
		result = set_slot_in_test(result, bits_per_entry, 1, true, false, false, 0);
		result = set_slot_in_test(result, bits_per_entry, 2, false, true, true, 0);
		result = set_slot_in_test(result, bits_per_entry, 3, true, false, false, 0);
		result = set_slot_in_test(result, bits_per_entry, 4, true, true, true, 0);
		result = set_slot_in_test(result, bits_per_entry, 5, false, true, true, 0);
		result = set_slot_in_test(result, bits_per_entry, 6, true, false, true, 0);
		result = set_slot_in_test(result, bits_per_entry, 7, false, false, true, 0);
		result = set_slot_in_test(result, bits_per_entry, 8, false, true, true, 0);
		check_equality(qf, result, false);
		
	}
	
	// Here we create a large(ish) filter, insert some random entries into it, and then make sure 
	// we get (true) positives for all entries we had inserted. 
	// This is to verify we do not get any false negatives. 
	// We then also check the false positive rate 
	static public void test3() {
		int bits_per_entry = 10;
		int num_entries_power = 5;
		int seed = 5; 
		//int num_entries = (int)Math.pow(2, num_entries_power);
		QuotientFilter qf = new QuotientFilter(num_entries_power, bits_per_entry);
		HashSet<Integer> added = new HashSet<Integer>();
		Random rand = new Random(seed);
		double load_factor = 1.00;
		for (int i = 0; i < qf.get_physcial_num_slots() * load_factor; i++) {
			int rand_num = rand.nextInt();
			boolean success = qf.insert(rand_num, false);
			if (success) {
				added.add(rand_num);
			}
			else {
				System.out.println("insertion failed");
			}
			
		}
		//qf.print_important_bits();
		//qf.pretty_print();
		
		for (Integer i : added) {
			//System.out.println("searching  " + i );
			boolean found = qf.search(i);
			if (!found) {
				System.out.println("something went wrong!! seem to have false negative " + i);
				qf.search(i);
				System.exit(1);
			}
		}
		

	}
	
	static public void experiment_false_positives() {
		int bits_per_entry = 10;
		int num_entries_power = 5;
		int seed = 5; 
		//int num_entries = (int)Math.pow(2, num_entries_power);
		int fingerprint_size = bits_per_entry - 3;
		QuotientFilter qf = new QuotientFilter(num_entries_power, bits_per_entry);
		HashSet<Integer> added = new HashSet<Integer>();
		Random rand = new Random(seed);
		double load_factor = 0.9;
		int num_queries = 20000;
		int num_false_positives = 0;
		
		for (int i = 0; i < qf.get_physcial_num_slots() * load_factor; i++) {
			int rand_num = rand.nextInt();
			boolean success = qf.insert(rand_num, false);
			if (success) {
				added.add(rand_num);
			}
			else {
				System.out.println("insertion failed");
			}
			
		}
		
		for (int i = 0; i < num_queries; i++) {
			int rand_num = rand.nextInt();
			if (!added.contains(rand_num)) {
				boolean found = qf.search(i);
				if (found) {
					//System.out.println("we seem to have a false positive");
					num_false_positives++;
				}
			}
		}
		double FPR = num_false_positives / (double)num_queries;
		System.out.println("measured FPR:\t" + FPR);
		double expected_FPR = Math.pow(2, - fingerprint_size);
		System.out.println("single fingerprint model:\t" + expected_FPR);
		double expected_FPR_bender = 1 - Math.exp(- load_factor / Math.pow(2, fingerprint_size));
		System.out.println("bender model:\t" + expected_FPR_bender);
	}
	
	static public void experiment_insertion_speed() {
		int bits_per_entry = 3;
		int num_entries_power = 12;
		int seed = 5; 
		//int num_entries = (int)Math.pow(2, num_entries_power);
		int fingerprint_size = bits_per_entry - 3;
		QuotientFilter qf = new QuotientFilter(num_entries_power, bits_per_entry);
		Random rand = new Random(seed);
		double load_factor = 0.1;
		int num_queries = 20000;
		int num_false_positives = 0;
		double num_insertions = qf.get_physcial_num_slots() * load_factor; 
		long start = System.nanoTime();
		long time_sum = 0;
		long time_sum_square = 0;
		for (int i = 0; i < num_insertions; i++) {
			long start1 = System.nanoTime();
			int rand_num = rand.nextInt();
			boolean success = qf.insert(rand_num, false);

			long end1 = System.nanoTime(); 
			//if (i > 5) {
			long time_diff = (end1 - start1);
			time_sum += time_diff;
			time_sum_square += time_diff * time_diff; 
			//}
			//System.out.println("execution time :\t" + ( end1 - start1) / (1000.0) + " mic s");	
		}
		long end = System.nanoTime(); 
		System.out.println("execution time :\t" + ( end - start) / (1000.0 * 1000.0) + " ms");
		System.out.println("execution time per entry :\t" + ( end - start) / (num_insertions * 1000.0) + " mic sec");
		
		double avg_nano = time_sum / num_insertions;
		System.out.println("avg :\t" + (avg_nano / 1000.0));

		double avg_normalized = avg_nano / 1000.0;
		double time_sum_square_normalized = time_sum_square / 1000000.0 ;
		double variance = (time_sum_square_normalized - avg_normalized * avg_normalized * num_insertions) / num_insertions;
		double std = Math.sqrt(variance);
		System.out.println("std :\t" + std);
	}

	
	// adds two entries to the end of the filter, causing an overflow
	// checks this can be handled
	static public void test4() {
		int bits_per_entry = 8;
		int num_entries_power = 3;
		int num_entries = (int)Math.pow(2, num_entries_power);
		int fingerprint_size = bits_per_entry - 3;
		QuotientFilter qf = new QuotientFilter(num_entries_power, bits_per_entry);

		long fp2 = 1 << fingerprint_size - 1;
	
		qf.insert(fp2, num_entries - 1, false);
		qf.insert(fp2, num_entries - 1, false);
		
		qf.pretty_print();
		
		qf.delete(fp2, num_entries - 1);
		boolean found = qf.search(fp2, num_entries - 1);
		if (!found) {
			System.out.println("Should have found the entry");
			System.exit(1);
		}
	}
	
	static public BitSet set_slot_in_test(BitSet result, int bits_per_entry, int slot, boolean is_occupied, boolean is_continuation, boolean is_shifted, long fingerprint) {
		int index = bits_per_entry * slot;
		result.set(index++, is_occupied); 
		result.set(index++, is_continuation); 
		result.set(index++, is_shifted); 
		for (int i = 0; i < bits_per_entry - 3; i++) {
			result.set(index++, get_fingerprint_bit(i, fingerprint) );
		}
		return result;
	}

	// This is a test for deleting items. We insert many keys into one slot to create an overflow. 
	// we then remove them and check that the other keys are back to their canonical slots. 
	static public void test5() {
		int bits_per_entry = 8;
		int num_entries_power = 3;
		int num_entries = (int)Math.pow(2, num_entries_power);
		int fingerprint_size = bits_per_entry - 3;
		QuotientFilter qf = new QuotientFilter(num_entries_power, bits_per_entry);

		long fp1 = 1 << 4;
		long fp2 = 1 << 3;
		long fp3 = 1 << 2;
	
		qf.insert(fp1, 1, false);
		qf.insert(fp1, 1, false);
		qf.insert(fp1, 1, false);
		qf.insert(fp1, 1, false);
		qf.insert(fp1, 1, false);
		qf.insert(fp2, 2, false);
		qf.insert(fp3, 4, false);
		
		qf.pretty_print();
		qf.delete(fp1, 1);
		qf.delete(fp1, 1);
		qf.delete(fp1, 1);
		qf.delete(fp1, 1);
		qf.delete(fp1, 1);

		BitSet result = new BitSet(num_entries * bits_per_entry);	
		result = set_slot_in_test(result, bits_per_entry, 2, true, false, false, fp2);
		result = set_slot_in_test(result, bits_per_entry, 4, true, false, false, fp3);
		check_equality(qf, result, true);
		qf.pretty_print();
	}
	
	static public  void main(String[] args) {
		test1(); // example from wikipedia
		test2(); // example from quotient filter paper
		test3(); // ensuring no false negatives
		test4(); // overflow test
		test5(); // deletion test 
		
		experiment_false_positives();
		experiment_insertion_speed();
	}

}





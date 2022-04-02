import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Random;

public class QuotientFilter {

	int bitPerEntry;
	int fingerprintLength; 
	int power_of_two_size;
	BitSet filter;
	
	QuotientFilter(int power_of_two, int bits_per_entry) {
		// add assertion that init_size is a power of 2
		power_of_two_size = power_of_two;
		bitPerEntry = bits_per_entry; 
		fingerprintLength = bits_per_entry - 3;
		int init_size = 1 << power_of_two;
		filter = new BitSet(bits_per_entry * init_size);
	}
	
	public int size() {
		return filter.size() / bitPerEntry;
	}
	
	void modify_slot(boolean is_occupied, boolean is_continuation, boolean is_shifted, 
			int index) {
		set_occupied(index, is_occupied);
		set_continuation(index, is_continuation);
		set_shifted(index, is_shifted);
	}
	
	void set_fingerprint(int index, BitSet fingerprint) {
		for (int i = index * bitPerEntry + 3, j = 0; i < index * bitPerEntry + 3 + fingerprintLength; i++, j++) {
			filter.set(i, fingerprint.get(j));
		}
	}
	
	BitSet get_fingerprint(int index) {
		BitSet fingerprint = new BitSet(fingerprintLength);
		for (int i = index * bitPerEntry + 3, j = 0; i < index * bitPerEntry + 3 + fingerprintLength; i++, j++) {
			fingerprint.set(j, filter.get(i));
		}
		return fingerprint;
	}
	

	
	boolean compare(int index, BitSet fingerprint) {
		for (int i = index * bitPerEntry + 3, j = 0; i < index * bitPerEntry + 3 + fingerprintLength; i++, j++) {
			if (filter.get(i) != fingerprint.get(j)) {
				return false;
			}
		}
		return true; 
	}
	
	void modify_slot(boolean is_occupied, boolean is_continuation, boolean is_shifted, 
			int index, BitSet fingerprint) {
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
	
	public void pretty_print() {	
		for (int i = 0; i < filter.size(); i++) {
			int remainder = i % bitPerEntry;
			if (remainder == 0) {
				System.out.print(" ");
			}
			if (remainder == 3) {
				System.out.print(" ");
			}
			System.out.print(filter.get(i) ? "1" : "0");
		}
		System.out.println();
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
	
	boolean scan_run(int index, BitSet fingerprint) {
		assert(!is_continuation(index));
		do {
			if (compare(index, fingerprint)) {
				//System.out.println("found matching FP at index " + index);
				return true; 
			}
			index++;
		} while (is_continuation(index));
		return false; 
	}
	
	boolean search(BitSet fingerprint, int index) {
		boolean does_run_exist = is_occupied(index);
		if (!does_run_exist) {
			return false;
		}
		int run_start_index = find_run_start(index);
		boolean found_fingerprint = scan_run(run_start_index, fingerprint);
		return found_fingerprint;
	}
	
	BitSet swap_fingerprints(int index, BitSet new_fingerprint) {
		BitSet existing = get_fingerprint(index);
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
	
	void insert_new_run(int index, BitSet fingerprint) {
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
			set_fingerprint(start_of_this_new_run, fingerprint);
			return; 
		}
		
		int current_index = start_of_this_new_run;
		boolean is_this_slot_empty;
		boolean finished_first_run = false;
		boolean temp_continuation = false;
		
		do {
			is_this_slot_empty = is_slot_empty(current_index);
			fingerprint = swap_fingerprints(current_index, fingerprint);

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
		//print_important_bits();
		
	}
	
	void insert(BitSet fingerprint, int index, boolean insert_only_if_no_match) {
		boolean does_run_exist = is_occupied(index);
		if (!does_run_exist) {
			insert_new_run(index, fingerprint);
			return;
		}
		
		int run_start_index = find_run_start(index);
		if (does_run_exist && insert_only_if_no_match) {
			boolean found_fingerprint = scan_run(run_start_index, fingerprint);
			if (found_fingerprint) {
				return; 
			}
		}
		
		int current_index = run_start_index;
		boolean is_this_slot_empty;
		boolean finished_first_run = false;
		boolean temp_continuation = false;
		
		do {
			is_this_slot_empty = is_slot_empty(current_index);
			fingerprint = swap_fingerprints(current_index, fingerprint);
		

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
			//is_new_run = !is_continuation(current_index);
		} while (!is_this_slot_empty);
		
	
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
	
	String get_fingerprint_str(BitSet fp, int length) {
		String str = "";
		for (int i = 0; i < length; i++) {
			str += fp.get(i) ? "1" : "0";
		}
		return str;
	}
	
	int get_slot_index(int large_hash) {
		int slot_index_mask = (1 << power_of_two_size) - 1;
		int slot_index = large_hash & slot_index_mask;
		return slot_index;
	}
	
	BitSet gen_fingerprint(int large_hash) {
		int fingerprint_mask = (1 << fingerprintLength) - 1;
		fingerprint_mask = fingerprint_mask << power_of_two_size;
		int fingerprint = (large_hash & fingerprint_mask) >> power_of_two_size;
		BitSet fingerprint_bs = new BitSet(fingerprintLength);
		for (int i = 0; i < fingerprintLength; i++) {
			int mask = (int)Math.pow(2, i);
			int masked = fingerprint & mask;
			boolean val = masked > 0;
			fingerprint_bs.set(i, val);
			//System.out.println(val);
		}
		return fingerprint_bs;
	}
	
	void insert(int input, boolean insert_only_if_no_match) {
		int large_hash = HashFunctions.normal_hash(input);
		int slot_index = get_slot_index(large_hash);
		BitSet fingerprint = gen_fingerprint(large_hash);
		
		//System.out.println(input + "\t" + slot_index + "\t" + get_fingerprint_str(fingerprint, fingerprintLength));
		/*print_int_in_binary(input, 31);
		print_int_in_binary(large_hash, 31);
		print_int_in_binary(slot_index_mask, 31);
		print_int_in_binary(slot_index, 31);
		print_int_in_binary(fingerprint_mask, 31);
		print_int_in_binary(fingerprint, 31);
		System.out.println(slot_index);*/
		
		insert(fingerprint, slot_index, false);
	}
	
	boolean search(int input) {
		int large_hash = HashFunctions.normal_hash(input);
		int slot_index = get_slot_index(large_hash);
		BitSet fingerprint = gen_fingerprint(large_hash);
		return search(fingerprint, slot_index);
	}
	
	public boolean get_bit_at_offset(int offset) {
		return filter.get(offset);
	}
	
	static public boolean check_equality(QuotientFilter qf, BitSet bs) {
		for (int i = 0; i < bs.size(); i++) {
			if (i % qf.bitPerEntry == 0 || i % qf.bitPerEntry == 1 || i % qf.bitPerEntry == 2) {
				if (qf.get_bit_at_offset(i) != bs.get(i)) {
					System.out.println("failed test!!!!!");
					System.exit(1);
				}
			}
		}
		return true;
	}
	
	static public void read_test() {
		QuotientFilter qf = new QuotientFilter(8, 8);
		
		BitSet f = new BitSet(5);
		f.set(0, 5, true);
		
		qf.modify_slot(false, false, false, 0); 
		qf.modify_slot(true, false, false, 1); 
		qf.modify_slot(true, true, true, 2); 
		qf.modify_slot(false, true, true, 3);
		qf.modify_slot(true, false, true, 4, f); 
		qf.modify_slot(false, false, true, 5); 
		qf.modify_slot(false, true, true, 6, f); 
		qf.modify_slot(true, false, false, 7); 
		
		
		qf.print(); 
		qf.print_important_bits();
		
		qf.search(f, 4);
	}
	
	// This test is based on the example from https://en.wikipedia.org/wiki/Quotient_filter
	// it performs the same insertions and query as the example and verifies that it gets the same results. 
	static public void test1() {
		int bits_per_entry = 8;
		int num_entries_power = 3;
		int num_entries = (int)Math.pow(2, num_entries_power);
		int fingerprint_size = bits_per_entry - 3;
		QuotientFilter qf = new QuotientFilter(num_entries_power, bits_per_entry);

		BitSet fingerprint0 = new BitSet(5);
		fingerprint0.set(0, fingerprint_size, false);
		
		BitSet fingerprint1 = new BitSet(5);
		fingerprint1.set(0, fingerprint_size, true);
	
		qf.insert(fingerprint0, 1, false);
		qf.insert(fingerprint1, 4, false);
		qf.insert(fingerprint0, 7, false);
		//qf.print_important_bits();
		qf.insert(fingerprint0, 1, false);
		//qf.print_important_bits();
		qf.insert(fingerprint0, 2, false);
		//qf.print_important_bits();
		qf.insert(fingerprint0, 1, false);
		//qf.print_important_bits();
		
		//qf.pretty_print();
		//qf.print();
		
		// these are the expecting resulting is_occupied, is_continuation, and is_shifted bits 
		// for all slots contigously. We do not store the fingerprints here
		BitSet result = new BitSet(num_entries * bits_per_entry);
		int index = 0;
		result.set(index++, false); result.set(index++, false); result.set(index++, false); index += bits_per_entry - 3;	
		result.set(index++, true); result.set(index++, false); result.set(index++, false); index += bits_per_entry - 3;
		result.set(index++, true); result.set(index++, true); result.set(index++, true); index += bits_per_entry - 3;
		result.set(index++, false); result.set(index++, true); result.set(index++, true); index += bits_per_entry - 3;
		result.set(index++, true); result.set(index++, false); result.set(index++, true); index += bits_per_entry - 3;
		result.set(index++, false); result.set(index++, false); result.set(index++, true); index += bits_per_entry - 3;
		result.set(index++, false); result.set(index++, false); result.set(index++, false); index += bits_per_entry - 3;
		result.set(index++, true); result.set(index++, false); result.set(index++, false); index += bits_per_entry - 3;
	
		check_equality(qf, result);
		
		int expected_slot = 6 ;
		int starting_offset = bits_per_entry * expected_slot + 3;
		for (int i = starting_offset; i < starting_offset + fingerprint_size; i++) {
			if (qf.get_bit_at_offset(i) != fingerprint1.get(i)) {
				System.out.println("failed test!!!!!");
				System.exit(1);
			}
		}
	}
	
	
	// This test is based on the example from the quotient filter paper 
	// it performs the same insertions as in Figure 2 and checks for the same result
	static public void test2() {
		int bits_per_entry = 8;
		int num_entries_power = 4;
		int num_entries = (int)Math.pow(2, num_entries_power);
		int fingerprint_size = bits_per_entry - 3;
		QuotientFilter qf = new QuotientFilter(4, 8);
		BitSet ones = new BitSet(fingerprint_size);
		ones.set(0, fingerprint_size, true);
		BitSet zeros = new BitSet(fingerprint_size);
		zeros.set(0, fingerprint_size, false);
		
		qf.insert(zeros, 1, false);
		qf.insert(zeros, 1, false);
		qf.insert(zeros, 3, false);
		qf.insert(zeros, 3, false);
		qf.insert(zeros, 3, false);
		qf.insert(zeros, 4, false);
		qf.insert(zeros, 6, false);
		qf.insert(zeros, 6, false);
		
		//qf.print_important_bits();
		//qf.print();
		
		BitSet result = new BitSet(num_entries * bits_per_entry);
		int index = 0;
		result.set(index++, false); result.set(index++, false); result.set(index++, false); index += bits_per_entry - 3;
		result.set(index++, true); result.set(index++, false); result.set(index++, false); index += bits_per_entry - 3;
		result.set(index++, false); result.set(index++, true); result.set(index++, true); index += bits_per_entry - 3;
		result.set(index++, true); result.set(index++, false); result.set(index++, false); index += bits_per_entry - 3;
		result.set(index++, true); result.set(index++, true); result.set(index++, true); index += bits_per_entry - 3;	
		result.set(index++, false); result.set(index++, true); result.set(index++, true); index += bits_per_entry - 3;
		result.set(index++, true); result.set(index++, false); result.set(index++, true); index += bits_per_entry - 3;
		result.set(index++, false); result.set(index++, false); result.set(index++, true); index += bits_per_entry - 3;
		result.set(index++, false); result.set(index++, true); result.set(index++, true); index += bits_per_entry - 3;
		result.set(index++, false); result.set(index++, false); result.set(index++, false); index += bits_per_entry - 3;	
		check_equality(qf, result);
	}
	
	// Here we create a large(ish) filter, insert some random entries into it, and then make sure 
	// we get (true) positives for all entries we had inserted. 
	// This is to verify we do not get any false negatives. 
	// We then also check the false positive rate 
	static public void test3() {
		int bits_per_entry = 8;
		int num_entries_power = 10;
		//int num_entries = (int)Math.pow(2, num_entries_power);
		int fingerprint_size = bits_per_entry - 3;
		QuotientFilter qf = new QuotientFilter(num_entries_power, bits_per_entry);
		HashSet<Integer> added = new HashSet<Integer>();
		Random rand = new Random(5);
		for (int i = 0; i < qf.size() * 0.9; i++) {
			int rand_num = rand.nextInt();
			added.add(rand_num);
			qf.insert(rand_num, false);
		}
		qf.print_important_bits();
		qf.pretty_print();
		
		for (Integer i : added) {
			//System.out.println("searching  " + i );
			boolean found = qf.search(i);
			if (!found) {
				System.out.println("something went wrong!! seem to have false negative");
				qf.search(i);
				System.exit(1);
			}
		}
		
		int num_queries = 20000;
		int num_false_positives = 0;
		for (int i = 0; i < num_queries; i++) {
			int rand_num = rand.nextInt();
			if (!added.contains(rand_num)) {
				//System.out.println("query for non-existing key " + rand_num );
				boolean found = qf.search(i);
				if (found) {
					//System.out.println("we seem to have a false positive");
					num_false_positives++;
				}
			}
		}
		double FPR = num_false_positives / (double)num_queries;
		System.out.println("FPR:\t" + FPR);
		double expected_FPR = Math.pow(2, - fingerprint_size);
		System.out.println("FPR:\t" + expected_FPR);
	}
	
	static public  void main(String[] args) {
		test1();
		test2();
		test3();
		
		//write_test3();
		
	}

}





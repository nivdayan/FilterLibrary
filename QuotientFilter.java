import java.util.BitSet;

public class QuotientFilter {

	int bitPerEntry;
	int fingerprintLength; 
	BitSet filter;
	
	QuotientFilter(int init_size, int bits_per_entry) {
		bitPerEntry = bits_per_entry; 
		fingerprintLength = bits_per_entry - 3;
		filter = new BitSet(bits_per_entry * init_size);
		
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
	
	
	void print() {	
		for (int i = 0; i < filter.size(); i++) {
			System.out.print(filter.get(i) ? "1" : "0");
		}
		System.out.println();
	}
	
	void print_important_bits() {	
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
		int num_runs = 0;
		while (is_shifted(current_index)) {
			if (is_occupied(current_index)) {
				if (num_runs == 0) {
					num_runs += 2;
				}
				else {
					num_runs++;
				}
				
			}
			current_index--;
			//System.out.println("current_index  " + current_index);
		}
		
		while (num_runs > 0) {
			if (!is_continuation(current_index)) {
				num_runs--;
				if (num_runs == 0) {
					break;
				}
			}
			current_index++;
			//System.out.println("current_index  " + current_index);
		}
		//System.out.println("current_index  " + current_index);
		return current_index;
	}
	
	boolean scan_run(int index, BitSet fingerprint) {
		assert(!is_continuation(index));
		do {
			if (compare(index, fingerprint)) {
				System.out.println("found matching FP at index " + index);
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
	
	void insert_new_run(int index, BitSet fingerprint) {
		int first_empty_slot = find_first_empty_slot(index);
		set_fingerprint(first_empty_slot, fingerprint);
		if (first_empty_slot != index) {
			set_shifted(first_empty_slot, true);
		}
		set_continuation(first_empty_slot, false);
		set_occupied(index, true);
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
	
	static public void write_test1() {
		QuotientFilter qf = new QuotientFilter(8, 8);

		BitSet f = new BitSet(5);
		f.set(0, 5, true);
		
		qf.insert(f, 1, false);
		qf.insert(f, 4, false);
		qf.insert(f, 7, false);
		
		qf.print_important_bits();
		
		qf.insert(f, 1, false);
		
		qf.print_important_bits();
		
		qf.insert(f, 2, false);
			
		qf.print_important_bits();
		
		qf.insert(f, 1, false);
		
		qf.print_important_bits();
	}
	
	static public void write_test2() {
		QuotientFilter qf = new QuotientFilter(16, 8);

		qf.print_important_bits();
		qf.print();
		
		BitSet ones = new BitSet(5);
		ones.set(0, 5, true);
		BitSet zeros = new BitSet(5);
		zeros.set(0, 5, false);
		
		
		qf.insert(zeros, 1, false);
		qf.insert(zeros, 1, false);
		qf.insert(ones, 3, false);
		qf.insert(ones, 3, false);
		qf.insert(ones, 3, false);
		qf.insert(ones, 4, false);
		qf.insert(ones, 4, false);
		qf.insert(zeros, 6, false);
		qf.insert(zeros, 6, false);
		
		qf.print_important_bits();
		qf.print();
		
		int run_start_index = qf.find_run_start(1);
		System.out.println("1 " + run_start_index);
		System.out.println("2 " + qf.find_run_start(2));
		System.out.println("3 " + qf.find_run_start(3));
		System.out.println("4 " + qf.find_run_start(4));
		System.out.println("5 " + qf.find_run_start(5));
		System.out.println("6 " + qf.find_run_start(6));

		
		boolean found = qf.search(ones, 5);
		if (!found) {
			System.out.println("not found ");
		}
	}
	
	static public  void main(String[] args) {
		
		write_test2();
		
	}

}





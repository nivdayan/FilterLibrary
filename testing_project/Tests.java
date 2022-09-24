package testing_project;

import java.util.BitSet;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import bitmap_implementations.Bitmap;

public class Tests {

	static public BitSet set_slot_in_test(BitSet result, int bits_per_entry, int slot, boolean is_occupied, boolean is_continuation, boolean is_shifted, long fingerprint) {
		int index = bits_per_entry * slot;
		result.set(index++, is_occupied); 
		result.set(index++, is_continuation); 
		result.set(index++, is_shifted); 
		for (int i = 0; i < bits_per_entry - 3; i++) {
			result.set(index++, Bitmap.get_fingerprint_bit(i, fingerprint) );
		}
		return result;
	}
	

	static public BitSet set_slot_in_test(BitSet result, int bits_per_entry, int slot, boolean is_occupied, boolean is_continuation, boolean is_shifted, String fingerprint) {
		long l_fingerprint = 0;
		for (int i = 0; i < fingerprint.length(); i++) {
			char c = fingerprint.charAt(i);
			if (c == '1') {
				l_fingerprint |= (1 << i);
			}
		}

		return set_slot_in_test(result, bits_per_entry, slot, is_occupied, is_continuation, is_shifted, l_fingerprint);
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
		qf.insert(fingerprint0, 1, false);
		qf.insert(fingerprint0, 2, false);
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
		//qf.print_filter_summary();

		check_equality(qf, result, true);

		if (qf.num_existing_entries != 6) {
			System.out.print("counter not working well");
			System.exit(1);
		}
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
		for (int i = 0; i < qf.get_logical_num_slots() * load_factor; i++) {
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

		//qf.pretty_print();

		qf.delete(fp2, num_entries - 1);
		boolean found = qf.search(fp2, num_entries - 1);
		if (!found) {
			System.out.println("Should have found the entry");
			System.exit(1);
		}
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
		long fp4 = 31;

		qf.insert(fp4, 1, false);
		qf.insert(fp1, 1, false);
		qf.insert(fp1, 1, false);
		qf.insert(fp2, 2, false);
		qf.insert(fp1, 1, false);
		qf.insert(fp1, 1, false);
		qf.insert(fp3, 4, false);


		//qf.pretty_print();
		qf.delete(31, 1);
		//qf.pretty_print();
		qf.delete(fp1, 1);
		//qf.pretty_print();
		qf.delete(fp1, 1);
		//qf.pretty_print();
		qf.delete(fp1, 1);
		//qf.pretty_print();
		qf.delete(fp1, 1);
		//qf.pretty_print();

		BitSet result = new BitSet(num_entries * bits_per_entry);	
		result = set_slot_in_test(result, bits_per_entry, 2, true, false, false, fp2);
		result = set_slot_in_test(result, bits_per_entry, 4, true, false, false, fp3);
		check_equality(qf, result, true);
		//qf.pretty_print();
	}
	
	// delete testing
	static public void test16() {
		int bits_per_entry = 8;
		int num_entries_power = 3;
		int num_entries = (int)Math.pow(2, num_entries_power);
		QuotientFilter qf = new QuotientFilter(num_entries_power, bits_per_entry);

		long fp1 = 1 << 4;
		long fp2 = 1 << 3;
		long fp3 = 1 << 2;
		long fp4 = 31;

		qf.insert(0, 1, false);
		qf.insert(0, 1, false);
		qf.insert(0, 2, false);
		qf.insert(0, 2, false);
		qf.insert(0, 3, false);
		qf.insert(0, 3, false);
		qf.insert(0, 3, false);
		qf.insert(0, 6, false);
		qf.insert(0, 6, false);
		qf.insert(0, 6, false);
		qf.insert(0, 7, false);

		//qf.pretty_print();
		//qf.delete(31, 1);
		//qf.pretty_print();
		//qf.delete(fp1, 1);
		//qf.pretty_print();
		//qf.delete(fp1, 1);
		//qf.pretty_print();
		//qf.delete(fp1, 1);
		//qf.pretty_print();
		//qf.delete(fp1, 1);
		//qf.pretty_print();
		
		//qf.pretty_print();
		qf.delete(0, 2);
		//qf.pretty_print();
		qf.delete(0, 3);
		//qf.pretty_print();
		
		BitSet result = new BitSet(num_entries * bits_per_entry);	
		result = set_slot_in_test(result, bits_per_entry, 1, true, false, false, 0);
		result = set_slot_in_test(result, bits_per_entry, 2, true, true, true, 0);
		result = set_slot_in_test(result, bits_per_entry, 3, true, false, true, 0);
		result = set_slot_in_test(result, bits_per_entry, 4, false, false, true, 0);
		result = set_slot_in_test(result, bits_per_entry, 5, false, true, true, 0);
		result = set_slot_in_test(result, bits_per_entry, 6, true, false, false, 0);
		result = set_slot_in_test(result, bits_per_entry, 7, true, true, true, 0);
		result = set_slot_in_test(result, bits_per_entry, 8, false, true, true, 0);
		result = set_slot_in_test(result, bits_per_entry, 9, false, false, true, 0);

		check_equality(qf, result, true);
		//qf.pretty_print();
	}
	
	// This is a test for deleting items. We insert many keys into one slot to create an overflow. 
	// we then remove them and check that the other keys are back to their canonical slots. 
	static public void test17() {
		int bits_per_entry = 8;
		int num_entries_power = 3;
		int num_entries = (int)Math.pow(2, num_entries_power);
		QuotientFilter qf = new QuotientFilter(num_entries_power, bits_per_entry);

		long fp1 = 1 << 4;
		long fp2 = 1 << 3;
		long fp3 = 1 << 2;
		long fp4 = 31;

		qf.insert(0, 1, false);
		qf.insert(0, 1, false);
		qf.insert(0, 2, false);
		qf.insert(0, 2, false);
		qf.insert(0, 3, false);
		qf.insert(0, 4, false);
		qf.insert(0, 4, false);
		qf.insert(0, 5, false);

		//qf.pretty_print();
		qf.delete(0, 3);
		//qf.pretty_print();
		
		BitSet result = new BitSet(num_entries * bits_per_entry);	
		result = set_slot_in_test(result, bits_per_entry, 1, true, false, false, 0);
		result = set_slot_in_test(result, bits_per_entry, 2, true, true, true, 0);
		result = set_slot_in_test(result, bits_per_entry, 3, false, false, true, 0);
		result = set_slot_in_test(result, bits_per_entry, 4, true, true, true, 0);
		result = set_slot_in_test(result, bits_per_entry, 5, true, false, true, 0);
		result = set_slot_in_test(result, bits_per_entry, 6, false, true, true, 0);
		result = set_slot_in_test(result, bits_per_entry, 7, false, false, true, 0);
		check_equality(qf, result, true);
	}
	
	// This is a test for deleting items. We insert many keys into one slot to create an overflow. 
	// we then remove them and check that the other keys are back to their canonical slots. 
	/*static public void test18() {
		int bits_per_entry = 8;
		int num_entries_power = 5;
		int num_entries = (int)Math.pow(2, num_entries_power);
		QuotientFilter qf = new QuotientFilter(num_entries_power, bits_per_entry);

		long fp1 = 1 << 4;
		long fp2 = 1 << 3;
		long fp3 = 1 << 2;
		long fp4 = 31;

		qf.insert(0, 1, false);
		qf.insert(0, 1, false);
		qf.insert(0, 2, false);
		qf.insert(0, 2, false);
		qf.insert(0, 3, false);
		qf.insert(0, 4, false);
		qf.insert(0, 4, false);
		qf.insert(0, 5, false);
		qf.insert(0, 6, false);
		qf.insert(0, 6, false);
		qf.insert(0, 7, false);
		qf.insert(0, 8, false);
		
		qf.insert(0, 10, false);
		qf.insert(0, 11, false);
		qf.insert(0, 12, false);
		
		qf.insert(0, 14, false);
		qf.insert(0, 15, false);
		
		qf.insert(0, 17, false);
		qf.insert(0, 17, false);
		qf.insert(0, 19, false);

		//qf.delete(31, 1);
		//qf.pretty_print();
		//qf.delete(fp1, 1);
		//qf.pretty_print();
		//qf.delete(fp1, 1);
		//qf.pretty_print();
		//qf.delete(fp1, 1);
		//qf.pretty_print();
		//qf.delete(fp1, 1);
		//qf.pretty_print();
		
		qf.pretty_print();
		qf.delete(0, 3);
		qf.pretty_print();
		
		BitSet result = new BitSet(num_entries * bits_per_entry);	
		result = set_slot_in_test(result, bits_per_entry, 1, true, false, false, 0);
		result = set_slot_in_test(result, bits_per_entry, 2, true, true, true, 0);
		result = set_slot_in_test(result, bits_per_entry, 3, true, false, true, 0);
		result = set_slot_in_test(result, bits_per_entry, 4, false, false, true, 0);
		result = set_slot_in_test(result, bits_per_entry, 5, false, true, true, 0);
		result = set_slot_in_test(result, bits_per_entry, 6, true, false, false, 0);
		result = set_slot_in_test(result, bits_per_entry, 7, true, true, true, 0);
		result = set_slot_in_test(result, bits_per_entry, 8, false, true, true, 0);
		result = set_slot_in_test(result, bits_per_entry, 9, false, false, true, 0);

		check_equality(qf, result, true);
		//qf.pretty_print();
	}*/

	static public void test6() {

		int bits_per_entry = 8;
		int num_entries_power = 4;
		//int num_entries = (int)Math.pow(2, num_entries_power);
		//int fingerprint_size = bits_per_entry - 3;
		QuotientFilter qf = new QuotientFilter(num_entries_power, bits_per_entry);

		qf.insert(0, 2, false);
		qf.insert(0, 3, false);
		qf.insert(0, 3, false);
		qf.insert(0, 4, false);
		qf.insert(0, 23, false); // last key in the filter
		qf.insert(0, 24, false); // outside the bounds, logical slot 14 does not exist logically, even if it might exist physically 

		Iterator it = new Iterator(qf);
		int[] arr = new int[] {2, 3, 3, 4, 23};
		int arr_index = 0;


		while (it.next()) {
			//System.out.println(it.bucket_index);
			if (arr[arr_index++] != it.bucket_index) {
				System.out.print("error in iteration");
				System.exit(1);
			}
		}

	}

	static public void test7() {

		int bits_per_entry = 8;
		int num_entries_power = 4;
		int num_entries = (int)Math.pow(2, num_entries_power);
		int fingerprint_size = bits_per_entry - 3;
		QuotientFilter qf = new QuotientFilter(num_entries_power, bits_per_entry);

		qf.insert(0, 1, false);
		qf.insert(0, 4, false);
		qf.insert(0, 7, false);
		//qf.pretty_print();
		qf.insert(0, 1, false);
		qf.insert(0, 2, false);
		//qf.pretty_print();
		qf.insert(0, 1, false);
		qf.insert(0, 15, false);

		Iterator it = new Iterator(qf);
		int[] arr = new int[] {1, 1, 1, 2, 4, 7, 15};
		int arr_index = 0;


		//qf.pretty_print();
		while (it.next()) {
			//System.out.println(it.bucket_index);
			if (arr[arr_index++] != it.bucket_index) {
				System.out.print("error in iteration");
				System.exit(1);
			}
		}
	}

	// In this test, we create one FingerprintShrinkingQF and expand it once.
	// We also create an expanded Quotient Filter with the same data from the onset and make sure they are logically equivalent. 
	static public void test8() {

		int bits_per_entry = 10;
		int num_entries_power = 4;
		BitSacrificer qf = new BitSacrificer(num_entries_power, bits_per_entry);
		qf.max_entries_before_expansion = Integer.MAX_VALUE; // disable automatic expansion
		//qf.print_key(1);

		for (int i = 0; i < 12; i++) {
			qf.insert(i, false);
		}

		//qf.pretty_print();
		qf.expand();
		//qf.pretty_print();

		QuotientFilter qf2 = new QuotientFilter(num_entries_power + 1, bits_per_entry - 1);

		for (int i = 0; i < 12; i++) {
			qf2.insert(i, false);
		}

		//qf2.pretty_print();

		if (qf.filter.size() != qf2.filter.size()) {
			System.out.print("filters have different sizes");
			System.exit(1);
		}

		for (int i = 0; i < qf.get_logical_num_slots(); i++) {
			Set<Long> set1 = qf.get_all_fingerprints(i);
			Set<Long> set2 = qf2.get_all_fingerprints(i);

			if (!set1.equals(set2)) {
				System.out.print("fingerprints for bucket " + i + " not identical");
				System.exit(1);
			}
		}
	}

	// insert entries across two phases of expansion, and then check we can still find all of them
	static public void test9() {

		int bits_per_entry = 10;
		int num_entries_power = 3;
		MultiplyingQF qf = new MultiplyingQF(num_entries_power, bits_per_entry);
		qf.max_entries_before_expansion = Integer.MAX_VALUE; // disable automatic expansion

		int i = 0;
		while (i < Math.pow(2, num_entries_power) - 2) {
			qf.insert(i, false);
			i++;
		}
		qf.expand();
		while (i < Math.pow(2, num_entries_power + 1) - 2) {
			qf.insert(i, false);
			i++;
		}

		for (int j = 0; j < i; j++) {
			if ( !qf.search(j) ) {
				System.out.println("false negative  " + j);
				System.exit(1);
			}
		}

	}

	static public void test10() {
		int bits_per_entry = 10;
		int num_entries_power = 3;		
		InfiniFilter qf = new InfiniFilter(num_entries_power, bits_per_entry);

		int i = 1;
		while (i < Math.pow(2, num_entries_power) - 1) {
			qf.insert(i, false);
			i++;
		}

		//qf.pretty_print();
		qf.expand();
		//qf.pretty_print();


		int num_entries = 1 << ++num_entries_power;
		BitSet result = new BitSet(num_entries * bits_per_entry);		
		result = set_slot_in_test(result, bits_per_entry, 0, false, false, false, "0000000");
		result = set_slot_in_test(result, bits_per_entry, 1, true, false, false, "1100101");
		result = set_slot_in_test(result, bits_per_entry, 2, true, false, false, "1010101");
		result = set_slot_in_test(result, bits_per_entry, 3, false, false, false, "0000000");
		result = set_slot_in_test(result, bits_per_entry, 4, false, false, false, "0000000");
		result = set_slot_in_test(result, bits_per_entry, 5, true, false, false, "0010001");
		result = set_slot_in_test(result, bits_per_entry, 6, false, false, false, "0000000");
		result = set_slot_in_test(result, bits_per_entry, 7, true, false, false, "0101101");
		result = set_slot_in_test(result, bits_per_entry, 8, true, false, false, "1001001");
		result = set_slot_in_test(result, bits_per_entry, 9, false, true, true, "0111001");
		check_equality(qf, result, true);

		i = 1;
		while (i < Math.pow(2, num_entries_power - 1) - 1) {
			boolean found = qf.search(i);
			//qf.compare(0, 0);
			if (!found) {
				System.out.println("not found entry " + i);
				System.exit(1);
			}
			i++;
		}
	}

	// this test ensures we issue enough insertions until the fingerprints of at least some of the first entries inserted 
	// run out. This means that for these entries, we are going to try the double insertion technique to avoid false negatives. 
	static public void test11() {
		int bits_per_entry = 7;
		int num_entries_power = 3;		
		InfiniFilter qf = new InfiniFilter(num_entries_power, bits_per_entry);
		qf.expand_autonomously = true;
		int max_key = (int)Math.pow(2, num_entries_power + qf.fingerprintLength + 1);
		for (int i = 0; i < max_key; i++) {
			qf.insert(i, false);
		}

		for (int i = 0; i < 100; i++) {
			boolean found = qf.search(i);
			if (!found) {
				System.out.println("not found entry " + i);
				System.exit(1);
			}
		}

		int false_positives = 0;
		for (int i = max_key; i < max_key + 10000; i++) {
			boolean found = qf.search(i);
			if (found) {
				false_positives++;
			}
		}
		if (false_positives == 0) {
			System.out.println("should have had a few false positives");
			System.exit(1);
		}
		//qf.pretty_print();
	}

	// this test ensures we issue enough insertions until the fingerprints of at least some of the first entries inserted 
	// run out. This means that for these entries, we are going to try the double insertion technique to avoid false negatives. 
	static public void test12() {
		int bits_per_entry = 7;
		int num_entries_power = 3;		
		ChainedInfiniFilter qf = new ChainedInfiniFilter(num_entries_power, bits_per_entry);
		qf.expand_autonomously = true;
		int max_key = (int)Math.pow(2, num_entries_power + qf.fingerprintLength * 2 + 1 );
		for (int i = 0; i < max_key; i++) {
			qf.insert(i, false);
		}

		for (int i = 0; i < max_key; i++) {
			boolean found = qf.search(i);
			if (!found) {
				System.out.println("not found entry " + i);
				System.exit(1);
			}
		}

		int false_positives = 0;
		for (int i = max_key; i < max_key + 10000; i++) {
			boolean found = qf.search(i);
			if (found) {
				false_positives++;
			}
		}
		if (false_positives == 0) {
			System.out.println("should have had a few false positives");
			System.exit(1);
		}
		//qf.pretty_print();
	}

	// here we test the rejuvenation operation of InfiniFilter
	static public void test13() {
		int bits_per_entry = 7;
		int num_entries_power = 2;		
		InfiniFilter qf = new InfiniFilter(num_entries_power, bits_per_entry);
		qf.expand_autonomously = false;

		qf.insert(2, false);
		//qf.pretty_print();
		qf.expand();
		//qf.pretty_print();
		qf.rejuvenate(2);
		//qf.pretty_print();

		BitSet result = new BitSet(qf.get_logical_num_slots() * bits_per_entry);		
		result = set_slot_in_test(result, bits_per_entry, 0, true, false, false, 3);

		check_equality(qf, result, true);
	}
	
	
	// Testing the capability of InfiniFilter to delete the longest matching fingerprint
	static public void test14() {
		int bits_per_entry = 8;
		int num_entries_power = 2;
		int num_entries = (int)Math.pow(2, num_entries_power);
		int fingerprint_size = bits_per_entry - 3;
		InfiniFilter qf = new InfiniFilter(num_entries_power, bits_per_entry);
		
		int fp1 = 1;
		int fp2 = 2;
		int fp3 = 0;
		
		qf.insert(fp1, 1, false);
		
		qf.expand();
		
		qf.insert(fp3, 5, false);
		
		qf.insert(fp2, 5, false);

		//qf.pretty_print();
		
		qf.delete(fp3, 5);  // we must delete the longest matching fingerprint, o
		//qf.pretty_print();


		BitSet result = new BitSet(num_entries * bits_per_entry);	
		
		result = set_slot_in_test(result, bits_per_entry, 5, true, false, false, 16);
		result = set_slot_in_test(result, bits_per_entry, 6, false, true, true, fp2);
		check_equality(qf, result, true);
		//qf.pretty_print();
	}
	
	
	// Here we're going to create a largish filter, and then perform deletes and insertions
	// we want to make sure we indeed get a positive for every entry that we inserted and still not deleted
	// for every 2 insertions, we make one deletes, in order to still allow the filter to expand 
	static public void test15() {
		int bits_per_entry = 10;
		int num_entries_power = 3;
		int seed = 2;  // 10
		//int num_entries = (int)Math.pow(2, num_entries_power);
		InfiniFilter qf = new ChainedInfiniFilter(num_entries_power, bits_per_entry);
		qf.expand_autonomously = true;
		TreeSet<Integer> added = new TreeSet<Integer>();
		Random rand = new Random(seed);
		double num_entries_to_insert = Math.pow(2, num_entries_power + 10); // we'll expand 3-4 times
		
		for (int i = 0; i < num_entries_to_insert; i++) {
			int rand_num = rand.nextInt();
			if (!added.contains(rand_num)) {
				boolean success = qf.insert(rand_num, false);
				if (success) {
					added.add(rand_num);
					boolean found = qf.search(rand_num);
					if (!found) {
						System.out.println("failed on key " + rand_num);
						qf.pretty_print();
						System.exit(1);
					}
					
				}
			}
			
			if (i % 4 == 0 && i > Math.pow(2, num_entries_power)) {
					int to_del = rand.nextInt();
					if (to_del > added.first()) {
					int r = added.floor(to_del);
					added.remove(r);
					
					boolean deleted = true;
						deleted = qf.delete(r);
					if (!deleted) {
						System.out.println("not deleted");
						System.exit(1);
					}
				}
			}
			
			int key = rand.nextInt();
			if (key > added.first()) {
				int to_query = added.floor(key);
				boolean found = qf.search(to_query);
				if (!found) {
					System.out.println("failed on key " + to_query);
					qf.pretty_print();
					System.exit(1);
				}
			}
		}

		for (Integer i : added) {
			boolean found = qf.search(i);
			if (!found) {
				System.out.println("something went wrong!! seem to have false negative " + i);
				qf.search(i);
				System.exit(1);
			}
		}
	}
	

	// Here we're going to create a largish filter, and then perform insertions and rejuvenation operations
	// we'll test correctness by ensuring all keys we have inserted indeed still give positives
	static public void test18() {
		int bits_per_entry = 10;
		int num_entries_power = 3;
		int seed = 5;  // 10
		//int num_entries = (int)Math.pow(2, num_entries_power);
		InfiniFilter qf = new ChainedInfiniFilter(num_entries_power, bits_per_entry);
		qf.expand_autonomously = true;
		TreeSet<Integer> added = new TreeSet<Integer>();
		Random rand = new Random(seed);
		double num_entries_to_insert = Math.pow(2, num_entries_power + 10); // we'll expand 3-4 times

		for (int i = 0; i < num_entries_to_insert; i++) {
			int rand_num = rand.nextInt();
			if (!added.contains(rand_num)) {
				boolean success = qf.insert(rand_num, false);
				if (success) {
					added.add(rand_num);
					boolean found = qf.search(rand_num);
					if (!found) {
						System.out.println("failed on key " + rand_num);
						qf.pretty_print();
						System.exit(1);
					}

				}
			}

			if (i % 4 == 0 && i > Math.pow(2, num_entries_power)) {
				int to_del = rand.nextInt();
				if (to_del > added.first()) {
					int r = added.floor(to_del);
					added.remove(r);

					boolean deleted =  qf.delete(r);
					if (!deleted) {
						System.out.println("not deleted");
						System.exit(1);
					}
				}
			}

			if (i % 2 == 0 && i > Math.pow(2, num_entries_power)) {
				int to_rejuv = rand.nextInt();
				if (to_rejuv > added.first()) {
					int key = added.floor(to_rejuv);

					boolean rejuved = qf.rejuvenate(key);
					if (!rejuved) {
						System.out.println("not rejuvenated");
						System.exit(1);
					}
				}
			}

			int key = rand.nextInt();
			if (key > added.first()) {
				int to_query = added.floor(key);
				boolean found = qf.search(to_query);
				if (!found) {
					System.out.println("failed on key " + to_query);
					qf.pretty_print();
					System.exit(1);
				}
			}
		}

		for (Integer i : added) {
			boolean found = qf.search(i);
			if (!found) {
				System.out.println("something went wrong!! seem to have false negative " + i);
				qf.search(i);
				System.exit(1);
			}
		}
	}

}

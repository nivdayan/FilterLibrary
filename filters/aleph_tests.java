package filters;

import java.util.ArrayList;

public class aleph_tests {

	// this test ensures we issue enough insertions until the fingerprints of at least some of the first entries inserted 
	// run out. This means that for these entries, we are going to try the double insertion technique to avoid false negatives. 
	static public void test11() {
		int bits_per_entry = 7;
		int num_entries_power = 3;		
		BasicInfiniFilter qf = new AlephFilter(num_entries_power, bits_per_entry);
		qf.expand_autonomously = true;
		int max_key = (int)Math.pow(2, num_entries_power + qf.fingerprintLength + 1);
		//int max_key = (int)Math.pow(2, num_entries_power + 4 );
		for (int i = 0; i < max_key; i++) {			
			qf.insert(i, false);
		}

		for (int i = 0; i < max_key; i++) {
			boolean found = qf.search(i);
			if (!found) {
				System.out.println("not found entry " + i + " in test11");
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
	
	static void Assert(boolean condition) {
		if (!condition) {
			System.out.println("assertion failed");
			System.exit(1);
		}
	}
	
	static public void test1() {
		aleph_deletion_map_creator creator = new aleph_deletion_map_creator(8);
		
		boolean success = false;
		success = creator.start_new_slot(2, 2);
		Assert(success);
		success = creator.insert_age(1);
		Assert(success);
		success = creator.insert_age(2);
		Assert(success);
		success = creator.insert_age(2);
		Assert(!success);
		
		success = creator.start_new_slot(6, 3);
		Assert(success);
		success = creator.insert_age(1);
		Assert(success);
		success = creator.insert_age(2);
		Assert(success);
		success = creator.insert_age(3);
		Assert(success);
		//creator.insert(4, 2);
		success = creator.insert_age(4);
		Assert(!success);
		//creator.print();
		
		AlephDeletionMap m = creator.generate();
		aleph_deletion_map_iterator it = new aleph_deletion_map_iterator(m);
		
		Assert(it.current_slot == 2 && it.current_age == 1);
		it.next();
		Assert(it.current_slot == 2 && it.current_age == 2);		
		it.next();
		Assert(it.current_slot == 6 && it.current_age == 1);	
		it.next();
		Assert(it.current_slot == 6 && it.current_age == 2);	
		it.next();
		Assert(it.current_slot == 6 && it.current_age == 3);	
		it.next();
		Assert(it.done());
		
		ArrayList<Long> new_void_slot_locations_first_half = new ArrayList<Long>();
		new_void_slot_locations_first_half.add(4L);
		new_void_slot_locations_first_half.add(4L);
		new_void_slot_locations_first_half.add(6L);
		
		ArrayList<Long> new_void_slot_locations_second_half = new ArrayList<Long>();
		new_void_slot_locations_second_half.add(9L);
		new_void_slot_locations_second_half.add(12L);
		new_void_slot_locations_second_half.add(15L);
		
		aleph_deletion_map_creator creator2 = new aleph_deletion_map_creator(33);
		
		AlephFilter.expand_half(m, new_void_slot_locations_first_half, 8, creator2, true, 4);
		AlephFilter.expand_half(m, new_void_slot_locations_second_half, 8, creator2, false, 4);
		
		/*it.reset();
		do {
			it.print_status();
		} while ( it.next() );*/
		
		//creator2.print();
		
		AlephDeletionMap m2 = creator2.generate();
		aleph_deletion_map_iterator it2 = new aleph_deletion_map_iterator(m2);
		
		/*do {
			it2.print_status();
		} while ( it2.next() );
		it2.reset();*/
		
		Assert(it2.current_slot == 2 && it2.current_age == 2);	
		it2.next();
		Assert(it2.current_slot == 2 && it2.current_age == 3);	
		it2.next();
		Assert(it2.current_slot == 4 && it2.current_age == 1);	
		it2.next();
		Assert(it2.current_slot == 4 && it2.current_age == 1);	
		it2.next();
		Assert(it2.current_slot == 6 && it2.current_age == 1);	
		it2.next();
		Assert(it2.current_slot == 6 && it2.current_age == 2);	
		it2.next();
		Assert(it2.current_slot == 6 && it2.current_age == 3);	
		it2.next();
		Assert(it2.current_slot == 6 && it2.current_age == 4);	
		it2.next();
		Assert(it2.current_slot == 9 && it2.current_age == 1);	
		it2.next();
		Assert(it2.current_slot == 10 && it2.current_age == 2);	
		it2.next();
		Assert(it2.current_slot == 10 && it2.current_age == 3);	
		it2.next();
		Assert(it2.current_slot == 12 && it2.current_age == 1);	
		it2.next();
		Assert(it2.current_slot == 14 && it2.current_age == 2);	
		it2.next();
		Assert(it2.current_slot == 14 && it2.current_age == 3);	
		it2.next();
		Assert(it2.current_slot == 14 && it2.current_age == 4);	
		it2.next();
		Assert(it2.current_slot == 15 && it2.current_age == 1);	
		it2.next();

		
		//AlephFilter.expand_half(m, new_void_slot_locations, 8, creator2, true, 3);

		//AlephDeletionMap m2 = AlephFilter.expand_deletion_map(m, new_void_slot_locations, m.map.size() * 2, );
		//m2.print();
		
		/*aleph_deletion_map_iterator it2 = new aleph_deletion_map_iterator(m2);*/
	}
	
	static public void test2() {
		int bits_per_entry = 10;
		int num_entries_power = 3;	
		AlephFilter qf = new AlephFilter(num_entries_power, bits_per_entry);
		qf.expand_autonomously = true;
		qf.fprStyle = FingerprintGrowthStrategy.FalsePositiveRateExpansion.UNIFORM;
		int max_key = (int)Math.pow(2, num_entries_power + qf.fingerprintLength + 0);
		//int key_range_offset_factor = 0;
		for (int i = 0; i < max_key; i++) {		
			//System.out.println(i);
			if (i == 184) {
				//qf.pretty_print();
				//System.out.println();
			}
			boolean success = qf.insert(i, false);
			Assert(success);
		}
		//qf.pretty_print();
		//qf.deletion_map.print();
		aleph_deletion_map_iterator it2 = new aleph_deletion_map_iterator(qf.deletion_map);
		/*do {
			it2.print_status();
		} while (it2.next());*/
		
		if (qf.deletion_map != null) {
			qf.deletion_map.print_age_histogram();
		}
		if (qf.deletion_map != null) {
			qf.deletion_map.print_slot_histogram();
		}
	}
	
	static public void insert_all_and_then_delete_all(DuplicatingChainedInfiniFilter qf) {

		int max_key = (int)Math.pow(2, qf.power_of_two_size + qf.fingerprintLength + 1);
		for (int i = 0; i < max_key; i++) {		
			boolean success = qf.insert(i, false);
			Assert(success);
		}

		//qf.pretty_print();
		
		for (int i = 0; i < max_key; i++) {		
			boolean success = qf.search(i);	
			Assert(success);
			success = qf.delete(i);
			Assert(success);
			success = qf.search(i);
			//Assert(!success);
		}
		
		if (qf.lazy_deletes) {
			qf.expand(); // we need to expand to remove all void entries 
		}
		
		//qf.pretty_print();
		//System.out.println("num_existing_entries "  + qf.num_existing_entries);
		//System.out.println("secondary_IF.num_existing_entries "  + qf.secondary_IF.num_existing_entries);
		
		for (int i = 0; i < max_key; i++) {		
			boolean success = qf.search(i);	
			Assert(!success);
		}
		
		// a key inserted before any expansions 
		Assert(qf.num_existing_entries == 0);
		Assert(qf.secondary_IF.num_existing_entries == 0);
		System.out.println("success");
	}
	
	static public void test3() {
		int bits_per_entry = 10;
		int num_entries_power = 3;	
		DuplicatingChainedInfiniFilter qf = new DuplicatingChainedInfiniFilter(num_entries_power, bits_per_entry, false);
		qf.expand_autonomously = true;
		qf.fprStyle = FingerprintGrowthStrategy.FalsePositiveRateExpansion.UNIFORM;
		insert_all_and_then_delete_all(qf);
	}
	
	static public void test4() {
		int bits_per_entry = 10;
		int num_entries_power = 3;	
		DuplicatingChainedInfiniFilter qf = new DuplicatingChainedInfiniFilter(num_entries_power, bits_per_entry, false);
		qf.expand_autonomously = true;
		qf.fprStyle = FingerprintGrowthStrategy.FalsePositiveRateExpansion.POLYNOMIAL;
		insert_all_and_then_delete_all(qf);
	}
	
	static public void test5() {
		int bits_per_entry = 10;
		int num_entries_power = 3;	
		DuplicatingChainedInfiniFilter qf = new DuplicatingChainedInfiniFilter(num_entries_power, bits_per_entry, true);
		qf.expand_autonomously = true;
		qf.fprStyle = FingerprintGrowthStrategy.FalsePositiveRateExpansion.UNIFORM;
		insert_all_and_then_delete_all(qf);
	}
	
	static public void test6() {
		int bits_per_entry = 10;
		int num_entries_power = 3;	
		DuplicatingChainedInfiniFilter qf = new DuplicatingChainedInfiniFilter(num_entries_power, bits_per_entry, true);
		qf.expand_autonomously = true;
		qf.fprStyle = FingerprintGrowthStrategy.FalsePositiveRateExpansion.POLYNOMIAL;
		insert_all_and_then_delete_all(qf);
	}

	
	public static void run_tests() {
		//test1();
		
		test3();
		test4();	
		test5();
		test6();
		
		test7();
		test8();	
		test9();
		test10();
		
		//test7();
	}
	
	static public void test7() {
		int bits_per_entry = 10;
		int num_entries_power = 3;
		int seed = 2;  // 10
		//int num_entries = (int)Math.pow(2, num_entries_power);
		BasicInfiniFilter qf = new DuplicatingChainedInfiniFilter(num_entries_power, bits_per_entry, false);
		qf.expand_autonomously = true;
		qf.fprStyle = FingerprintGrowthStrategy.FalsePositiveRateExpansion.UNIFORM;
		Tests.test_insertions_and_deletes(qf);
		System.out.println("success");
	}
	
	static public void test8() {
		int bits_per_entry = 10;
		int num_entries_power = 3;
		int seed = 2;  // 10
		//int num_entries = (int)Math.pow(2, num_entries_power);
		BasicInfiniFilter qf = new DuplicatingChainedInfiniFilter(num_entries_power, bits_per_entry, false);
		qf.expand_autonomously = true;
		qf.fprStyle = FingerprintGrowthStrategy.FalsePositiveRateExpansion.POLYNOMIAL;
		Tests.test_insertions_and_deletes(qf);
		System.out.println("success");
	}
	
	static public void test9() {
		int bits_per_entry = 10;
		int num_entries_power = 3;
		int seed = 2;  // 10
		//int num_entries = (int)Math.pow(2, num_entries_power);
		DuplicatingChainedInfiniFilter qf = new DuplicatingChainedInfiniFilter(num_entries_power, bits_per_entry, true);
		qf.expand_autonomously = true;
		qf.fprStyle = FingerprintGrowthStrategy.FalsePositiveRateExpansion.UNIFORM;
		Tests.test_insertions_and_deletes(qf);
		System.out.println("success");
	}
	
	static public void test10() {
		int bits_per_entry = 10;
		int num_entries_power = 3;
		int seed = 2;  // 10
		//int num_entries = (int)Math.pow(2, num_entries_power);
		BasicInfiniFilter qf = new DuplicatingChainedInfiniFilter(num_entries_power, bits_per_entry, true);
		qf.expand_autonomously = true;
		qf.fprStyle = FingerprintGrowthStrategy.FalsePositiveRateExpansion.POLYNOMIAL;
		Tests.test_insertions_and_deletes(qf);
		System.out.println("success");
	}
	

	
	static public void test12() {
		int bits_per_entry = 10;
		int num_entries_power = 3;	
		DuplicatingChainedInfiniFilter qf = new DuplicatingChainedInfiniFilter(num_entries_power, bits_per_entry, true);
		qf.expand_autonomously = true;
		qf.fprStyle = FingerprintGrowthStrategy.FalsePositiveRateExpansion.UNIFORM;
		int max_key = (int)Math.pow(2, num_entries_power + qf.fingerprintLength + 1);
		for (int i = 0; i < max_key; i++) {		
			boolean success = qf.insert(i, false);
			Assert(success);
		}

		//qf.pretty_print();
		
		boolean success = qf.search(0);
		
		success = qf.delete(0);
		Assert(success);
		
		qf.pretty_print();
		
		success = qf.search(0);
		Assert(!success);
		
		//qf.pretty_print();
		
		qf.expand();
		
		System.out.println("success");
	}

	


	
}
		
		
		
		


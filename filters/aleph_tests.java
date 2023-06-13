package filters;

import java.util.ArrayList;

public class aleph_tests {
	
	static public void insert_all_and_then_delete_all(DuplicatingChainedInfiniFilter qf) {

		int max_key = (int)Math.pow(2, qf.power_of_two_size + qf.fingerprintLength + 1);
		for (int i = 0; i < max_key; i++) {		
			boolean success = qf.insert(i, false);
			Assert(success);
		}

		//qf.pretty_print();
		
		//System.out.println(qf.num_void_entries + "  " + qf.secondary_IF.num_void_entries);

		
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
		Assert(qf.num_void_entries == 0);
		Assert(qf.num_distinct_void_entries == 0);
		
		Assert(qf.secondary_IF.num_existing_entries == 0);
		Assert(qf.secondary_IF.num_void_entries == 0);
		Assert(qf.secondary_IF.num_distinct_void_entries == 0);
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

	
	static void Assert(boolean condition) {
		if (!condition) {
			System.out.println("assertion failed");
			System.exit(1);
		}
	}

	
}
		
		
		
		


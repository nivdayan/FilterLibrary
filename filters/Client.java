package filters;

import infiniFilter_experiments.Experiment1;
import infiniFilter_experiments.Experiment2;
import infiniFilter_experiments.Experiment3;
import infiniFilter_experiments.Experiment4;
import infiniFilter_experiments.InfiniFilterExperiments;

public class Client {

	static void run_tests() {
		Tests.test1(); // example from wikipedia
		Tests.test2(); // example from quotient filter paper
		Tests.test3(); // ensuring no false negatives
		Tests.test4(); // overflow test
		Tests.test5(); // deletion test 
		Tests.test6(); // iteration test 1
		Tests.test7(); // iteration test 2
		Tests.test8(); // expansion test for FingerprintShrinkingQF
		Tests.test9(); // expansion test for MultiplyingQF
		Tests.test10(); // testing InfiniFilter
		Tests.test12(); // testing InfiniFilter - chained
		Tests.test13(); // testing InfiniFilter - rejuvenation 
		Tests.test14(); // InfiniFilter deleting largest matching fingerprint 
		Tests.test15(); // testing deletes
		Tests.test16(); // testing deletes 
		Tests.test17(); // testing deletes 
		Tests.test18(); // testing deletes & rejuv operations
		Tests.test19(); // testing xxhash 
		Tests.test20(1000000); //testing xxhash(ByteBuffer)==xxhash(long)
		Tests.test21(1000000); // testing insert,search an delete of types int,long,String,byte[] 
		Tests.test22(); // testing no false negatives for bloom filter 
		Tests.test23(); // no false negatives for cuckoo filter
		Tests.test24(); // testing false positive rate for quotient filter  
		Tests.test25(); // testing false positive rate for cuckoo filter 
		Tests.test26(); // testing false positive rate for bloom filter 
		Tests.test27(); // exceeding the bound of the quotient filter 
		
		System.out.println("all tests passed");
	}
	
	static public  void main(String[] args) {
		
		run_tests();
				
		//QuotientFilter new_qf = new QuotientFilter(30 + 1, 13 + 3);
		
		//Experiment2.main(null);
		//Experiment4.main(null);
		//InfiniFilterExperiments.rejuvenation_experiment();
		//InfiniFilterExperiments.memory_experiment();
		//InfiniFilterExperiments.experiment_false_positives();
		//InfiniFilterExperiments.experiment_insertion_speed();
		//InfiniFilterExperiments.increasing_fingerprint_sizes_experiment();
	}


}

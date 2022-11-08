package testing_project;

public class Client {

	static public  void main(String[] args) {
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
		Tests.test11(); // testing InfiniFilter 
		Tests.test12(); // testing InfiniFilter - chained
		Tests.test13(); // testing InfiniFilter - rejuvenation 
		Tests.test14(); // InfiniFilter deleting largest matching fingerprint 
		Tests.test15(); // testing deletes
		Tests.test16(); // testing deletes
		Tests.test17(); // testing deletes
		Tests.test18(); // testing deletes & rejuv operations
	
		System.out.println("all tests passed");
		//InfiniFilterExperiments.scalability_experiment();
		//InfiniFilterExperiments.rejuvenation_experiment();
		//InfiniFilterExperiments.memory_experiment();
		//InfiniFilterExperiments.experiment_false_positives();
		//InfiniFilterExperiments.experiment_insertion_speed();
		//InfiniFilterExperiments.increasing_fingerprint_sizes_experiment();
	}


}

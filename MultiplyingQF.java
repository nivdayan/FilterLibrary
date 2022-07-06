package testing_project;

import java.util.ArrayList;
import java.util.BitSet;

public class MultiplyingQF extends QuotientFilter {

	enum SizeExpansion {
		LINEAR,
		GEOMETRIC,
	}
	
	enum FalsePositiveRateExpansion {
		UNIFORM,
		POLYNOMIAL,
		GEOMETRIC,
	}
	
	SizeExpansion sizeStyle;
	FalsePositiveRateExpansion fprStyle;

	MultiplyingQF(int power_of_two, int bits_per_entry) {
		super(power_of_two, bits_per_entry);
		older_filters = new ArrayList<QuotientFilter>();
		max_entries_before_expansion = (int)(Math.pow(2, power_of_two_size) * expansion_threshold);
		sizeStyle = SizeExpansion.GEOMETRIC;
		fprStyle = FalsePositiveRateExpansion.UNIFORM;
	}
	
	ArrayList<QuotientFilter> older_filters;

	int get_num_entries() {
		int num_entries = super.get_num_entries();
		for (QuotientFilter q : older_filters) {
			num_entries += q.get_num_entries();
		}
		return num_entries; 
	}
	
	double get_utilization() {
		int num_slots = 1 << power_of_two_size;
		for (QuotientFilter q : older_filters) {
			num_slots += 1 << q.power_of_two_size;
		}
		int num_entries = get_num_entries();
		double utilization = num_entries / (double) num_slots;
		return utilization;
	}
	
	int get_new_fingerprint_size() {
		int original_fingerprint_size = older_filters.get(0).bitPerEntry - 3;
		double original_FPR = Math.pow(2, -original_fingerprint_size);
		int new_filter_num = older_filters.size();
		double new_filter_FPR = 0;
		if (fprStyle == FalsePositiveRateExpansion.GEOMETRIC) {
			double factor = 1.0 / Math.pow(2, new_filter_num);
			new_filter_FPR = factor * original_FPR; 
		}
		else if (fprStyle == FalsePositiveRateExpansion.POLYNOMIAL) {
			double factor = 1.0 / Math.pow(new_filter_num + 1, 2);
			new_filter_FPR = factor * original_FPR; 
		}
		else if (fprStyle == FalsePositiveRateExpansion.UNIFORM) {
			new_filter_FPR = original_FPR; 
		}
		double fingerprint_size = -Math.ceil( Math.log(new_filter_FPR) / Math.log(2) );
		int fingerprint_size_int = (int) fingerprint_size;
		return fingerprint_size_int;
	}
	
	double measure_num_bits_per_entry() {
		int num_entries = get_num_entries();
		for (QuotientFilter q : older_filters) {
			int q_num_entries = q.get_num_entries();
			num_entries += q_num_entries;
		}
		int init_size = 1 << power_of_two_size;
		int num_bits = bitPerEntry * init_size + num_extension_slots * bitPerEntry;
		for (QuotientFilter q : older_filters) {
			init_size += 1 << (q.power_of_two_size + 1);
			num_bits += q.bitPerEntry * init_size + q.num_extension_slots * q.bitPerEntry;
		}
		
		double bits_per_entry = num_bits / num_entries;
		return bits_per_entry;
	}
	
	void expand() {
		QuotientFilter placeholder = new QuotientFilter(power_of_two_size, bitPerEntry, filter);
		older_filters.add(placeholder);
		placeholder.num_existing_entries = num_existing_entries;
		num_existing_entries = 0;
		power_of_two_size += sizeStyle == SizeExpansion.GEOMETRIC ? 1 : 0;
		fingerprintLength = get_new_fingerprint_size();
		bitPerEntry = fingerprintLength + 3;
		int init_size = 1 << (power_of_two_size + 1);
		filter = new BitSet(bitPerEntry * init_size);
		num_extension_slots += 2;		
		max_entries_before_expansion = (int)(Math.pow(2, power_of_two_size) * expansion_threshold);
		//System.out.println("expanding");
	}
	
	// The hash function is being computed here for each filter 
	// However, it's not such an expensive function, so it's probably not a performance issue. 
	boolean search(int input) {
		if (super.search(input)) {
			return true;
		}
		
		for (QuotientFilter qf : older_filters) {
			if (qf.search(input)) {
				return true;
			}
		}
		
		return false;
	}
	
	
	void print_levels() {
		double sum_FPRs = 0;
		for (QuotientFilter q : older_filters) {
			double FPR = Math.pow(2, - q.fingerprintLength);
			sum_FPRs += FPR;
			System.out.println(q.num_existing_entries + "\t" + q.fingerprintLength + "\t" + q.fingerprintLength + "\t" + FPR);
			
		}
		double FPR = Math.pow(2, - fingerprintLength);
		sum_FPRs += FPR;
		System.out.println(num_existing_entries + "\t" + fingerprintLength + "\t" + fingerprintLength + "\t" + FPR);
		System.out.println("sum FPRs: " + sum_FPRs);
	}
	
}

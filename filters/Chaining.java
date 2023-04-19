package filters;

import java.util.ArrayList;

public class Chaining extends QuotientFilter {

	public enum SizeExpansion {
		LINEAR,
		GEOMETRIC,
	}

	SizeExpansion sizeStyle;
	FingerprintGrowthStrategy.FalsePositiveRateExpansion fprStyle;

	public void set_fpr_style(FingerprintGrowthStrategy.FalsePositiveRateExpansion val) {
		fprStyle = val;
	}
	
	public void set_growth_style(SizeExpansion val) {
		sizeStyle = val;
	}
	
	public Chaining(int power_of_two, int bits_per_entry) {
		super(power_of_two, bits_per_entry);
		older_filters = new ArrayList<QuotientFilter>();
		max_entries_before_expansion = (int)(Math.pow(2, power_of_two_size) * expansion_threshold);
		sizeStyle = SizeExpansion.GEOMETRIC;
		fprStyle = FingerprintGrowthStrategy.FalsePositiveRateExpansion.UNIFORM;
	}
	
	ArrayList<QuotientFilter> older_filters;

	public long get_num_entries(boolean include_all_internal_filters) {
		long num_entries = super.get_num_entries(false);
		if (!include_all_internal_filters) {
			return num_entries;
		}
		for (QuotientFilter q : older_filters) {
			num_entries += q.get_num_entries(false);
		}
		return num_entries; 
	}
	
	public double get_utilization() {
		long num_slots = 1L << power_of_two_size;
		for (QuotientFilter q : older_filters) {
			num_slots += 1L << q.power_of_two_size;
		}
		long num_entries = get_num_entries(true);
		double utilization = num_entries / (double) num_slots;
		return utilization;
	}
	
	public double measure_num_bits_per_entry() {
		return measure_num_bits_per_entry(this, older_filters);
	}
	
	boolean expand() {
		QuotientFilter placeholder = new QuotientFilter(power_of_two_size, bitPerEntry, filter);
		placeholder.hash_type = this.hash_type;
		older_filters.add(placeholder);
		placeholder.num_existing_entries = num_existing_entries;
		num_existing_entries = 0;
		power_of_two_size += sizeStyle == SizeExpansion.GEOMETRIC ? 1 : 0;
		
		fingerprintLength = FingerprintGrowthStrategy.get_new_fingerprint_size(original_fingerprint_size, num_expansions, fprStyle);
		bitPerEntry = fingerprintLength + 3;
		long init_size = 1L << power_of_two_size;
		num_extension_slots += 2;		
		filter = make_filter(init_size, bitPerEntry);
		super.update(init_size);
		max_entries_before_expansion = (long)(Math.pow(2, power_of_two_size) * expansion_threshold);
		//System.out.println("expanding");
		return true;
	}
	
	// The hash function is being computed here for each filter 
	// However, it's not such an expensive function, so it's probably not a performance issue. 
	public boolean search(long input) {
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
	
	public void pretty_print() {	
		
		for (QuotientFilter q : older_filters) {
			q.pretty_print();
			System.out.println();
		}
		System.out.println();
		super.pretty_print();
	}
	
}

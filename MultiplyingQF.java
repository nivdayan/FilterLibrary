import java.util.ArrayList;
import java.util.BitSet;

public class MultiplyingQF extends QuotientFilter {

	MultiplyingQF(int power_of_two, int bits_per_entry) {
		super(power_of_two, bits_per_entry);
		older_filters = new ArrayList<QuotientFilter>();
		max_entries_before_expansion = (int)(Math.pow(2, power_of_two_size) * expansion_threshold);

	}
	
	ArrayList<QuotientFilter> older_filters;

	void expand() {
		QuotientFilter placeholder = new QuotientFilter(power_of_two_size, bitPerEntry, filter);
		older_filters.add(placeholder);
		placeholder.num_existing_entries = num_existing_entries;
		num_existing_entries = 0;
		bitPerEntry++;
		power_of_two_size++;
		fingerprintLength++;
		int init_size = 1 << (power_of_two_size + 1);
		filter = new BitSet(bitPerEntry * init_size);
		num_extension_slots += 2;		
		max_entries_before_expansion = (int)(Math.pow(2, power_of_two_size) * expansion_threshold);

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
	
}

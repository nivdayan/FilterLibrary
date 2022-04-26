
public class ExpandableQF extends QuotientFilter {

	ExpandableQF(int power_of_two, int bits_per_entry) {
		super(power_of_two, bits_per_entry);

	}
	
	// There is probably a more efficient implementation of this using rank&select
	// however, most fingerprints will have short unary codes 
	// the expected parsing time is therefore constant amortized time. 
	int parse_unary(long fingerprint) {
		//print_int_in_binary((int) fingerprint, power_of_two_size + 1);
		long mask = 1;
		for (int i = 0; i < 63; i++) {
			boolean one = (fingerprint & mask) > 0;
			if (!one) {
				return i;
			}
			mask = mask << 1;
		}
		return -1;
	}
	
	long gen_fingerprint(int large_hash) {
		int fingerprint_mask = (1 << (fingerprintLength - 1)) - 1;
		fingerprint_mask = fingerprint_mask << power_of_two_size;
		int fingerprint = (large_hash & fingerprint_mask) >> power_of_two_size;
		//print_int_in_binary( fingerprint_mask, fingerprintLength );
		//print_int_in_binary( fingerprint, fingerprintLength );
		fingerprint <<= 1;
		//print_int_in_binary( fingerprint, fingerprintLength );
		System.out.println();
		return fingerprint;
	}
	
	void expand() {
		QuotientFilter new_qf = new QuotientFilter(power_of_two_size + 1, bitPerEntry - 1);
		num_extension_slots = (power_of_two_size + 1) * 2;
		Iterator it = new Iterator(this);
		
		while (it.next()) {
			int bucket = it.bucket_index;
			long fingerprint = it.fingerprint;
			
			//int generation = parse_unary(fingerprint);
			long mask = ~(~0 << fingerprintLength);
			long new_fingerprint = ((fingerprint << 1) | 1) & mask;
			
			new_fingerprint = new_fingerprint & mask; 
			print_int_in_binary( (int)fingerprint, fingerprintLength);

			print_int_in_binary( (int)new_fingerprint, fingerprintLength);
			System.out.println(); 
			
			long pivot_bit = (1 & fingerprint);
			long bucket_mask = pivot_bit << power_of_two_size;
			long updated_bucket = bucket | bucket_mask;
			//long updated_fingerprint = fingerprint >> 1;
			
			/*System.out.println(bucket); 
			System.out.print("bucket1      : ");
			print_int_in_binary( bucket, power_of_two_size);
			System.out.print("fingerprint1 : ");
			print_int_in_binary((int) fingerprint, fingerprintLength);
			System.out.print("pivot        : ");
			print_int_in_binary((int) pivot_bit, 1);
			System.out.print("bucket2      : ");
			print_int_in_binary((int) updated_bucket, power_of_two_size + 1);
			System.out.print("fingerprint2 : ");
			print_int_in_binary((int) updated_fingerprint, fingerprintLength - 1);
			System.out.println();
			System.out.println();*/
			
			new_qf.insert(new_fingerprint, (int)updated_bucket, false);
		}
		
		filter = new_qf.filter;
		power_of_two_size++;
		num_extension_slots += 2;
		bitPerEntry--;
		//fingerprintLength--;
		max_entries_before_expansion = (int)(Math.pow(2, power_of_two_size) * expansion_threshold);

	}

}

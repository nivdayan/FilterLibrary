package infiniFilter_experiments;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;

import filters.BitSacrificer;
import filters.BloomFilter;
import filters.ChainedInfiniFilter;
import filters.CuckooFilter;
import filters.Filter;
import filters.InfiniFilter;
import filters.MultiplyingQF;
import filters.QuotientFilter;
import infiniFilter_experiments.InfiniFilterExperiments.baseline;

public class Experiment1 {

	static int bits_per_entry = 16;
	static int num_entries_power = 10;	
	static int num_cycles = 22; // went up to 31
	
	static void parse_arguments(String[] args) {
		if (args != null) {
	        ArrayList<Integer> argsArr = new ArrayList<Integer>(args.length); // could be 9
	        for (String val : args) {
	            int temp = Integer.parseInt(val);
	            argsArr.add(temp);
	        }   
	        if (argsArr.size() > 0 && argsArr.get(0) > 0) {
	        	bits_per_entry = argsArr.get(0);
	        }
	        if (argsArr.size() > 1 && argsArr.get(1) > 0) {
	        	num_entries_power = argsArr.get(1);
	        }
	        if (argsArr.size() > 2 && argsArr.get(2) > 0) {
	        	num_cycles = argsArr.get(2);
	        }
		}
	}
	
	public static File create_file(String file_name) {
		try {
			File f = new File( file_name  );
			if (f.createNewFile()) {
				System.out.println("Results File created: " + f.getName());
			} else {
				System.out.println("Results file will be overwritten.");
			}
			return f;
		} catch (IOException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
		return null;
	}
	
	public static void main(String[] args) {
		parse_arguments(args);
		
		System.gc();
		{
			QuotientFilter qf = new QuotientFilter(num_entries_power, bits_per_entry);
			InfiniFilterExperiments.scalability_experiment(qf, 0, qf.get_max_entries_before_expansion() - 1, new baseline());
		}
		{
			QuotientFilter qf = new QuotientFilter(num_entries_power, bits_per_entry);
			InfiniFilterExperiments.scalability_experiment(qf, 0, qf.get_max_entries_before_expansion() - 1, new baseline());
		}
		
		System.gc();
		
		baseline bloom_res = new baseline();
		{
			int num_entries = (int) Math.pow(2, (num_entries_power + num_cycles) / 2);
			Filter bloom = new BloomFilter(num_entries, bits_per_entry);
			long starting_index = 0;
			for (int i = num_entries_power; i < (num_entries_power + num_cycles) / 2 + 1; i++ ) {
				long end_key = (int)(Math.pow(2, i) ); // 
				InfiniFilterExperiments.scalability_experiment(bloom, starting_index, end_key, bloom_res);
				starting_index = end_key;
				//int num_insertions = original_qf_res.metrics.get("num_entries").get(i);
				//orig.expand();
				//System.out.println("# entries: " + qf.num_existing_entries + " new capacity: " + Math.pow(2, qf.power_of_two_size));
			}
		}
		System.out.println("finished bloom");
		System.gc();
		
		baseline cuckoo_res = new baseline();
		{
			Filter cuckoo = new CuckooFilter((num_entries_power + num_cycles) / 2, bits_per_entry);
			long starting_index = 0;
			for (int i = num_entries_power; i < (num_entries_power + num_cycles) / 2 + 1; i++ ) {
				long end_key = (int)(Math.pow(2, i) * 0.95); // 
				InfiniFilterExperiments.scalability_experiment(cuckoo, starting_index, end_key, cuckoo_res);
				starting_index = end_key;
			}
		}
		System.out.println("finished cuckoo");
		
		System.gc();
		
		baseline original_qf_res = new baseline();
		{
			QuotientFilter orig = new QuotientFilter((num_entries_power + num_cycles) / 2, bits_per_entry);
			orig.set_expand_autonomously(false); 
			long starting_index = 0;
			for (int i = num_entries_power; i < (num_entries_power + num_cycles) / 2 + 1; i++ ) {
				long end_key = (int)(Math.pow(2, i) * 0.90); // 
				InfiniFilterExperiments.scalability_experiment(orig, starting_index, end_key, original_qf_res);
				starting_index = end_key;
			}
		}

		System.gc();
		System.out.println("finished quotient");

		baseline chained_IF_res = new baseline();
		{
			InfiniFilter qf = new ChainedInfiniFilter(num_entries_power, bits_per_entry);
			qf.set_expand_autonomously(true); 
			long starting_index = 0;
			long end_key = qf.get_max_entries_before_expansion() - 1;
			for (int i = num_entries_power; i <= num_cycles; i++ ) {
				InfiniFilterExperiments.scalability_experiment(qf, starting_index, end_key,  chained_IF_res);
				starting_index = end_key;
				end_key = qf.get_max_entries_before_expansion() * 2 - 1;
			}
		}	
		System.out.println("finished infinifilter");
		System.gc();
		
		baseline bit_sacrifice_res = new baseline();
		{
			BitSacrificer qf2 = new BitSacrificer(num_entries_power, bits_per_entry);
			qf2.set_expand_autonomously(true); 
			long starting_index = 0;
			long end_key = qf2.get_max_entries_before_expansion() - 1;
			for (int i = num_entries_power; i <= num_cycles && qf2.get_fingerprint_length() > 0; i++ ) {
				InfiniFilterExperiments.scalability_experiment(qf2, starting_index, end_key, bit_sacrifice_res);
				starting_index = end_key;
				end_key = qf2.get_max_entries_before_expansion() * 2 - 1;
			}
		}
		System.out.println("finished BF");
		
		System.gc();

		baseline geometric_expansion_res = new baseline();
		{
			MultiplyingQF qf3 = new MultiplyingQF(num_entries_power, bits_per_entry);
			qf3.set_expand_autonomously(true);
			long starting_index = 0;
			long end_key = qf3.get_max_entries_before_expansion() - 1;
			for (int i = num_entries_power; i <= num_cycles - 1; i++ ) {
				InfiniFilterExperiments.scalability_experiment(qf3, starting_index, end_key, geometric_expansion_res);
				starting_index = end_key + 1;
				end_key = (long)(qf3.get_max_entries_before_expansion() * 2 + starting_index - 1);
				//System.out.println("thresh  " + qf3.max_entries_before_expansion);
				
				//(long)(Math.pow(2, power_of_two_size) * expansion_threshold)
				//System.out.println("# entries: " + qf3.num_existing_entries + " new capacity: " + Math.pow(2, qf3.power_of_two_size + 1));
			}
		}
		System.out.println("finished geometric chaining");

		int commas_before = 1;
		int commas_after = 5;
		original_qf_res.print("num_entries", "insertion_time", commas_before++, commas_after--);
		chained_IF_res.print("num_entries", "insertion_time", commas_before++, commas_after--);
		bit_sacrifice_res.print("num_entries", "insertion_time", commas_before++, commas_after--);
		geometric_expansion_res.print("num_entries", "insertion_time", commas_before++, commas_after--);
		bloom_res.print("num_entries", "insertion_time", commas_before++, commas_after--);
		cuckoo_res.print("num_entries", "insertion_time", commas_before++, commas_after--);

		
		System.out.println();

		commas_before = 1;
		commas_after = 5;
		original_qf_res.print("num_entries", "query_time", commas_before++, commas_after--);
		chained_IF_res.print("num_entries", "query_time", commas_before++, commas_after--);
		bit_sacrifice_res.print("num_entries", "query_time", commas_before++, commas_after--);
		geometric_expansion_res.print("num_entries", "query_time", commas_before++, commas_after--);
		bloom_res.print("num_entries", "query_time", commas_before++, commas_after--);
		cuckoo_res.print("num_entries", "query_time", commas_before++, commas_after--);

		System.out.println();

		commas_before = 1;
		commas_after = 5;
		original_qf_res.print("num_entries", "FPR", commas_before++, commas_after--);
		chained_IF_res.print("num_entries", "FPR", commas_before++, commas_after--);
		bit_sacrifice_res.print("num_entries", "FPR", commas_before++, commas_after--);
		geometric_expansion_res.print("num_entries", "FPR", commas_before++, commas_after--);
		bloom_res.print("num_entries", "FPR", commas_before++, commas_after--);
		cuckoo_res.print("num_entries", "FPR", commas_before++, commas_after--);

		
		System.out.println();

		commas_before = 1;
		commas_after = 5;
		original_qf_res.print("num_entries", "memory", commas_before++, commas_after--);
		chained_IF_res.print("num_entries", "memory", commas_before++, commas_after--);
		bit_sacrifice_res.print("num_entries", "memory", commas_before++, commas_after--);
		geometric_expansion_res.print("num_entries", "memory", commas_before++, commas_after--);
		bloom_res.print("num_entries", "memory", commas_before++, commas_after--);
		cuckoo_res.print("num_entries", "memory", commas_before++, commas_after--);

		String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(Calendar.getInstance().getTime());

		LocalDate ld = java.time.LocalDate.now();
		String dir_name = "Exp1_" + timeStamp.toString();
	    Path path = Paths.get(dir_name);

		try {
			Files.createDirectories(path);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		String write_latency_file_name = dir_name + "/writes_speed.txt";
		String read_latency_file_name  = dir_name + "/read_speed.txt";
		String FPR_file_name  = dir_name + "/false_positive_rate.txt";
		String memory_file_name  = dir_name + "/memory.txt";
		
		create_file(write_latency_file_name);
		create_file(read_latency_file_name);
		create_file(FPR_file_name);
		create_file(memory_file_name);
		
	    try {
	        FileWriter insertion_writer = new FileWriter(write_latency_file_name);
	        
			commas_before = 1;
			commas_after = 5;
			original_qf_res.print_to_file("num_entries", "insertion_time", commas_before++, commas_after--, insertion_writer);
			chained_IF_res.print_to_file("num_entries", "insertion_time", commas_before++, commas_after--, insertion_writer);
			bit_sacrifice_res.print_to_file("num_entries", "insertion_time", commas_before++, commas_after--, insertion_writer);
			geometric_expansion_res.print_to_file("num_entries", "insertion_time", commas_before++, commas_after--, insertion_writer);
			bloom_res.print_to_file("num_entries", "insertion_time", commas_before++, commas_after--, insertion_writer);
			cuckoo_res.print_to_file("num_entries", "insertion_time", commas_before++, commas_after--, insertion_writer);

			//System.out.println();
			insertion_writer.close();
	        FileWriter reads_writer = new FileWriter(read_latency_file_name);

			commas_before = 1;
			commas_after = 5;
			original_qf_res.print_to_file("num_entries", "query_time", commas_before++, commas_after--, reads_writer);
			chained_IF_res.print_to_file("num_entries", "query_time", commas_before++, commas_after--, reads_writer);
			bit_sacrifice_res.print_to_file("num_entries", "query_time", commas_before++, commas_after--, reads_writer);
			geometric_expansion_res.print_to_file("num_entries", "query_time", commas_before++, commas_after--, reads_writer);
			bloom_res.print_to_file("num_entries", "query_time", commas_before++, commas_after--, reads_writer);
			cuckoo_res.print_to_file("num_entries", "query_time", commas_before++, commas_after--, reads_writer);

			//System.out.println();
			reads_writer.close();
	        FileWriter FPR_writer = new FileWriter(FPR_file_name);

			commas_before = 1;
			commas_after = 5;
			original_qf_res.print_to_file("num_entries", "FPR", commas_before++, commas_after--, FPR_writer);
			chained_IF_res.print_to_file("num_entries", "FPR", commas_before++, commas_after--, FPR_writer);
			bit_sacrifice_res.print_to_file("num_entries", "FPR", commas_before++, commas_after--, FPR_writer);
			geometric_expansion_res.print_to_file("num_entries", "FPR", commas_before++, commas_after--, FPR_writer);
			bloom_res.print_to_file("num_entries", "FPR", commas_before++, commas_after--, FPR_writer);
			cuckoo_res.print_to_file("num_entries", "FPR", commas_before++, commas_after--, FPR_writer);

			FPR_writer.close();
			FileWriter mem_writer = new FileWriter(memory_file_name);
			
			//System.out.println();

			commas_before = 1;
			commas_after = 5;
			original_qf_res.print_to_file("num_entries", "memory", commas_before++, commas_after--, mem_writer);
			chained_IF_res.print_to_file("num_entries", "memory", commas_before++, commas_after--, mem_writer);
			bit_sacrifice_res.print_to_file("num_entries", "memory", commas_before++, commas_after--, mem_writer);
			geometric_expansion_res.print_to_file("num_entries", "memory", commas_before++, commas_after--, mem_writer);
			bloom_res.print_to_file("num_entries", "memory", commas_before++, commas_after--, mem_writer);
			cuckoo_res.print_to_file("num_entries", "memory", commas_before++, commas_after--, mem_writer);
	        
			mem_writer.close();
	        System.out.println("Successfully wrote to the files.");
	      } catch (IOException e) {
	        System.out.println("An error occurred.");
	        e.printStackTrace();
	      }

	}
	
}

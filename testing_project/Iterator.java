package testing_project;

import java.util.ArrayDeque;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

public class Iterator  {

	QuotientFilter qf;
	int index;
	int bucket_index;
	long fingerprint;
	Queue<Integer> s;

	Iterator(QuotientFilter new_qf) {
		qf = new_qf;
		s = new LinkedList<Integer>();
		//s = new ArrayDeque<Integer>();
		index = 0;
		bucket_index = -1;
		fingerprint = -1;
	}
	
	void clear() {
		s.clear();
		index = 0;
		bucket_index = -1;
		fingerprint = -1;
	}

	boolean next() {
		
		if (index == qf.get_logical_num_slots_plus_extensions()) {
			return false;
		}	
		
		boolean occupied = qf.is_occupied(index);
		boolean shifted = qf.is_shifted(index);
		boolean continuation = qf.is_continuation(index);
		
		while (!occupied && !continuation && !shifted && index < qf.get_logical_num_slots_plus_extensions()) {
			index++;
			if (index == qf.get_logical_num_slots_plus_extensions()) {
				return false;
			}	
			occupied = qf.is_occupied(index);
			shifted = qf.is_shifted(index);
			continuation = qf.is_continuation(index); 
		} 

		if (occupied && !continuation && !shifted) {
			s.clear();
			s.add(index);
			bucket_index = index;
		}
		else if (occupied && continuation && shifted) {
			s.add(index);
		}
		else if (!occupied && !continuation && shifted) {
			s.remove();
			bucket_index = s.peek();
		}
		else if (!occupied && continuation && shifted) {
			// do nothing
		}
		else if (occupied && !continuation && shifted) {
			s.add(index);
			s.remove(); 
			bucket_index = s.peek();
		}

		fingerprint = qf.get_fingerprint(index);
		index++;
		return true;
	}
	
	void print() {
		System.out.println("original slot: " + index + "  " + bucket_index);
	}


}

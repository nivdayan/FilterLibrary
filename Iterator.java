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
		index = 0;
		bucket_index = -1;
		fingerprint = -1;
	}
	
	boolean finished() {
		int num_slots = qf.get_logical_num_slots();
		return index >= num_slots;
	}
	
	void clear() {
		s.clear();
		index = 0;
		bucket_index = -1;
		fingerprint = -1;
	}

	boolean next() {
		boolean occupied = qf.is_occupied(index);
		boolean shifted = qf.is_shifted(index);
		boolean continuation = qf.is_continuation(index);
		boolean finished = false;
		
		while (!occupied && !continuation && !shifted && !finished) {
			index++;
			occupied = qf.is_occupied(index);
			shifted = qf.is_shifted(index);
			continuation = qf.is_continuation(index); 
			finished = finished();
		} 
		
		if (finished) {
			bucket_index = -1;
			fingerprint = -1;
			return false;
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

	/*void scan() {
		Queue<Integer> s = new LinkedList<Integer>();
		int current_index = 0;

		for (int i = 0; i < qf.filter.size(); i++) {
			boolean occupied = qf.is_occupied(i);
			boolean shifted = qf.is_shifted(i);
			boolean continuation = qf.is_continuation(i); 

			if (!occupied && !continuation && !shifted) {
				current_index = -1;
				continue;
			}

			if (occupied) {
				s.add(i);
			}


			if (!continuation && !shifted) {
				current_index = i;
			}
			else if (!continuation && shifted) {
				s.remove();
				current_index = s.peek();
			}

			long fingerprint = qf.get_fingerprint(i);

			System.out.println("original slot: " + i + "  " + current_index);
		}
	}*/


}

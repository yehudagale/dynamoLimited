import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.Random;
import java.util.Collections;
public class Ring extends Message implements Serializable {
	ArrayList<Integer> virtualNodes;
	HashMap<Integer, Integer> virtual_to_real;
	Integer replicationNum;
	//for now this number is 3 can change depending on requirements
	final Integer VIRTUAL_NUM = 3;
	Ring(List<Integer> realNodes, Integer replicationNum)
	{
		if (realNodes.size() < replicationNum) {
			throw new IllegalArgumentException("N must be less than or equal to the number of nodes");
		}
		this.virtualNodes = new ArrayList<Integer>();
		this.virtual_to_real = new HashMap<Integer, Integer>();
		Random randGen = new Random();
		int nextRand;
		for (Integer node : realNodes) {
			for (int i = 0; i < VIRTUAL_NUM; i++ ) {
				nextRand = randGen.nextInt();
				virtualNodes.add(nextRand);
				virtual_to_real.put(nextRand, node);
			}
		}
		Collections.sort(virtualNodes);
		System.out.println(virtualNodes);
	}
	public List<Integer> getLocations(Integer key)
	{
		ArrayList<Integer> toRet = new ArrayList<Integer>(replicationNum);
		int location = binSearch(key);
		for (int i = 0; i < replicationNum; i++) {
			toRet.add(virtual_to_real.get(virtualNodes.get(i)));
		}
		return toRet;
	}
	private int binSearch(Integer key)
	{
		//used https://en.wikipedia.org/wiki/Binary_search_algorithm;
		int left = 0;
		int right = virtualNodes.size();
		int mid = -1; 
		while(left < right){
			mid = (left + right) / 2;
			if (virtualNodes.get(mid) < key) {
				left = mid + 1;
			}
			else if (virtualNodes.get(mid) > key) {
				right = mid - 1;
			}
			else{
				return mid;
			}
		}
		return mid;	
	}
	public static void main(String[] args) {
		ArrayList realNodes = new ArrayList<Integer>();
		realNodes.add(320948);
		realNodes.add(645233);
		realNodes.add(345353);
		realNodes.add(345432);
		realNodes.add(656765);
		realNodes.add(234566);
		realNodes.add(74534);
		Ring thisRing = new Ring(realNodes, 4);
		System.out.println(thisRing.getLocations(10));
	}
}
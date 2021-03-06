import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.Random;
import java.util.Collections;
import java.util.HashSet;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;

public class Ring extends Response implements Serializable {
	ArrayList<Integer> virtualNodes;
	HashMap<Integer, Integer> virtual_to_real;
	Integer replicationNum;
	Integer numExtra;
	//for now this number is 3 can change depending on requirements

	final Integer VIRTUAL_NUM = 10;
	Ring(List<Integer> realNodes, Integer replicationNum, Integer numExtra)
	{
		super(false);
		if (realNodes.size() < replicationNum + numExtra) {
			throw new IllegalArgumentException("N must be less than or equal to the number of nodes");
		}
		this.numExtra = numExtra;
		this.replicationNum = replicationNum;
		HashSet<Integer> tempVirtual = new HashSet<Integer>();
		this.virtual_to_real = new HashMap<Integer, Integer>();
		Random randGen = new Random();
		int nextRand;
		int num_virtual_nodes = VIRTUAL_NUM;
		for (Integer node : realNodes) {
			for (int i = 0; i < num_virtual_nodes; i++ ) {
				nextRand = randGen.nextInt();
				if (!tempVirtual.add(nextRand)){
					num_virtual_nodes++;
				}
				else{
					virtual_to_real.put(nextRand, node);
				}
			}
		}
		this.virtualNodes = new ArrayList<Integer>(tempVirtual);
		Collections.sort(virtualNodes);
		// System.out.println(virtualNodes);
		// for (Integer key : virtual_to_real.keySet() ) {
		// 	System.out.println(key + ", " + virtual_to_real.get(key));
		// }
	}
	public HashSet<Integer> getSetLocations(Object toInsert, int extraNodes)
	{
		Integer key = hashWithMD5(toInsert);
		// System.out.println("key is:" + key);
		HashSet<Integer> toRet = new HashSet<Integer>(replicationNum);
		int location = binSearch(key);
		int next_node;
		int tempRepNum = this.replicationNum + extraNodes;
		// System.out.println(location);
		for (int i = 0; i < tempRepNum; i++) {
			next_node = virtual_to_real.get(virtualNodes.get((i + location) % virtualNodes.size()));
			if (!toRet.add(next_node)){
				tempRepNum++;
			}
		}
		return toRet;
	}
	public ArrayList<Integer> getPorts()
	{
		HashSet<Integer> ports = new HashSet<Integer>();
		for (Integer key : this.virtual_to_real.keySet()) {
			ports.add(this.virtual_to_real.get(key));
		}
		return new ArrayList<Integer>(ports);
	}
	private Integer hashWithMD5(Object toHash)
	{
		if (!(toHash instanceof Serializable)) {
			System.out.println("using default hash");
			return toHash.hashCode();
		}
		else {
			// System.out.println("using MD5 hash");
			MessageDigest digest = null;
			try{
				digest = MessageDigest.getInstance("MD5"); 
			}catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
				return -1;
			}
			try{
				byte[] unhashed = convertToBytes(toHash);
				digest.update(unhashed);
			}
			catch (IOException e) {
				System.out.println("IOException using default hash");
				return toHash.hashCode();
			}
			byte[] hashed = digest.digest();
			//used https://stackoverflow.com/questions/7619058/convert-a-byte-array-to-integer-in-java-and-vice-versa
			return hashed[0] << 24 | (hashed[1] & 0xFF) << 16 | (hashed[2] & 0xFF) << 8 | (hashed[3] & 0xFF);
		}

	}
	//copied from https://stackoverflow.com/questions/2836646/java-serializable-object-to-byte-array
	private byte[] convertToBytes(Object object) throws IOException {
	    try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
	         ObjectOutputStream out = new ObjectOutputStream(bos)) {
	        out.writeObject(object);
	        return bos.toByteArray();
	     }
	}
	public List<Integer> getLocations(Object toInsert, int extraNodes)
	{
		return new ArrayList<Integer>(getSetLocations(toInsert, extraNodes));
	}
	public List<Integer> getLocations(Object toInsert)
	{
		return getLocations(toInsert, 0);
	}
	public HashSet<Integer> getSetLocations(Object toInsert)
	{
		return getSetLocations(toInsert, 0);
	}
	public List<Integer> prefList(Object toInsert)
	{
		return getLocations(toInsert, numExtra);
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
		ArrayList<Integer> realNodes = new ArrayList<Integer>();
		realNodes.add(320948);
		realNodes.add(645233);
		realNodes.add(345353);
		realNodes.add(345432);
		realNodes.add(656765);
		realNodes.add(234566);
		realNodes.add(74534);
		realNodes.add(2354);
		realNodes.add(235325);
		realNodes.add(835933);
		realNodes.add(894935);
		realNodes.add(434094);
		realNodes.add(253234);

		Ring thisRing = new Ring(realNodes, 4, 3);
		System.out.println(thisRing.getLocations("10"));
		System.out.println(thisRing.getLocations("We can encode many things"));
		System.out.println(thisRing.getLocations("10"));
		System.out.println(thisRing.getLocations("We can encode many things"));
		System.out.println(thisRing.getLocations(120933321));
		System.out.println(thisRing.getLocations(50));


	}
}
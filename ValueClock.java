import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.HashSet;
public class ValueClock implements Message, Serializable{
	private ArrayList<Object> values;
	private ArrayList<HashMap<Integer, Integer>> vectorClock;
	ValueClock(Object value, Integer creatingProccess, Integer clockNum)
	{
		this.values = new ArrayList<Object>();
		this.vectorClock = new ArrayList<>();
		HashMap<Integer, Integer> map1 = new HashMap<>();
		map1.put(creatingProccess, clockNum);
		this.values.add(value);
		this.vectorClock.add(map1);
	}
	public ArrayList<Object> values()
	{
		return (ArrayList<Object>) values.clone();
	}
	// public synchronized ValueClock resolveAfterWrite(Object newValue, Integer proccess, Integer clockNum)
	// {
	// }
	public synchronized ValueClock maxClock(Object value, Integer proccess, Integer clockNum)
	{
		HashMap<Integer, Integer> newClock = vectorClock.get(0);
		if (values.size() > 1)
		{
			for (int i = 1; i < vectorClock.size() ; i++) {
				for (Integer key : vectorClock.get(i).keySet()) {
					if (newClock.get(key) == null) {
						newClock.put(key, vectorClock.get(i).get(key));
					}
					else{
						newClock.put(key, Math.max(newClock.get(key), vectorClock.get(i).get(key)));
					}
				}
			}
		}
		newClock.put(proccess, clockNum);
		this.vectorClock.clear();
		this.values.clear();
		values.add(value);
		vectorClock.add(newClock);
		return this;
	}
	public synchronized ValueClock resolveClocks(ValueClock newClock)
	{
		ArrayList<Object> tempVals = new ArrayList<Object>();
		ArrayList<HashMap<Integer, Integer>> tempVectorClock = new ArrayList<>();
		for (int i = 0; i < newClock.values.size() ;i++) {
			Object workingValue = newClock.values.get(i);
			HashMap<Integer, Integer> workingClock = newClock.vectorClock.get(i);
			boolean added = false;
			for (int j = 0; j < this.values.size() ;j++) {
				if (isAncestor(newClock.vectorClock.get(i), this.vectorClock.get(j))) {
					//current working clock is an ancestor, can be forgotten
					break;
				}
				else if(isDecendant(newClock.vectorClock.get(i), this.vectorClock.get(j))){
					if (!added) {
						//current working clock is a decendant, 
						//we need to replace the ancestor with it
						this.values.set(j, workingValue);
						this.vectorClock.set(j, workingClock);
						added = true;
					}
					else {
						//current working clock is a decendant,
						//but we already added it so we just delete the unnesesary one
						this.values.remove(j);
						this.vectorClock.remove(j);
					}
				}
				else{
					//current working clock and current other clock 
					//are both neccesary
					if (!added) {
						//current working clock is a decendant, 
						//we need to replace the ancestor with it
						this.values.add(workingValue);
						this.vectorClock.add(workingClock);
						added = true;
					}
				}
			}
		}
		return this;
	}
	// public synchronized ValueClock resolveClocks(ValueClock newClock)
	// {
	// 	//fix concurrent error if possible trying to resolve and send at same time
	// 	if (newClock != null) {
	// 		ArrayList<Object> tempVals = new ArrayList<Object>();
	// 		ArrayList<HashMap<Integer, Integer>> tempVectorClock = new ArrayList<>();
	// 		// LinkedList<Integer> delLocs = new LinkedList<>();
	// 		HashSet<Integer> skipSet = new HashSet<>();
	// 		for (int i = 0; i < newClock.values.size() ;i++) {
	// 			// boolean overWriten = false;
	// 			for (int j = 0; j < this.values.size() ;j++) {
	// 				if (skipSet.contains(j)) {
	// 					continue;
	// 				}
	// 				System.out.println("new is: " + newClock.vectorClock.get(i) + " old is: " + this.vectorClock.get(j) + " i " + i + " j " + j);
	// 				if (isAncestor(newClock.vectorClock.get(i), this.vectorClock.get(j))) {
	// 					tempVals.add(this.values.get(j));
	// 					tempVectorClock.add(this.vectorClock.get(j));
	// 					break;
	// 				}
	// 				else if(isDecendant(newClock.vectorClock.get(i), this.vectorClock.get(j))){
	// 						tempVals.add(newClock.values.get(i));
	// 						tempVectorClock.add(newClock.vectorClock.get(i));
	// 						skipSet.add(j);
	// 				}
	// 				else{
	// 					tempVals.add(newClock.values.get(i));
	// 					tempVectorClock.add(newClock.vectorClock.get(i));
	// 					tempVals.add(this.values.get(j));
	// 					tempVectorClock.add(this.vectorClock.get(j));
	// 				}
	// 			}
	// 		}
	// 		// for (Integer location : delLocs) {
				
	// 		// }
	// 		this.values.addAll(tempVals);
	// 		this.vectorClock.addAll(tempVectorClock);
	// 	}
	// 	return this;
	// }
	/*
	equal clocks are ancestors so that we do minimum amount of work
	*/
	private boolean isDecendant(HashMap<Integer, Integer> toCompare, HashMap<Integer, Integer> thisClock)
	{
		for (Integer thisKey: thisClock.keySet()) {
			if (toCompare.get(thisKey) == null || toCompare.get(thisKey) < thisClock.get(thisKey)) {
				return false;
			}
		}
		return true;
	}
	private boolean isAncestor(HashMap<Integer, Integer> toCompare, HashMap<Integer, Integer> thisClock)
	{
		for (Integer otherKey: toCompare.keySet()) {
			if (thisClock.get(otherKey) == null || thisClock.get(otherKey) < toCompare.get(otherKey)) {
				return false;
			}
		}
		return true;
	}
	// toCompare[A:1, B:1, C:1],
	// thisClock[A:1, B:1]

	@Override
	public String toString()
	{
		if (values == null) {
			return null;
		}
		else{
			return values.toString() + " vectorClock: " + vectorClock.toString();
		}
	}
}
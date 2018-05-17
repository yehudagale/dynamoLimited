import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.HashSet;
public class ValueClock extends Message implements  Serializable{
	private ArrayList<ValueClockPair> values;
	// private ArrayList<Object> values;
	// private ArrayList<HashMap<Integer, Integer>> vectorClock;
	ValueClock(Object value, Integer creatingProccess, Integer clockNum)
	{
		super(creatingProccess);
		this.values = new ArrayList<ValueClockPair>();
		// this.vectorClock = new ArrayList<>();
		HashMap<Integer, Integer> map1 = new HashMap<>();
		map1.put(creatingProccess, clockNum);
		this.values.add(new ValueClockPair(value, map1));
		// this.values.add(value);
		// this.vectorClock.add(map1);
	}
	public ArrayList<Object> values()
	{
		ArrayList<Object> toRet = new ArrayList<Object>();
		for (ValueClockPair pair : values) {
			toRet.add(pair.value);
		}
		return toRet;
	}
	public boolean same(ValueClock otherClock)
	{
		if (otherClock == null) {
			return false;
		}
		HashSet<ValueClockPair> thisSet = new HashSet<>(this.values);
		HashSet<ValueClockPair> otherSet = new HashSet<>(otherClock.values);
		return thisSet.equals(otherSet);
	}
	// public synchronized ValueClock resolveAfterWrite(Object newValue, Integer proccess, Integer clockNum)
	// {
	// }
	// @Override
	// public boolean equals(Object otherClock)
	// {
	// 	// this.values.sort();
	// 	// otherClock.values.sort();
	// 	if (! (otherClock instanceof ValueClock)) {
	// 		return false;
	// 	}
	// 	return this.values.equals(otherClock.values);
		// if (this.values.size() != otherClock.values.size()) {
		// 	return false;
		// }
		// for (int i = 0; i < values.size() ; i++) {
			
		// 	if (this.values.get(i).vectorClock.size() != otherClock.values.get(i).vectorClock.size()) {
				
		// 	}
		// 	for (Integer key : this.values.get(i).vectorClock.keySet()) {
		// 		if (otherClock.vectorClock.get(key) != this.vectorClock.get(key)) {
		// 			return false;
		// 		}
		// 	}
		// }

	// }
	public synchronized ValueClock maxClock(Object value, Integer proccess, Integer clockNum)
	{
		HashMap<Integer, Integer> newClock = this.values.get(0).vectorClock;
		if (values.size() > 1)
		{
			for (int i = 1; i < values.size() ; i++) {
				for (Integer key : values.get(i).vectorClock.keySet()) {
					if (newClock.get(key) == null) {
						newClock.put(key, values.get(i).vectorClock.get(key));
					}
					else{
						newClock.put(key, Math.max(newClock.get(key), values.get(i).vectorClock.get(key)));
					}
				}
			}
		}
		newClock.put(proccess, clockNum);
		// this.vectorClock.clear();
		this.values.clear();
		this.values.add(new ValueClockPair(value, newClock));
		// vectorClock.add(newClock);
		return this;
	}
	public synchronized ValueClock resolveClocks(ValueClock newClock)
	{
		ArrayList<ValueClockPair> tempVals = new ArrayList<>();
		// ArrayList<Object> tempVals = new ArrayList<Object>();
		// ArrayList<HashMap<Integer, Integer>> tempVectorClock = new ArrayList<>();
		for (int i = 0; i < newClock.values.size() ;i++) {
			ValueClockPair workingPair = newClock.values.get(i);
			// Object workingValue = newClock.values.get(i);
			// HashMap<Integer, Integer> workingClock = newClock.vectorClock.get(i);
			boolean added = false;
			for (int j = 0; j < this.values.size() ;j++) {
				if (isAncestor(workingPair.vectorClock, this.values.get(j).vectorClock)) {
					//current working clock is an ancestor, can be forgotten
					break;
				}
				else if(isDecendant(workingPair.vectorClock, this.values.get(j).vectorClock)){
					if (!added) {
						//current working clock is a decendant, 
						//we need to replace the ancestor with it
						this.values.set(j, workingPair);
						// this.values.set(j, workingValue);
						// this.vectorClock.set(j, workingClock);
						added = true;
					}
					else {
						//current working clock is a decendant,
						//but we already added it so we just delete the unnesesary one
						this.values.remove(j);
						// this.vectorClock.remove(j);
					}
				}
				else{
					//current working clock and current other clock 
					//are both neccesary
					if (!added) {
						//current working clock is a decendant, 
						//we need to replace the ancestor with it
						this.values.add(workingPair);
						// this.vectorClock.add(workingClock);
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
			return values.toString();
		}
	}
}
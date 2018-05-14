import java.util.ArrayList;
import java.util.List;
public class ValueClock implements Message{
	ArrayList<Object> values;
	ArrayList<List<Integer>> clockNums;
	ArrayList<List<Integer>> proccesses;
	ValueClock(Object value, Integer creatingProccess, Integer clockNum)
	{
		this.values = new ArrayList<Object>();
		this.clockNums = new ArrayList<>();
		this.proccesses = new ArrayList<>();
		ArrayList<Integer> firstClock = new ArrayList<>();
		ArrayList<Integer> pNum = new ArrayList<>();
		firstClock.add(clockNum);
		pNum.add(creatingProccess);
		clockNums.add(firstClock);
		proccesses.add(pNum);
		this.values.add(value);
	}
	public void resolveValues(Object newValue, Integer proccess, Integer clockNum)
	{

	}
}
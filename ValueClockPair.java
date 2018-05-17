import java.io.Serializable;
import java.util.HashMap;
public class ValueClockPair implements Serializable {
	Object value;
	HashMap<Integer, Integer> vectorClock;
	ValueClockPair(Object value, HashMap<Integer, Integer> clock)
	{
		this.value = value;
		this.vectorClock = clock;
	}
	@Override
	public String toString()
	{
		return value + ":" + vectorClock.toString();
	}
	@Override
	public boolean equals(Object other)
	{
		if (! (other instanceof ValueClockPair) ) {
			return false;
		}

		return this.vectorClock.equals(((ValueClockPair) other).vectorClock);
	}
	@Override
	public int hashCode()
	{
		return this.vectorClock.hashCode();
	}
}
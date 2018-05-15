import java.io.Serializable;
import java.util.ArrayList;
public class ReadResponse extends Response implements Serializable {
	public ValueClock values;
	public Object key;
	public Integer coordinator;
	ReadResponse(ValueClock values, Object key, Integer coordinator)
	{
		super(false);
		this.values = values;
		this.key = key;
		this.coordinator = coordinator;
	}
	public 	ArrayList<Object> getValues()
	{
		if (this.values != null) {
			return this.values.values();
		}
		else {
			return null;
		}
	}
	@Override
	public String toString()
	{
		if (values == null) {
			return "Value Not Found";
		}
		return this.values.toString();
	}
}
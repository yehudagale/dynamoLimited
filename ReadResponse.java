import java.io.Serializable;
import java.util.ArrayList;
public class ReadResponse extends Response implements Serializable {
	public ArrayList<Object> values;
	public Object key;
	public Integer coordinator;
	ReadResponse(ArrayList<Object> values, Object key)
	{
		this.values = values;
		this.key = key;
	}
	@Override
	public String toString()
	{
		return this.values.toString();
	}
}
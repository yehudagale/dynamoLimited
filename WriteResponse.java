import java.io.Serializable;
public class WriteResponse extends ReadResponse implements Serializable{
	WriteResponse(ValueClock values, Object key, Integer coordinator)
	{
		super(values, key, coordinator);
	}
}
import java.io.Serializable;
public class WriteRequest extends ReadRequest implements Serializable {
	public Object value;	
	public ReadResponse context = null;
	WriteRequest(Object key, Object value, Integer responsePort)
	{
		super(key, responsePort);
		this.value = value;
	}
	WriteRequest(Object key, Object value, Integer responsePort, ReadResponse context)
	{
		this(key, value, responsePort);
		this.context = context;
	}
}
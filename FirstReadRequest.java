import java.io.Serializable;
public class ReadRequest extends Message implements Serializable {
	public Object key;
	public Integer responsePort;
	FirstReadRequest(Object key, Integer responsePort)
	{
		this.key = key;
		this.responsePort = responsePort;
	}
}
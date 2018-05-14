import java.io.Serializable;
public class SimpleRead implements Serializable, Message {
	public Object key;
	public Integer responsePort;
	SimpleRead(Object key, Integer responsePort)
	{
		this.key = key;
		this.responsePort = responsePort;
	}
}
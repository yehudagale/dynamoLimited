import java.io.Serializable;
public class SimpleRead extends Message implements Serializable {
	public Object key;
	public Integer responsePort;
	SimpleRead(Object key, Integer responsePort)
	{
		this.key = key;
		this.responsePort = responsePort;
	}
}
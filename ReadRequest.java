import java.io.Serializable;
public class ReadRequest extends Message implements Serializable {
	public Object key;
	public Integer responsePort;
	boolean fowarded;		
	ReadRequest(Object key, Integer responsePort)
	{
		this.key = key;
		this.responsePort = responsePort;
		this.fowarded = false;
	}
}
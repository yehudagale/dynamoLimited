import java.io.Serializable;
public class ReadRequest extends SimpleRead implements Serializable {
	boolean fowarded;		
	ReadRequest(Object key, Integer responsePort)
	{
		super(key, responsePort);
		this.fowarded = false;
	}
}

import java.io.Serializable;
public class RingRequest extends Message implements Serializable  {
	Integer ringServer;
	RingRequest(Integer responsePort)
	{
		super();
		this.ringServer = responsePort;
	}
}
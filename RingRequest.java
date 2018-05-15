
import java.io.Serializable;
public class RingRequest extends Message implements Serializable  {
	RingRequest(Integer responsePort)
	{
		super(responsePort);
	}
}
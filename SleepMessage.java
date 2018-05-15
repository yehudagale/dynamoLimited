import java.io.Serializable;
public class SleepMessage extends Message implements Serializable  {
	Integer responsePort;
	SleepMessage(Integer responsePort)
	{
		this.responsePort = responsePort;
	}
}
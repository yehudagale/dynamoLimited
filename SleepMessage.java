import java.io.Serializable;
public class SleepMessage implements Serializable, Message  {
	Integer responsePort;
	SleepMessage(Integer responsePort)
	{
		this.responsePort = responsePort;
	}
}
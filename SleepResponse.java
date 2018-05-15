import java.io.Serializable;
public class SleepResponse implements Serializable, Message  {
	Integer tempPort;
	SleepResponse(Integer tempPort)
	{
		System.out.println("made new SleepResponse");
		this.tempPort = tempPort;
	}
}
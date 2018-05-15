import java.io.Serializable;
public class SleepResponse extends Message implements Serializable  {
	Integer tempPort;
	SleepResponse(Integer tempPort)
	{
		System.out.println("made new SleepResponse");
		this.tempPort = tempPort;
	}
}
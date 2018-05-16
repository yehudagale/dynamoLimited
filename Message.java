import java.io.Serializable;
public abstract class Message {
	Integer ringServer;
	Message()
	{
		ringServer = -1;
	}
	Message(Integer ringServer)
	{
		this.ringServer = ringServer;
	}
}
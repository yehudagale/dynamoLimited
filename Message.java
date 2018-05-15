import java.io.Serializable;
public abstract class Message {
	Integer ringServer;
	Message(Integer ringServer)
	{
		this.ringServer = ringServer;
	}
}
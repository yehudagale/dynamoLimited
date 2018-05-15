import java.io.Serializable;
public class Response implements Message, Serializable  {
	boolean failed;
	Response(boolean failed)
	{
		this.failed = failed;
	}
}
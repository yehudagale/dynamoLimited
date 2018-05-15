import java.io.Serializable;
public class Response extends Message implements Serializable  {
	boolean failed;
	Response(boolean failed)
	{
		this.failed = failed;
	}
}
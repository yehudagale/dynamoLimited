import java.io.Serializable;
public class Response extends Message implements Serializable  {
	boolean failed;
	Response(boolean failed)
	{
		this.failed = failed;
	}
	public static void main(String[] args) {
		Client this_client = new Client(args[0]);
		this_client.send_requests();
	}
}
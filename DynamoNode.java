import java.net.*;
import java.io.IOException;
import java.nio.file.*;
public class DynamoNode {
	private ServerSocket messageGetter;
	private Integer myPortNum;
	DynamoNode(String fileName)
	{
		myPortNum = makeNewSocket();
		//using https://stackoverflow.com/questions/1625234/how-to-append-text-to-an-existing-file-in-java
		String strToWrite = myPortNum.toString();
		strToWrite += '\n';
		try {
		    Files.write(Paths.get(fileName), strToWrite.getBytes(), StandardOpenOption.APPEND);
		}catch (IOException e) {
		    e.printStackTrace();
		}

	}
	private Integer makeNewSocket()
	{
		try {
		   this.messageGetter = new ServerSocket(0);
		}
		catch (IOException e) {
		   System.out.println(e);
		}
		return this.messageGetter.getLocalPort();
	}
	public void start()
	{
		boolean exit = false;
		while(!exit)
		{
			//add responses to messages here
		}
	}
	public static void main(String[] args) {
		System.out.println(args);
		DynamoNode thisNode = new DynamoNode(args[0]);
		thisNode.start();
	}
}
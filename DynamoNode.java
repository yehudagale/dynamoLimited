import java.net.*;
import java.io.IOException;
import java.nio.file.*;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ConcurrentHashMap;
public class DynamoNode{
	private ServerSocket messageGetter;
	Integer myPortNum;
	private Socket socket = null;
	Ring myRing;
	private ExecutorService threadPool = Executors.newFixedThreadPool(10);
	ConcurrentHashMap<Integer, Object> dataMap;
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
		dataMap = new ConcurrentHashMap<>();
		//for testing;
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
	public void initialize()
	{
		//used http://www.coderpanda.com/java-socket-programming-transferring-of-java-objects-through-sockets/
		try {
			socket = messageGetter.accept();
			System.out.println("Connected");
			ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());

			this.myRing = (Ring) inStream.readObject();
			System.out.println("Object received = " + myRing);
			socket.close();
			this.acceptMessages();

		} catch (SocketException se) {
			se.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException cn) {
			cn.printStackTrace();
		}

	}
	private void acceptMessages()
	{
		System.out.println(myRing.getLocations(1000));
		boolean exit = false;
		while(!exit)
		{
		//used http://tutorials.jenkov.com/java-multithreaded-servers/thread-pooled-server.html			try {
	        socket = null;
	        try {
	            socket = this.messageGetter.accept();
	        } catch (IOException e) {
	        	e.printStackTrace();
	        }
	        this.threadPool.execute(
	            new MessageProcessor(socket, this));
	    }

	}
	public static void main(String[] args) {
		System.out.println(args);
		DynamoNode thisNode = new DynamoNode(args[0]);
		thisNode.initialize();
	}
}
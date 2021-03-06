import java.net.*;
import java.io.IOException;
import java.nio.file.*;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Set;
public class DynamoNode{
	private ServerSocket messageGetter;
	Integer myPortNum;
	private Socket socket = null;
	Ring myRing;
	boolean ignoreNext = false;
	private ExecutorService threadPool = Executors.newFixedThreadPool(10);
	ConcurrentHashMap<Object, ValueClock> dataMap;
	ConcurrentHashMap<Integer, Set<SimpleReadResponse>> readMap;
	AtomicInteger counter;
	AtomicInteger readKeyGetter;
	//for testing purposes
	// boolean alive = true;
	Integer responsePort = null;
	DynamoNode(String fileName)
	{
		counter = new AtomicInteger();
		readKeyGetter = new AtomicInteger();
		this.readMap = new ConcurrentHashMap<>();
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
			// if (! message.) {
				
			// }
			Message messageRecieved = (Message) inStream.readObject();
			String messageType = messageRecieved.getClass().toString();
			messageType = messageType.substring(messageType.indexOf(" ")).trim();
			//used https://stackoverflow.com/questions/4584541/check-if-a-class-is-subclass-of-another-class-in-java
			if (SimpleRead.class.isAssignableFrom(messageRecieved.getClass())) {
				Client.sendMessage(new RingRequest(myPortNum), ((SimpleRead) messageRecieved).responsePort, 10);
			}
			else {
				this.myRing = (Ring) messageRecieved;
			}
			System.out.println("Object received = " + myRing);
			socket.close();

		} catch (SocketException se) {
			se.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException cn) {
			cn.printStackTrace();
		}

	}
	public void acceptMessages()
	{
		// System.out.println(myRing.getLocations(1000));
		while(true)
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
	        // if (asleep) {
	        // 	System.out.println("got all the way here");
	        // 	this.waitForWake();
	        // }
	    }
	}
	void exit()
	{
		try {
		    messageGetter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.exit(0);
	}
	// private void waitForWake()
	// {
	// 	ServerSocket tempSocket = null;
	// 	try {
	// 	   this.messageGetter.close();
	// 	   tempSocket = new ServerSocket(0);
	// 	}
	// 	catch (IOException e) {
	// 	   System.out.println(e);
	// 	}
	// 	Client.sendMessage(new SleepResponse(tempSocket.getLocalPort()), this.responsePort);
	// 	while(asleep)
	// 	{
	// 	//used http://tutorials.jenkov.com/java-multithreaded-servers/thread-pooled-server.html			try {
	//         socket = null;
	//         try {
	//             socket = tempSocket.accept();
	//         } catch (IOException e) {
	//         	e.printStackTrace();
	//         }
	//         Message messageRecieved = null;
	//         try{
	//         	ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());
	//         	messageRecieved = (Message) inStream.readObject();
	//         	System.out.println("Object received while waiting to wake = " + messageRecieved);
	//         	socket.close();
	//         } catch (SocketException se) {
	//         	se.printStackTrace();
	//         } catch (IOException e) {
	//         	e.printStackTrace();
	//         } catch (ClassNotFoundException cn) {
	//         	cn.printStackTrace();
	//         }
	//         String messageType = messageRecieved.getClass().toString();
	//         messageType = messageType.substring(messageType.indexOf(" ")).trim();
	//         System.out.println("messageType:" + messageType);
 //        	if (messageType.equals("WakeUpMessage")) {
 //        		asleep = false;
 //        		try{
 //        			this.messageGetter = new ServerSocket(this.myPortNum);
 //        		}catch (IOException e) {
 //        			e.printStackTrace();
 //        		}
 //        		return;
 //        	}
	//     }

	// }
	public static void main(String[] args) {
		System.out.println(args);
		DynamoNode thisNode = new DynamoNode(args[0]);
		thisNode.initialize();
		// thisNode.acceptMessages();
	}
}
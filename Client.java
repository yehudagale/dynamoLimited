import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.List;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.ServerSocket;

public class Client {
	private final int EXTRA = 0;
	private List<Integer> ports;
	private Integer placeInList;
	private ServerSocket messageGetter;
	private Integer portNum;
	Client(String filePath)
	{
		placeInList = 0;
		this.getPorts(filePath);
		this.portNum = makeNewSocket();
	}
	private void getPorts(String filePath)
	{
		this.ports = new ArrayList<>();
		//used https://www.journaldev.com/709/java-read-file-line-by-line
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(filePath));
			String line = reader.readLine();
			System.out.println("line is: " + line);
			line = reader.readLine();
			System.out.println("line is: " + line);
			while (line != null) {
				// read next line
				System.out.println("line is: " + line);
				ports.add(Integer.valueOf(line));
				line = reader.readLine();

			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(ports);
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

	public void sendRings(Integer repNum)
	{
		Ring ring = new Ring(ports, repNum, EXTRA);
		for (Integer port : ports) {
			sendMessage(ring, port);
		}
	}
	private Integer getNextAddress()
	{
		placeInList = (placeInList + 1) % ports.size();
		return ports.get(placeInList);
	}
	public static void sendMessage(Message toSend, int portNum)
	{
		//used http://www.coderpanda.com/java-socket-programming-transferring-of-java-objects-through-sockets/
		boolean isConnected = false;
		while (!isConnected) {
			try {
				Socket socket = new Socket("localHost", portNum);
				System.out.println("Connected");
				isConnected = true;
				ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
				System.out.println("Object to be written = " + toSend);
				outputStream.writeObject(toSend);

			} catch (SocketException se) {
				se.printStackTrace();
			// System.exit(0);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
	public ReadResponse sendRead(Object key, Integer tries)
	{
		ReadResponse response = null;
		if (tries == 0) {
			return response;
		}
		Integer targetNode = getNextAddress();
		ReadRequest request = new ReadRequest(key, this.portNum);
		sendMessage(request, targetNode);
		try{
			Socket socket = this.messageGetter.accept();
			ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());
			response = (ReadResponse) inStream.readObject();
			System.out.println("Object received = " + response);
			socket.close();
		} catch (SocketException se) {
			se.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException cn) {
			cn.printStackTrace();
		}
		return response;
	}
	public static void main(String[] args) {
		Client this_client = new Client(args[0]);
		this_client.sendRings(Integer.valueOf(args[1]));
		System.out.println(this_client.sendRead(2245, 1));
	}
}
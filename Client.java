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
import java.util.Scanner;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
public class Client {
	private final int EXTRA = 0;
	private List<Integer> ports;
	private Integer placeInList;
	private ServerSocket messageGetter;
	private Integer portNum;
	private ReadResponse lastRead;
	// private HashMap<Integer, Integer> sleepingNodes;
	private Ring ring;
	// private static Set<Integer> toSkip;
	Client(String filePath)
	{
		// if (toSkip ==  null) {
			// toSkip = ConcurrentHashMap.newKeySet();
		// }
		// System.out.println(toSkip);
		// toSkip = new HashSet<>();
		placeInList = 0;
		this.getPorts(filePath);
		this.portNum = makeNewSocket();
		// sleepingNodes = new HashMap<>();
	}
	Client(Integer ringServer)
	{
		placeInList = 0;
		this.ports = new ArrayList<Integer>();
		ports.add(-1);
		this.portNum = makeNewSocket();
		this.ring = (Ring) sendWithResponse(new RingRequest(this.portNum), ringServer, 1);
		
		System.out.println(this.ring);
		this.ports = this.ring.getPorts();
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
		this.ring = ring;
		for (Integer port : ports) {
			boolean sent = false;
			while(! sent){
				System.out.println("sending ring to port: " + port);
				sent = sendMessage(ring, port, 10);
			}
		}
	}
	private Integer getNextAddress()
	{
		placeInList = (placeInList + 1) % ports.size();
		return ports.get(placeInList);
	}
	public static boolean sendMessage(Message toSend, int portNum, int tries)
	{
		//used http://www.coderpanda.com/java-socket-programming-transferring-of-java-objects-through-sockets/
		// if (toSkip.contains(portNum)) {
		// 	return false;
		// }
		System.out.println("sending " + toSend + " to " + portNum);
		boolean isConnected = false;
		int howMany = tries;
		while (!isConnected) {
			// System.out.println(howMany);
			// System.out.println("failed");
			try {
				Socket socket = new Socket();
				socket.connect(new InetSocketAddress("localHost", portNum), 10);
				// System.out.println("Connected");
				isConnected = true;
				ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
				// System.out.println("Object to be written = " + toSend);
				outputStream.writeObject(toSend);
				if (tries != 0) {
					howMany--;
					if (howMany <= 0) {
						System.out.println("failed to send message");
						return false;
					}
				}

			} catch (SocketTimeoutException ste) {
				return false;
			}
			catch (SocketException se) {
				//se.printStackTrace();
				if (tries == 0) {
					tries = 10;
					howMany = 10;
				}
				else{
					howMany --;
				}
				if (howMany <= 0) {
					System.out.println("failed to send message");
					return false;
				}

			// System.exit(0);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return true;
	}
	public static void sendMessage(Message toSend, int portNum)
	{
		Client.sendMessage(toSend, portNum, 0);

	}
	public WriteResponse sendWrite(Object key, Object value, Integer tries, Integer targetNode)
	{
		WriteRequest request = new WriteRequest(key, value, this.portNum);
		Response response = this.sendWithResponse(request, targetNode, tries);
		if (response.failed) {
			return null;
		}
		return (WriteResponse) response;
	}
	public WriteResponse sendWrite(Object key, Object value, Integer tries)
	{
		return sendWrite(key, value, tries, getNextAddress());
	}
	public WriteResponse sendWriteWithContext(Object key, Object value, Integer tries)
	{
		return sendWriteWithContext(key, value, tries, getNextAddress());
	}
	public WriteResponse sendWriteWithContext(Object key, Object value, Integer tries, Integer targetNode)
	{
		WriteRequest request = new WriteRequest(key, value, this.portNum, lastRead);
		Response response = this.sendWithResponse(request, targetNode, tries);
		if (response.failed) {
			return null;
		}
		return (WriteResponse) response;
		// WriteResponse response = null;
		// if (tries == 0) {
		// 	return response;
		// }
		// Integer targetNode = getNextAddress();
		// sendMessage(request, targetNode);
		// try{
		// 	Socket socket = this.messageGetter.accept();
		// 	ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());
		// 	response = (WriteResponse) inStream.readObject();
		// 	System.out.println("Object received = " + response);
		// 	socket.close();
		// } catch (SocketException se) {
		// 	se.printStackTrace();
		// } catch (IOException e) {
		// 	e.printStackTrace();
		// } catch (ClassNotFoundException cn) {
		// 	cn.printStackTrace();
		// }
		// return response;

	}
	public Response sendWithResponse(Message request, Integer targetNode, Integer tries)
	{
		Response response = null;
		if (tries == 0) {
			return response;
		}
		boolean sent = false;
		int targetsTried = this.ports.size();
		while(! sent && targetsTried != 0){
			
			System.out.println("sending too " + targetNode);
			sent = sendMessage(request, targetNode, 10);
			targetNode = this.getNextAddress();
			targetsTried--;
		}
		if (targetsTried == 0) {
			System.out.println("failed to send message");
			return null;
		}
		try{
			messageGetter.setSoTimeout(600);
			Socket socket = this.messageGetter.accept();
			socket.setSoTimeout(500);
			ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());

			response = (Response) inStream.readObject();
			System.out.println("Object received = " + response);
			socket.close();
		}  catch (SocketException se) {
			se.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException cn) {
			cn.printStackTrace();
		}
		return response;
	}
	public ReadResponse sendRead(Object key, Integer tries, Integer targetNode)
	{
		ReadRequest request = new ReadRequest(key, this.portNum);
		Response response = this.sendWithResponse(request, targetNode, tries);
		if (response.failed) {
			return null;
		}
		ReadResponse readResponse = (ReadResponse) response;
		this.lastRead = readResponse;
		return readResponse;
	}
	public ReadResponse sendRead(Object key, Integer tries)
	{
		return sendRead(key, tries, getNextAddress());
	}
	// public void getSleepResponse(Integer toSleep)
	// {
	// 	try{
	// 		Socket socket = this.messageGetter.accept();
	// 		ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());
	// 		this.sleepingNodes.put(toSleep, ((SleepResponse) inStream.readObject()).tempPort);
	// 		socket.close();
	// 	} catch (SocketException se) {
	// 		se.printStackTrace();
	// 	} catch (IOException e) {
	// 		e.printStackTrace();
	// 	} catch (ClassNotFoundException cn) {
	// 		cn.printStackTrace();
	// 	}
	// }
	public void send_requests()
	{
		boolean exit = false;
		//used https://stackoverflow.com/questions/11871520/how-can-i-read-input-from-the-console-using-the-scanner-class-in-java/34549731
		Scanner sc = new Scanner(System.in);
		// System.out.println("\n>");
		StringBuilder log = new StringBuilder();
		StringBuilder log2 = new StringBuilder();
		String answer = "";
		while(!exit)
		{
			String word = "";
			System.out.print(">");
			word = sc.nextLine();
			log.append(word + "\n");
			try{

				if (word.startsWith("read")) {
					String key = word.substring(word.indexOf(' ') + 1);
					// System.out.println("--"+key+"--");
					answer = this.sendRead(key, 1).toString();
					System.out.println("****" + answer);
					log2.append(word + " " + answer + "\n");
				}
				else if (word.startsWith("history")) {
					System.out.println(log.toString());
				}
				else if (word.startsWith("ahistory")){
					System.out.println(log2.toString());
				}
				else if (word.startsWith("sleep")) {
					Integer dest = Integer.valueOf(word.substring(word.indexOf(' ') + 1));
					// this.toSkip.add(dest);
					boolean sent = sendMessage(new SleepMessage(portNum), dest, 10);
					// if (sent) {
					// 	this.getSleepResponse(dest);
					// }
				}
				else if (word.startsWith("killall")) {
					for (Integer dest : this.ports) {
						Client.sendMessage(new KillMessage(), dest, 10);
						Client.sendMessage(new DummyMessage(), dest, 10);
					}
				}
				else if (word.startsWith("kill")) {
					Integer dest = Integer.valueOf(word.substring(word.indexOf(' ') + 1));
					Client.sendMessage(new KillMessage(), dest, 10);
				}

				else if (word.startsWith("pref")) {
					String key = word.substring(word.indexOf(' ') + 1);
					System.out.println(ring.getLocations(key));
				}
				else if (word.startsWith("ring")) {
					// Integer dest = Integer.valueOf(word.substring(word.indexOf(' ') + 1));
					// Integer trueDest = this.sleepingNodes.get(dest);
					// if (trueDest == null) {
					// 	System.out.println("This client did not put that node to sleep");
					// }
					// else{
					this.ring = (Ring) sendWithResponse(new RingRequest(this.portNum), this.getNextAddress(), 1);
					// }
					// this.toSkip.remove(dest);
				}
				else if (word.startsWith("write")) {
					String key = word.substring(word.indexOf(' ') + 1, word.lastIndexOf(' '));
					String value = word.substring(word.lastIndexOf(' ') + 1);;
					answer = this.sendWrite(key, value, 1).toString();
					System.out.println("****" + answer);
					log2.append(word + " " + answer + "\n");
				}
				else if (word.startsWith("write")) {
					String key = word.substring(word.indexOf(' ') + 1, word.lastIndexOf(' '));
					String value = word.substring(word.lastIndexOf(' ') + 1);;
					answer = this.sendWrite(key, value, 1).toString();
					System.out.println("****" + answer);
					log2.append(word + " " + answer + "\n");
				}
				else if (word.startsWith("tread")) {
					String key = word.substring(word.indexOf(' ') + 1, word.lastIndexOf(' '));
					Integer destination = Integer.valueOf(word.substring(word.lastIndexOf(' ') + 1));
					answer = this.sendRead(key, 1, destination).toString();
					System.out.println("****" + answer);
					log2.append(word + " " + answer + "\n");
					// System.out.println("****" + this.sendRead(key, 1, destination));
				}
				else if (word.startsWith("twrite")) {
					Scanner tempScanner = new Scanner(word);
						tempScanner.next();
						String key =tempScanner.next();
						String value = tempScanner.next();
						Integer dest = tempScanner.nextInt();
						answer = this.sendWrite(key, value, 1, dest).toString();
						System.out.println("****" + answer);
						log2.append(word + " " + answer + "\n");
				}

				else if (word.startsWith("cwrite")) {
					String key = word.substring(word.indexOf(' ') + 1, word.lastIndexOf(' '));
					String value = word.substring(word.lastIndexOf(' ') + 1);
					answer = this.sendWriteWithContext(key, value, 1).toString();
					System.out.println("****" + answer);
					log2.append(word + " " + answer + "\n");
				}
				else if (word.startsWith("tcwrite")) {
					Scanner tempScanner = new Scanner(word);
							tempScanner.next();
							String key =tempScanner.next();
							String value = tempScanner.next();
							Integer dest = tempScanner.nextInt();
					answer = this.sendWriteWithContext(key, value, 1, dest).toString();
					System.out.println("****" + answer);
					log2.append(word + " " + answer + "\n");
				}

				else if (word.startsWith("exit")) {
					exit = true;
				}
				else{
					System.out.println("command not found");
				}
			}catch (Exception e) {
				e.printStackTrace();
				System.out.println("please enter in the commands correctly");;
			}

		}
	}
	public static void main(String[] args) {
		Client this_client = new Client(args[0]);
		this_client.sendRings(Integer.valueOf(args[1]));
		this_client.send_requests();
	}	
}
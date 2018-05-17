import java.net.*;

import java.io.ObjectInputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class MessageProcessor implements Runnable {
	DynamoNode node;
	Socket socket;
	MessageProcessor(Socket socket, DynamoNode node)
	{
		this.socket = socket;
		this.node = node;
	}
	@Override
	public void run()
	{
		Message messageRecieved = null;
		try{
			ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());
			messageRecieved = (Message) inStream.readObject();
			System.out.println("Object received = " + messageRecieved);
			socket.close();
		} catch (SocketException se) {
			se.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException cn) {
			cn.printStackTrace();
		}
		String messageType = messageRecieved.getClass().toString();
		messageType = messageType.substring(messageType.indexOf(" ")).trim();
		System.out.println("messageType:" + messageType);
		switch (messageType) {
			case "KillMessage":
				// node.alive = false;
				node.exit();
				break;
			case "DummyMessage":
			break;
			case "RingRequest":
				System.out.println("got request sedning ring");
				Client.sendMessage(node.myRing, ((RingRequest) messageRecieved).ringServer, 10);
			break;
			case "SleepMessage":
				// node.responsePort = ((SleepMessage) messageRecieved).responsePort;
				node.ignoreNext = true;
				// boolean sent = true;
				// while(sent == true)
				// {
				// 	System.out.println("sending dummy");
				// 	sent = Client.sendMessage(new DummyMessage(), node.myPortNum, 10);
				// }
				// Client.sendMessage(new SleepResponse)
				break;
			case "WriteRequest":
				WriteRequest write = (WriteRequest) messageRecieved;
				if (checkIfMine(write)) {
					System.out.println("doing write myself");
					proccessWrite(write);
				}
				else{
					fowardRequest(write);
				}
			break;
			case "ReadRequest":
				ReadRequest request = (ReadRequest) messageRecieved;
				if (checkIfMine(request)) {
					proccessRead(request);
				}
				else{
					fowardRequest(request);
				}
			break;
			case "SimpleishRead":
				SimpleishRead query = (SimpleishRead) messageRecieved;
				proccessSimpleRead(query);
			break;
			case "SimpleReadResponse":
				SimpleReadResponse resp = (SimpleReadResponse) messageRecieved;
				System.out.println("resp.key " +resp.uniqueKey);
				node.readMap.get(resp.uniqueKey).add(resp);
			break;
			case "SimpleWrite":
				SimpleWrite wQuery = (SimpleWrite) messageRecieved;
				proccessSimpleWrite(wQuery);
			break;
			default:
					System.out.println("Error:Unrecognized message");	
			
		}
	}
	private void proccessSimpleWrite(SimpleWrite toWrite)
	{
		if (node.ignoreNext) {
			node.ignoreNext = false;
			return;
		}
		System.out.println("writing: " + toWrite.value +  " from: " + node.myPortNum + " with key: " + toWrite.key);
		ValueClock current = node.dataMap.get(toWrite.key);
		if (current == null) {
			node.dataMap.put(toWrite.key, toWrite.value);
		}
		else{
			node.dataMap.put(toWrite.key, current.resolveClocks(toWrite.value));
		}
	}
	private void proccessSimpleRead(SimpleishRead request)
	{
		Client.sendMessage(new SimpleReadResponse(request.key, node.dataMap.get(request.key), node.myPortNum, request.uniqueKey), request.responsePort, 10);
	}
	private boolean checkIfMine(ReadRequest request)
	{
		if(request.fowarded)
		{
			return true;
		}
		else{
			//O(N) lookup consider improving
			return node.myRing.getSetLocations(request.key).contains(node.myPortNum);
		}
	}//change this method to allow for different values of R
	private void proccessRead(ReadRequest request)
	{
		ValueClock myValue = node.dataMap.get(request.key);
		System.out.println("requesting: " + request.key +" from: " + node.myPortNum + " with value: " + myValue);
		ReadResponse response = new ReadResponse(myValue, request.key, node.myPortNum);
		if (response.values == null) {
			response.values = new ValueClock(null, node.myPortNum , 0);
		}
		Client.sendMessage(response, request.responsePort, 10);
		if (myValue != null) {
			int uniqueKey = node.readKeyGetter.getAndIncrement();
			node.readMap.put(uniqueKey, ConcurrentHashMap.newKeySet());
			List<Integer> presedenceList =  node.myRing.getLocations(request.key);
			SimpleishRead readAsk = new SimpleishRead(request.key, node.myPortNum, uniqueKey);
			for (Integer dest : presedenceList) {
				Client.sendMessage(readAsk, dest, 10);
			}
			try{
				Thread.sleep(300);	
			}
			catch (InterruptedException ie) {
				ie.printStackTrace();
			}
			myValue = node.dataMap.get(request.key);
			SimpleWrite toWrite = new SimpleWrite(request.key, myValue, 0);
			for (SimpleReadResponse resp : node.readMap.get(uniqueKey)) {
				if(!myValue.same(resp.value)){
					Client.sendMessage(toWrite, resp.responsePort, 10);
				}
			}
		}
	}
	private void fowardRequest(ReadRequest request)
	{
		//put in retry info here
		request.fowarded = true;
		List<Integer> presedenceList =  node.myRing.getLocations(request.key);
		boolean sent = false;
		for (Integer dest : presedenceList) {
			sent = Client.sendMessage(request, dest, 10);
			System.out.println("sent to: " + dest + "sent is: " + sent);
			if (sent) {
				return;
			}
		}
		Client.sendMessage(new Response(true), request.responsePort);
	}
	private void proccessWrite(WriteRequest request)
	{
		List<Integer> presedenceList =  node.myRing.getLocations(request.key);
		//put in data versioning stuff here
		ValueClock clock;
		if (request.context == null) {
			//change when implimenting versioning
			clock = new ValueClock(request.value, node.myPortNum, node.counter.getAndIncrement());
			// node.dataMap.put(request.key, clock);
		}
		else {
			clock = request.context.values.maxClock(request.value, node.myPortNum, node.counter.getAndIncrement());
			//clock = request.context.values.resolveClocks(clock);
		}

		// Client.sendMessage(request, presedenceList.get(0));
		//change 0 to new port for responses if implimenting write response
		SimpleWrite toWrite = new SimpleWrite(request.key, clock, 0);
		this.proccessSimpleWrite(toWrite);
		Client.sendMessage(new WriteResponse(clock, request.key, node.myPortNum), request.responsePort);
		// System.out.println("at least I made it here");
		for (Integer location : presedenceList) {
			System.out.println("location:" + location + " myPortNum" + node.myPortNum + "equal:" + (location - node.myPortNum));
			if (!location.equals(node.myPortNum)) {
				System.out.println(location);
				Client.sendMessage(toWrite, location);
			}
			else{
				System.out.print("skipping me");
			}
		}

	}
}
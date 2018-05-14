import java.net.*;

import java.io.ObjectInputStream;
import java.io.IOException;
import java.util.List;
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
			case "WriteRequest":
				WriteRequest write = (WriteRequest) messageRecieved;
				if (checkIfMine(write)) {
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
			case "SimpleRead":
				SimpleRead query = (SimpleRead) messageRecieved;
				proccessSimpleRead(query);
			break;
			default:
					System.out.println("Error:Unrecognized message");	
			
		}
	}
	private void proccessSimpleRead(SimpleRead request)
	{
		Client.sendMessage(node.dataMap.get(request.key), request.responsePort);
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
		ReadResponse response = new ReadResponse(myValue, request.key, node.myPortNum);
		if (response.values = null) {
			response.values = new 	ValueClock(null, node.myPortNum , 1)
		}
		Client.sendMessage(response, request.responsePort);
		//put in read repair here
	}
	private void fowardRequest(ReadRequest request)
	{
		//put in retry info here
		request.fowarded = true;
		List<Integer> presedenceList =  node.myRing.getLocations(request.key);
		Client.sendMessage(request, presedenceList.get(0));
	}
	private void proccessWrite(WriteRequest request)
	{
		List<Integer> presedenceList =  node.myRing.getLocations(request.key);
		//put in data versioning stuff here
		ValueClock clock;
		if (request.context == null) {
			//change when implimenting versioning
			clock = new ValueClock(request.value, node.myPortNum, 1);
		}
		else {
			request.context.values.resolveValues(request.value, node.myPortNum, 1);
			clock = request.context.values;
		}
		Client.sendMessage(request, presedenceList.get(0));
		//change 0 to new port for responses if implimenting write response
		SimpleWrite toWrite = new SimpleWrite(request.key, clock, 0);
		for (Integer location : presedenceList) {
			Client.sendMessage(request, presedenceList.get(0));
		}
	}
}
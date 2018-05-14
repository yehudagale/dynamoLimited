import java.net.*;

import java.io.ObjectInputStream;
import java.io.IOException;
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
			case "ReadRequest":
				ReadRequest request = (ReadRequest) messageRecieved;
				if (checkIfMine(request)) {
					proccessRead(request);
				}
				else{
					fowardReadMessage(request);
				}
			break;
			default:
					System.out.println("Error:Unrecognized message");	
			
		}
	}
	private boolean checkIfMine(ReadRequest request)
	{
		if(request.fowarded)
		{
			return true;
		}
		else{
			//O(N) lookup consider improving
			return node.myRing.getLocations(request.key).contains(node.myPortNum);
		}
	}
	private void proccessRead(ReadRequest request)
	{

	}
	private void fowardReadMessage(ReadRequest request)
	{
		// Client.sendMessage(request, )
	}
}
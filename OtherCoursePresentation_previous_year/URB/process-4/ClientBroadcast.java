/* 
 * Client to send Message to another Process Server

*/

import java.io.*;
import java.net.*;

public class ClientBroadcast {

	private final int processNumber = 4;
	private Socket clientSocket;
	private int []message;
//	private int messageNumber;       // We can create message as an object and sent it by serialization method
//	private String messageProcess;   // Cretaed Process DNS for reference in Differnt Host,If it is localhost only then processNumber or Port can make difference in message
	private static int previousMN = 0;
	public FailureDetector fd;
	public ServerBroadcast bs;
	public Thread fdThread;
	public Thread bsThread;

	/*	public ClientBroadcast () {  
	//	clientSocket = new 
		fdThread = fd;
		try {
			bs = new ServerBroadcast() ;
		}
		
		catch( IOException ioe ) {
			ioe.printStackTrace();
		}
		bsThread = bs;
		
	} // Constructor END */ 
	public static void main( String []args ) {
		ClientBroadcast bc = new ClientBroadcast();	
		bc.instantiateClass();
		int option=1;
		//System.out.println("Please specify the current Procees Number<unique> : ");
		BufferedReader br = new BufferedReader( new InputStreamReader(System.in));
		//processNumber = Integer.parseInt( br.readLine() );
		while ( true ) {
			System.out.println("Select Option");
			System.out.println("1) Broadcast Message Size - 16 B");
			System.out.println("2) Broadcast Message Size - 10KB");
			System.out.println("3) Broadcast Message Size - 1MB");
			System.out.println("4) Broadcast Message Size - 10MB");
			try {
				option = Integer.parseInt(br.readLine());
			}
			catch(IOException ioe) {
				ioe.printStackTrace();
			}
			switch ( option ) {
				case 1:
					if(bc.broadcastClient(5))
						System.out.println("Main-1 : Successfully Broadcasted");
					else
						System.out.println("Main-1 : Failed to Broadcast");
					break;
				case 2:
					if(bc.broadcastClient(2500))   // 10 KB =10  * 250 * 4(int)
						System.out.println("Main-1 : Successfully Broadcasted");
					else
						System.out.println("Main-1 : Failed to Broadcast");
					break;
	
				case 3:
					if(bc.broadcastClient(250000))  // 1M  = 1000 * 250 * 4 
						System.out.println("Main-1 : Successfully Broadcasted");
					else
						System.out.println("Main-1 : Failed to Broadcast");
					break;
	
				case 4:
					if(bc.broadcastClient(2500000))
						System.out.println("Main-1 : Successfully Broadcasted");
					else
						System.out.println("Main-1 : Failed to Broadcast");
					break;
				default:
					System.out.println("You Did not select Correct option");
	
			}
		}

	}
	public boolean broadcastClient(int size)  {  // BROADCAST the Message from the Process to Others , This is the Message Starting Place
		if(size > 0) {
			message = new int[size];
			System.out.println("Size of Message selected -" + message.length);
			message[0] = 0 ;   //Encode  msg-0 , Ack -1 ,Alive-2 , Alive message Success-3 , Alive Message Failure-4    message type
			message[1] = size ;    // Message Size
			message[2] = processNumber ;    // identify the message who send
		}
		else {
			System.out.println("BroadCast-1 : Array Size is less than Zero");
		}  // END of Message Header
		for( int i=3 ; i < size ; i++ ) {		// Create an Encoded Message with Unique Number
			if ( i==3 ) {
				message[i] = ++previousMN ;
			}
			else {
				message[i] = -2;
			}			 
		}  //  END of Message Construction
		System.out.println("Message is Created");
		
		if ( sendMsgToNext() ) {
			System.out.println ("broadcastClient - 2: Message is successfully Sent ");
			return true;
		} // End of IF
		else {
			System.out.println("broadcastClient - 3: Failed to send Message");
			return false;
		}
		// Check the Perfect Failure Detector
		// Send to the Correct neighbour Process
		// Wait for Acknowledgement 
		// Got an Acknowledgment,then Deliver
		
	}  // END of Broadcast Function
	public boolean reBroadcast() {		// Re Broadcast
//		instantiateClass();
		return sendMsgToNext() ;

	}
	public boolean sendMsgToNext() {       // Send Message to next correct process
		instantiateClass();
//		if( isClientAlive() && isServerAlive( ) ) {
			System.out.println("Starting Next Correct process");
			int nextCorrectprocess = fd.nextCorrectProcess() ;
			System.out.println("Next Correct process is : " + nextCorrectprocess);
			if( nextCorrectprocess != -1 ) {
//				setMessage(2, nextCorrectprocess);   // It is not needed , Same process Number to identify All the process
				try {
					clientSocket = new Socket( bs.getDNS(nextCorrectprocess),bs.getServerPort(nextCorrectprocess));
					DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());
					int sizeOfFile = message[1];
					for ( int i=0 ; i< sizeOfFile ; i++ ) {
						dos.writeInt(message[i]);
					}
					
				}
				catch( Exception e ) {
					e.printStackTrace();
				}
				return true;
			}
			else
				return false;
			
/*		}
		else {
			System.out.println("Process - "+ processNumber+"is not Alive or Failured Detector is Failed<It is not possible>");
			return false;
		}*/
		
	}
	public boolean isClientAlive() {
		instantiateClass();
		Socket clSocket;
		int alive[]= new int[4];
		alive[0] = 2;
		alive[1] = 4 ; // SIZE of the packet
		alive[2] = processNumber ; 
		alive[3] = ++previousMN ;
//		FailureDetector fd = new FailureDetector(); 
		//fd.start();
		try {
			clSocket = new Socket(fd.getDNS( processNumber ),fd.getServerPort( processNumber));
			System.out.println("Check the Client Process - " + processNumber+" is Alive or Not ."); 
			DataOutputStream dos = new DataOutputStream( clSocket.getOutputStream() );
			for ( int i=0 ; i < 4 ; i++ ) {
				dos.writeInt( alive[i] );
			}
			DataInputStream dis = new DataInputStream( clSocket.getInputStream() );
			if ( dis.readInt() == 3 ) { // 3 - Alive Message is Succeded
				//clSocket.close();
				return true;
			}
			else {
				//clSocket.close();
				return false;
			}
		/*	if ( dis.readInt() == 3 ) { // 3 - Alive Message is Succeded
				dis.readInt();   // MSG size read..
				if( dis.readInt() == processNumber ) {
					if( dis.readInt() == previousMN ) {
						System.out.println("Client Process is Alive & Verified");
						clSocket.close();
						return true;
					}
					else {
						clSocket.close();
						return false;
					}
				}
				else {
					clSocket.close();
					return false;
				}
			}
			else {
				clSocket.close();
				return false;
			} */
		}
		catch(Exception e) {
			e.printStackTrace();
//			clSocket.close();
			return false;
		}
						
	}
	public boolean isServerAlive( ) {
		instantiateClass();
		if( fd.isServerAlive() ) {
			System.out.println("Current Process Server is Alive");
			return true;
		}
		else return false;

	}
	public void setMessage(int index, int number ) {
		message[ index ] = number ;
	}
	
	public int getMessage( int index ) {    // index shoud be from 0 to Size-1 (i.e Array Index)
		
		return message[ index ] ;
	}
	public void instantiateClass() {
		fd = new FailureDetector();
		bs = new ServerBroadcast();
	}
	public void allocateArray(int size) {
		message = new int[size];
	}
}

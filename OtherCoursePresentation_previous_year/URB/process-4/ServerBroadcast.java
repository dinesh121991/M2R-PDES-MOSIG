/* Sever Process to capture message and Perfect Failure Detector will check the ClientBroadcast is working or not */

import java.io.*;
import java.net.*;

public class ServerBroadcast extends Thread {

	private final int processNumber= 4;
	private ServerSocket serverSocket;
	private static boolean ssI=false;
//	private int failedProcess[];
	private ClientBroadcast bc;
//	private FailureDetector fd;
	private static int []waitingMessage;
	 private final String []DNSList={"localhost","localhost","localhost","localhost","localhost"};
        private final int []serverPortList = {5000,6000,7000,8000,9000};
    private static int count=0;  //   
/*	public ServerBroadcast ( ) throws IOException {
		waitingMessage = new int[100];
//		serverSocket = new ServerSocket(getServerPort(processNumber));
//		bc = new ClientBroadcast();
//		fd = new FailureDetector();
		
	} // END of Constructor */
	public void run() {
		while( true ) {
			try {
				if( ssI == false ) {
					instantiateClass();
					serverSocket = new ServerSocket(getServerPort(processNumber));
					ssI = true ;
				}
				System.out.println("ServerBroadcast:run-Waiting for Client to connect on Port"+ serverSocket.getLocalPort()+ ".....") ;
				Socket server = serverSocket.accept() ;   // return Client connected socket Port 
	  		        System.out.println("ServerBroarcast:run-Just connected to " + server.getRemoteSocketAddress());
			        DataInputStream in = new DataInputStream(server.getInputStream());
			    int msg[] = new int[4];
			    msg[0] = in.readInt();
			    msg[1] = in.readInt();
			    msg[2] = in.readInt();
			    msg[3] = in.readInt();
			    bc.allocateArray(msg[1]);
				bc.setMessage(0 , msg[0]) ;    // Msg -Type
				bc.setMessage(1 , msg[1]) ;//MSG size
				bc.setMessage(2 , msg[2]) ;	//processNumber Who sends
				bc.setMessage(3, msg[3]) ;	//messageNumber
				if (bc.getMessage(0) == 0 ) {  // if it is msg-0 then we have to rebroadcast
		count = count + 1;
					if( (( bc.getMessage(2) + 2 ) % 4 )+1  == processNumber ) {
						System.out.println("The message - "+
								bc.getMessage(3)+" From the Process - " + bc.getMessage(2)+"was Delivered Successfully in - "+processNumber);
						bc.setMessage(0 , 1) ;  // Assign the Message type is ACK
						bc.setMessage(1 , msg[1]) ;//MSG size
						bc.setMessage(2 , msg[2]) ;	//processNumber Who sends
						bc.setMessage(3, msg[3]) ;	//messageNumber
						if ( bc.sendMsgToNext() ) {
							System.out.println ("broadcastClient - 2: Message is successfully Sent ");
							
						} // End of IF
						else {
							System.out.println("broadcastClient - 3: Failed to send Message");
						
						}
					}
					else {
						for(  int i=4 ; i< bc.getMessage(1) ; i++ ) 
							bc.setMessage(i , in.readInt()) ; // Copy the Message 
						//bc.reBroadcast();  // it will rebroadcast from the Current process
						if ( bc.reBroadcast() ) {
							System.out.println ("broadcastClient - 2: Message is successfully Sent ");
						
						} // End of IF
						else {
	/*						bc.setMessage(0 , 1) ;
							bc.setMessage(1, bc.get);
							bc.setMessage(2, number);
							bc.setMessage(3, number); */
							System.out.println("broadcastClient - 3: Failed to send Message");
						
						}
					}
	
				} //Rebroadcast END block
				else if ( bc.getMessage(0) == 1 ) {        // ACK-1 then we have to compare the value with Correct message waiting and deliver
//					count = count + 1;
					if ( ((bc.getMessage(2)+1)%4 +1) == processNumber ) {
						System.out.println("The message - "+
								bc.getMessage(3)+" From the Process - " + bc.getMessage(2)+"was Delivered Successfully in - "+processNumber);
						System.out.println("The Message is finally Reached end of the Process");
					}
					else {
						System.out.println("The message - "+
								bc.getMessage(3)+" From the Process - " + bc.getMessage(2)+"was received Successfully in - "+processNumber);
						if(bc.reBroadcast()) {
							System.out.println("The message is successfully rebroadcated");
						}
					}
					int arrayIndex = (bc.getMessage(2) - 1) % 100 ;
					waitingMessage[ arrayIndex ] = 0 ;
					System.out.println("Message -" + bc.getMessage(2) + " is delivered successfully in "+ processNumber);
				}
				else if ( bc.getMessage(0) == 2 ){               // Alive
					DataOutputStream dos = new DataOutputStream(server.getOutputStream()) ; 
					dos.writeInt(3);
			/*		dos.writeInt(bc.getMessage(1));
					dos.writeInt(bc.getMessage(2));
					dos.writeInt(bc.getMessage(3));*/
	
				}
				else {
					System.out.println("Server receive only MSG,ACK and ALIVE , But I could not undertand message type");
				}
			       /* System.out.println(in.readUTF());
			        DataOutputStream out = new DataOutputStream(server.getOutputStream());
			        out.writeUTF("Thank you for connecting to " + server.getLocalSocketAddress() + "\nGoodbye!"); */
				//server.close();
				//serverSocket.close();  // Never Close this one
	
	
			}  // try END
			catch ( SocketTimeoutException toe ) {
				System.out.println("ServerBroadcast - run - 1 : Socket Time out Exception ");
				break ;
				
			}
			catch ( IOException ioe ) {
				System.out.println("ServerBroadcast - run - 2 : IO Exception - Start print Stacktrace");
				ioe.printStackTrace();
				break ;
			}
			catch (Exception e) {
				System.out.println("SevereBroadcast - run - 3 : Finally Block ");
				e.printStackTrace();
				break ;
			}  // CATCH and FINALLY END
		} // WHILE END

	}  // TREAD Logic
	public boolean waitForAck ( int messageNumber ) {  // Wait for Ack and Confirm the Acknowledgment is same as Client Broadcast message Number
		int arrayIndex = ( messageNumber - 1 )%100 ;
		if( waitingMessage[arrayIndex] == 0 ){
			waitingMessage[arrayIndex] = messageNumber;
			System.out.println("WaitForAck is assigned successfully ") ;
			return true;
		}
		else
			return false;

	}  // waitForAck  END
	public String getDNS( int processNumber ) {
		return DNSList [ processNumber - 1 ] ;
	}
	public int getServerPort( int processNumber ) {
		return serverPortList[ processNumber - 1 ] ;
	}
	
	
	 public static void main(String [] args)
	   {
	      try
	      {
	         Thread t = new ServerBroadcast();
	         t.start();
	      }catch(Exception e)
	      {
	         e.printStackTrace();
	      }
	   }
	 public void instantiateClass() {
			
			bc = new ClientBroadcast();
		}
	 public int numberOfMessagesReceived() {
		 return count;
	 }
}  // END of Class
 

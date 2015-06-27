/***************************************************************************************************************************
 *  This is to Check the ClientBroadcast and BroadcastServer running fine and Update the Table maintained Failed Processes
 *

****************************************************************************************************************************/

import java.io.*;
import java.net.*;

public class FailureDetector extends Thread{
	
	private static final int processNumber = 4 ;
	private ServerBroadcast bs ;
	public Thread bsThread;
	private ClientBroadcast bc;
	private static boolean ssI=false;
	private static int previousMN = 0;
	private Socket socketServerPFD;        // Server Process is running Correctly is checked by PFD client Socket
	private ServerSocket socketClientPFD;  // Client Process is running correctly is checked by PFD(Perfect Failure Detector) ServerSocket
//	private static int []FailedProcess;		// Failed Process List
//	private final int numberProcessInURB = 1;     // Number of Process in the Uniform Reliable Broadcast (URB)
	private final String []DNSList={"localhost","localhost","localhost","localhost","localhost"};
    private final int []serverPortList = {5001,6001,7001,8001,9001};   // PFD server Socket
    private boolean instantiation=false;

/*	public FailureDetector() {
	/*	try {
//			socketClientPFD = new ServerSocket( serverPortList[processNumber - 1]) ;
			bs = new ServerBroadcast();
			bsThread = bs ;
		}
		catch(IOException ioe) {
			System.out.println("FAILURE DETECTOR - 1: socketClientPFD - Does not create correctly");
			ioe.printStackTrace();
		}
		finally {
			System.out.println("FAILURE DETECTOR - 2: socketClientPFD - Does not create correctly");
		}
	} */
	public void run() {
		instantiateClass();
		while( true ) {
		    try {
					if( ssI == false ) {
						
						socketClientPFD = new ServerSocket( serverPortList[processNumber - 1]) ;
						ssI = true;
					}
		          System.out.println("FailureDetector:run-Waiting for Client to connect on Port"+ socketClientPFD.getLocalPort()+ ".....") ;
		          socketServerPFD = socketClientPFD.accept() ;   // return Client connected socket Port 
		          System.out.println("FailureDetector:run-Just connected to " + socketServerPFD.getRemoteSocketAddress());
		          DataInputStream in = new DataInputStream(socketServerPFD.getInputStream());
		          int msg[] = new int[2];
		          msg[0] = in.readInt();
		          msg[1] = in.readInt();
		          bc.instantiateClass();   // Nothing will affect
		          bc.allocateArray(msg[1]);
		          System.out.println("Why-1");
		          bc.setMessage( 0 , msg[0]);    // Msg -Type Alive for Client process right now
		          bc.setMessage( 1 , msg[1]); // Size of the MSG
		          bc.setMessage( 2 , in.readInt());  // process number
		          bc.setMessage( 3 , in.readInt());  // MSG Number
				if ( bc.getMessage(0) == 2 ) {
					System.out.println("bc.getMessage(0) == 2");
					if(bc.getMessage(2) != processNumber) {
						System.out.println("bc.getMessage(2) != processNumber");
						if( bc.isServerAlive()) {
							System.out.println("bc.isClientAlive() && bc.isServerAlive()");
							bc.setMessage(0 , 3);
						}
						else {
							System.out.println("else:bc.isClientAlive() && bc.isServerAlive()");
							bc.setMessage(0 , 4);   // 4-for failure in Alive message
						}
					}
					else {
						System.out.println("else :bc.getMessage(2) != processNumber");
						bc.setMessage(0 , 3);     // Current process is Alive ,we checked already and this will not be reached logically
					}
					
				}
				System.out.println("Reaching out");
				DataOutputStream out = new DataOutputStream( socketServerPFD.getOutputStream());
//				for ( int i=0 ; i< bc.getMessage(1) ; i++ )
					out.writeInt(bc.getMessage(0)) ;
	            //socketClientPFD.close();   // Never Close Server Socket
	            //socketServerPFD.close();
	        }  // try END
	
			catch ( SocketTimeoutException toe ) {
				System.out.println("FailureDetector:run- 1 : Socket Time out Exception ");
				break ;
				
			}
			catch ( IOException ioe ) {
				System.out.println("FailureDetector:run- 2 : IO Exception - Start print Stacktrace");
				ioe.printStackTrace();
				break ;
			}
			catch (Exception e) {
				System.out.println("FailureDetector:run- 3 : Finally Block ");
				e.printStackTrace();
				break ;
			}
		}


	}

/*	public boolean checkCurrentProcess() {	// Check Current Process(Client and Server) is working Correctly

	}
	public int[] failedProcess(int processNumb) {		// Returns list of failed process in the Group

	} */
	public String getDNS( int processNumb ) {    // process Number is from 1 to 4 not like an array index
		return DNSList[processNumb -1 ] ;
	}
	public int getServerPort ( int processNumb ) {
		return serverPortList[ processNumb -1 ] ;
	}
	public boolean isServerAlive() {
		instantiateClass();
		Socket server;
		try {
//			bsThread.start();
			System.out.println("Why-2");
			server = new Socket( bs.getDNS(processNumber),bs.getServerPort( processNumber )) ;   // This is modified
			DataOutputStream dos = new DataOutputStream ( server.getOutputStream() ) ;
			int []message = new int[4];
			message[0] = 2;
			message[1] = 4;
			message[2] = processNumber;
			message[3] = ++previousMN;
			for ( int i=0 ; i< message[1] ; i++ ) {
				dos.writeInt( message[i] );
			}
			System.out.println("FD-servere checkup - Client is created");
			DataInputStream dis = new DataInputStream( server.getInputStream() );
			System.out.println("FD-servere checkup - Client is created");
			if( dis.readInt() == 3 ) {
				System.out.println("Server Alive : dis.readInt() == 3");
		//		server.close();
				return true;
			}
	/*			dis.readInt(); //MSG Size
				if( dis.readInt() == processNumber ) {
					if( dis.readInt() == previousMN ) {
						server.close();
						return true;
					} 
					else {
						server.close();
						return false;
					}
				
				}
				else {
					server.close();
					return false;
				}
	
			} */
			else {
				System.out.println("Server Alive Else : dis.readInt() == 3");
	//			server.close();
				return false;
			}
			} // TRY END BLOCK
		catch(IOException ioe){
			System.out.println("FD-servere checkup - Client is created");
			//server.close();
			ioe.printStackTrace();
			return false;
		}

		
	}

	public int nextCorrectProcess() {
//		instantiateClass();
		Socket server;
		System.out.println("Start of nextCorrectprocess - 1");
		int processNumb = processNumber ; 
		int pn;
		int []message = new int[4] ;
		message[0] = 2 ; // Alive message -2
		message[1] = 4 ;
		message[2] = processNumber ; 
		message[3] = ++previousMN; 
		System.out.println("Start of nextCorrectprocess - 2");
	//	int pn = ((++processNumb) % 4)+1 ) ; 
		while ( (pn = ((processNumb++) % 4)+1 ) != processNumber ) {
			System.out.println("Start of nextCorrectprocess - 3"+pn);	
			try {			
				server = new Socket( getDNS( pn ), getServerPort( pn ));
				System.out.println("Start of nextCorrectprocess - 4");
				DataOutputStream dos = new DataOutputStream(server.getOutputStream() );
				for( int i=0 ; i< message[1] ; i++ ) {
					dos.writeInt(message[i]);
				}
				System.out.println("Start of nextCorrectprocess - 5");
				//socketServerPFD = socketClientPFD.accept();   // it is not needed ...
				DataInputStream dis = new DataInputStream(server.getInputStream());
				if(dis.readInt() == 3) {
					//server.close();
					return processNumb;
			/*		dis.readInt();
					if( dis.readInt() == processNumber ) {
						System.out.println("Start of nextCorrectprocess - 6");
						server.close();
						return processNumb;
					}
					else {
						server.close();
						System.out.println("Link is not Reliable <but it is not possible>");
					} */
				} 
					
				else {
				//	server.close();
					continue;
				}
					
			}
			catch ( Exception e ) {
				System.out.println("The process - " + processNumb + " is not Alive,So please check ");
				//server.colse();
				continue;
				//e.printStackTrace();
			}
		}
			System.out.println("Current Process is the only Alive Process,So we are delivering only in our process only");
			//server.close();
			return -1;
			
	}
	
	 public static void main(String [] args)
	   {
	      try
	      {
	    	 System.out.println("Starting Failure Detector");
	         Thread t = new FailureDetector();
	         t.start();
	      }catch(Exception e)
	      {
	         e.printStackTrace();
	      }
	   }
	 public void instantiateClass() {
		 	if ( instantiation == false ) {
		 		bs = new ServerBroadcast();
				bc = new ClientBroadcast();
				instantiation = true;
		 	}			
		}


}  // END of FailureDetector class

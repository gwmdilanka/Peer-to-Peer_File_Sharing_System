

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.parsers.ParserConfigurationException;

import org.hsqldb.jdbc.JDBCDataSource;
import org.xml.sax.SAXException;

/** 
 * @author Madushani Dilanka
 *
 */
public class ServerThread extends Thread {

	DataInputStream dis;
	DataOutputStream dos;

	Socket remoteClient;
	Server server;

	String userName;
	String privateUser;

	int downloadPort;	
	
	ArrayList<ServerThread> connectedClients; // keep track of all the other
											// clients connected to the
												// Server
	
	//To receiving file
	int bytesRead;
    int current = 0;
    FileOutputStream fos = null;
    BufferedOutputStream bos = null;  	
	ObjectInputStream inputStream = null;
	private String searchIpAddress;	
	ObjectInputStream ois = null;	
	
	private Timer timer = new Timer();
	private int delay = 5000; // delay for 20 sec.
    private int period = 20000; // repeat every 10 sec.
    
    private boolean exists = false;
    
	/**
	 * @param remoteClient
	 *            - the client that uses the service
	 * @param server
	 *            - the service that the client needs
	 * @param connectedClients
	 *            - clients that are currently connected to the server
	 */
	public ServerThread(Socket remoteClient, Server server,
			ArrayList<ServerThread> connectedClients) {
		this.remoteClient = remoteClient;
		this.connectedClients = connectedClients;
		//Server Ping to clients
		timer.schedule(new RemindTask(), delay,period);
		try {
			this.dis = new DataInputStream(remoteClient.getInputStream());
			this.dos = new DataOutputStream(remoteClient.getOutputStream());		
			this.server = server;					
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {

		while (true) // main protocol decode loop
		{			
			try {    
				int mesgType = dis.readInt();		
				
				// decode the message type based on the integer sent from the
				// client
				switch (mesgType) {
				
				//File donwloanPort number
				case ServerConstants.FILE_PORT:
					downloadPort = dis.readInt();					
					break;
				
				// Message can be seen by all online clients
				case ServerConstants.CHAT_BROADCAST:
					String data = dis.readUTF();
					server.getSystemLog().append(
							remoteClient.getInetAddress() + ":"+ remoteClient.getPort() + ">" + data+ "\n");

					for (ServerThread otherClient : connectedClients) {
						// don't send the message to the client that sent the
						// message in the first place
						if (!otherClient.equals(this)) {
							otherClient.getDos().writeInt(ServerConstants.CHAT_BROADCAST);
							otherClient.getDos().writeUTF(data);
						}
					}
					break;

				// Message can only be seen by the selected client
				case ServerConstants.PRIVATE_MESSAGE:
					
					int privateClient = dis.readInt();
					String chatDisplay = dis.readUTF();
					String serverDisplay = dis.readUTF();

					server.getSystemLog().append(remoteClient.getInetAddress() + ":"+ remoteClient.getPort() + ">"+ serverDisplay + "\n");
					connectedClients.get(privateClient).getDos().writeInt(ServerConstants.PRIVATE_MESSAGE);
					connectedClients.get(privateClient).getDos().writeUTF("Private message from " + chatDisplay+ "\n");
					break;

				// Online clients will be informed if a client has just joined
				case ServerConstants.REGISTER_BROADCAST:
					String login = dis.readUTF();
					for (ServerThread otherClient : connectedClients) {
						// don't send the message to the client that sent the
						// message in the first place
						if (!otherClient.equals(this)) {
							otherClient.getDos().writeInt(ServerConstants.REGISTER_BROADCAST);
							otherClient.getDos().writeUTF(login);
						}
					}
					break;

				// Online clients are shown
				case ServerConstants.CLIENT_JOIN:
					userName = dis.readUTF();

					server.getSystemLog().append(
							remoteClient.getInetAddress() + ":"	+ remoteClient.getPort() + ">" + userName+ " has joined\n");
					for (ServerThread otherClient : connectedClients) {						
						otherClient.getDos().writeInt(ServerConstants.CLIENT_JOIN);
						otherClient.getDos().writeInt(connectedClients.size());
						for (ServerThread oc : connectedClients) {							
							otherClient.getDos().writeUTF(oc.userName);							
							otherClient.getDos().writeUTF(oc.remoteClient.getInetAddress().getCanonicalHostName()); // does this really work?
							otherClient.getDos().writeInt(oc.downloadPort);
							otherClient.getDos().writeUTF(oc.userName);							
						}						
					}
					break;

				// Client decided to close the chat
				case ServerConstants.CLIENT_LEAVE:
					String exitClient = dis.readUTF();
					String inetAddress = remoteClient.getInetAddress().toString();
					
					int exitClientPort = remoteClient.getPort();
					
					System.out.println("Exit client port: " + exitClientPort);
					// Server will be informed if a client has just left
					server.getSystemLog().append(
							remoteClient.getInetAddress() + ":"	+ remoteClient.getPort() + ">" + exitClient	+ " has left\n");

					for (ServerThread otherClient : connectedClients) {
						// don't send the message to the client that exited the
						// chat
						if (!otherClient.equals(this)) {
							otherClient.getDos().writeInt(ServerConstants.CLIENT_LEAVE);
							otherClient.getDos().writeUTF(exitClient + " has left");
							otherClient.getDos().writeInt(connectedClients.size());
							otherClient.getDos().writeUTF(exitClient);
						}
					}					
					
					JDBCDataSource removeDs = new JDBCDataSource();

					removeDs.setUrl("jdbc:hsqldb:hsql://localhost/");
					// set other data source properties
					removeDs.setPassword("");
					removeDs.setUser("SA");

					// setup connection and query
					Connection removeConn = removeDs.getConnection();
					Statement removeStatement = removeConn.createStatement();

					// send query to database
					ResultSet removeRs = removeStatement.executeQuery("select * from Files");
					removeRs = removeStatement.executeQuery("delete from files where PORT_NUMBER='"+exitClientPort+"'");
					
					ResultSetMetaData removeRsmd = removeRs.getMetaData();

					// close the database resources
					removeRs.close();
					removeStatement.close();
					removeConn.close();
					
					timer.cancel(); //cancel the server ping when client leave from the chat
					connectedClients.remove(this);
					break;
					
				case ServerConstants.CLIENT_PUBLISH:
					
					String receivedFileName = dis.readUTF();
					String client = dis.readUTF();					
					server.getSystemLog().append(remoteClient.getInetAddress()+ ":"+ remoteClient.getPort() +">"+client +"> File Name "+ receivedFileName +"\n");
					
					//save to database
					JDBCDataSource ds = new JDBCDataSource();
					
					ds.setUrl("jdbc:hsqldb:hsql://localhost/");
					// set other data source properties
					ds.setPassword("");
					ds.setUser("SA");

					// setup connection and query
					Connection conn = ds.getConnection();
					Statement statement = conn.createStatement();

					// create the database table
					statement.execute("create table if not exists Files(file_id int GENERATED BY DEFAULT AS IDENTITY (START WITH 1, INCREMENT BY 1) NOT NULL, file_Name varchar(50), inet_address varchar(50), port_number varchar(50), blurb varchar(250), PRIMARY KEY (file_id))");
					
					// create a bulk insert statement
					String insertStatements = "insert into Files (FILE_NAME, INET_ADDRESS,PORT_NUMBER, BLURB) " +
							"values ('"+receivedFileName+"','"+remoteClient.getInetAddress()+"','"+remoteClient.getPort()+"','blah blah blah');";
					
					// batch all updates to db at once - though we would normally do one at a time
					statement.executeUpdate(insertStatements,Statement.RETURN_GENERATED_KEYS);//generate auto generate number in primary key

					// send query to database
					ResultSet rs = statement.executeQuery("select * from Files");
					ResultSetMetaData rsmd = rs.getMetaData();

					int cols = rsmd.getColumnCount();
					// iterate through the results
					while(rs.next())
					{
						for(int i=1 ; i<cols; i++ )
						{
							System.out.println(rs.getString(i));
						}
						
					}
					// close the database resources
					rs.close();
					statement.close();
					conn.close();	
					break;
					
				case ServerConstants.CLIENT_SEARCH:
					//Search file from database					
					String searchingFile = dis.readUTF();

					JDBCDataSource dsearch = new JDBCDataSource();
					dsearch.setUrl("jdbc:hsqldb:hsql://localhost/");
					// set other data source properties
					dsearch.setPassword("");
					dsearch.setUser("SA");

					// setup connection and query
					Connection newconn = dsearch.getConnection();
					Statement newstatement = newconn.createStatement();
					// send query to database
					ResultSet newrs = newstatement.executeQuery("select * from Files");
					ResultSetMetaData newrsmd = newrs.getMetaData();

					int newcols = newrsmd.getColumnCount();
					String searchClient = dis.readUTF();
					// iterate through the results
					while(newrs.next())
					{
						for(int i=1 ; i<newcols; i++ )
						{
							if(newrs.getString(i).equals(searchingFile))
							{							
							String searchInetAddress = newrs.getString(i+1);	
							String searchPortNumber = newrs.getString(i+2);				
							searchIpAddress = searchInetAddress+":"+searchPortNumber;							
							System.out.println(searchIpAddress);					
												
							server.getSystemLog().append(remoteClient.getInetAddress() +":"+ remoteClient.getPort() + ">"+searchClient+" searched IP address is"+ searchIpAddress + "\n");
				  			dos.writeInt(ServerConstants.CLIENT_SEARCH);
				  			dos.writeUTF(searchIpAddress);						
				            dos.flush();							
							}
							else
							{
								System.out.println("not match with col "+ i);
							}
						}						
					}
					
					// close the database resources
					newrs.close();
					newstatement.close();
					newconn.close();				
				
				case ServerConstants.CLIENT_PING:
					String clientPingMsg = dis.readUTF();
					String pingClient = dis.readUTF();
					server.getSystemLog().append(remoteClient.getInetAddress()+ ":"+ remoteClient.getPort() + "> "+pingClient+" >" +clientPingMsg + "\n");
					break;
				
				}
			}
			catch (NullPointerException ex){
				System.err.println("Did not enter file name to search");
			}
			catch (Exception e) {
				System.err.println("Client exited");
				//e.printStackTrace();
				return;
			}
		}
	}		
	
	//Ping message method to client from the server every 20 sec
	class RemindTask extends TimerTask {
	    public void run() {
	    	try{
	      System.out.println("Server Ping!");
	      dos.writeInt(ServerConstants.SERVER_PING);
	      dos.writeUTF("Ping from Server");
	      dos.flush();
			
	    	}catch (Exception e){
	    		//e.printStackTrace();
	    		System.out.println("Client exited");
	    		
	    	}	    
	    }
	  }
	/**
	 * @return
	 */
	public DataOutputStream getDos() {
		return dos;
	}
}

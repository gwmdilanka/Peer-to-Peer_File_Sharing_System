
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;

import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

/**
 * This Client.Class is for the clients' chat interface
 * 
 * @author @author Madushani Dilanka
 *
 */
public class Client extends JFrame implements ActionListener, Runnable {
	private static final long serialVersionUID = 980389841528802556L;

	// define a thread to take care of messages sent from the server
	Thread clientThread = new Thread(this);

	// Chat Interface
	JTextField chatInput = new JTextField(30);
	JTextArea chatHistory = new JTextArea(5, 30);
	JButton btnSendMessage = new JButton("Send");
	JLabel chatHeaderLbl = new JLabel("Active chat:");
	JButton searchBtn = new JButton("Search File");
	JButton pingBtn = new JButton("Ping");	

	// Register client
	JLabel nameLabel = new JLabel("Please Enter Name: ");
	JTextField lblClientName = new JTextField(30);
	JButton registerBtn = new JButton("Register");
	String clientName = "";
	int clientPort =0;
	Icon icon = null;
	Component frame = null;
	Object[] possibilities = null;

	// define the socket and input streams
	Socket client;
	DataInputStream dis;
	DataOutputStream dos;
	
	//To downloading
	DataInputStream ddis;
	DataOutputStream ddos;
	
	//To sending
	DataInputStream rdis;
	DataOutputStream rdos;
	
	// Clients List
	DefaultListModel listModel;
	JList clientList;
	
	JLabel onlineHeaderLbl = new JLabel("Online Users:");
	private final JButton btnExit = new JButton("Exit");

	boolean processing = true;

	// Select files from the directory
	JFileChooser chooser = new JFileChooser();
	
	FileInputStream fis = null;
	BufferedInputStream bis = null;
	OutputStream os = null;
		
	private static  JPanel mainChatPanel;
	int port;	
	
	ServerSocket downloadSocket; 
	int downloadport;
	Thread downloadThread;
	Socket requestSocket;
	String selectSeverClient;
	
	int bytesRead;
    int current = 0;
    FileOutputStream fos = null;
    BufferedOutputStream bos = null;
    
   	/**
	 * Initialises the user interface
	 */
	public Client() {	    
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setBackground(Color.GRAY);
		setForeground(Color.GRAY);
		// create the user interface and setup an action listener linked to the
		// send message button
		Container contentPane = this.getContentPane();
		contentPane.setLayout(new BorderLayout());

		// Add item to the list
		listModel = new DefaultListModel();
		clientList = new JList(listModel);
		clientList.setSelectionBackground(new Color(255, 255, 204));
		clientList.setSelectionForeground(Color.RED);
		clientList.setForeground(Color.WHITE);
		clientList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		clientList.setBackground(Color.GRAY);

		// Initialises Chat Panel
		JPanel chatPanel = new JPanel();
		JPanel historyPanel = new JPanel();
		JPanel inputPanel = new JPanel();
		inputPanel.setBackground(Color.GRAY);
		mainChatPanel = new JPanel();

		historyPanel.setLayout(new BorderLayout());
		mainChatPanel.setLayout(new BorderLayout());
		chatPanel.setLayout(new BorderLayout());
		chatHeaderLbl.setFont(new Font("Tahoma", Font.PLAIN, 12));
		chatHeaderLbl.setForeground(Color.BLACK);
		chatHeaderLbl.setBackground(Color.GRAY);

		// Header tags
		chatHeaderLbl.setHorizontalAlignment(SwingConstants.CENTER);
		onlineHeaderLbl.setFont(new Font("Tahoma", Font.PLAIN, 12));
		onlineHeaderLbl.setForeground(Color.BLACK);
		onlineHeaderLbl.setHorizontalAlignment(SwingConstants.CENTER);

		// Chat text area and buttons
		inputPanel.add(chatInput, BorderLayout.CENTER);
		btnSendMessage.setFont(new Font("Tahoma", Font.BOLD, 12));
		btnSendMessage.setBackground(new Color(0, 128, 0));
		btnSendMessage.setForeground(Color.WHITE);
		inputPanel.add(btnSendMessage, BorderLayout.EAST);
		searchBtn.setFont(new Font("Tahoma", Font.BOLD, 12));
		searchBtn.setForeground(Color.WHITE);
		searchBtn.setBackground(Color.BLUE);
		inputPanel.add(searchBtn);
		pingBtn.setFont(new Font("Tahoma", Font.BOLD, 12));
		pingBtn.setForeground(Color.WHITE);
		pingBtn.setBackground(Color.MAGENTA);
		inputPanel.add(pingBtn);				
		btnExit.setFont(new Font("Tahoma", Font.BOLD, 12));
		btnExit.setForeground(Color.WHITE);
		btnExit.setBackground(Color.RED);
		inputPanel.add(btnExit);

		historyPanel.add(chatHeaderLbl, BorderLayout.NORTH);
		chatHistory.setEditable(false);
		historyPanel.add(new JScrollPane(chatHistory), BorderLayout.CENTER);
		historyPanel.add(inputPanel, BorderLayout.SOUTH);

		chatPanel.add(historyPanel, BorderLayout.CENTER);
		mainChatPanel.add(chatPanel, BorderLayout.CENTER);

		mainChatPanel.add(chatPanel);
		// Initialises Online Panel
		JPanel membersPanel = new JPanel();

		// Setting the buttons' properties and listener
		btnSendMessage.setPreferredSize(new Dimension(70, 25));
		btnSendMessage.setActionCommand("send");
		btnSendMessage.addActionListener(this);

		// searchBtn.setPreferredSize(new Dimension(70, 25));
		searchBtn.setActionCommand("search");
		searchBtn.addActionListener(this);

		pingBtn.setPreferredSize(new Dimension(70, 25));
		pingBtn.setActionCommand("ping");
		pingBtn.addActionListener(this);
				
		btnExit.addActionListener(this);
		btnExit.setPreferredSize(new Dimension(70, 25));
		btnExit.setActionCommand("exit");
		btnExit.addActionListener(this);

		membersPanel.setLayout(new BorderLayout());
		membersPanel.add(onlineHeaderLbl, BorderLayout.NORTH);
		JScrollPane scrollPane = new JScrollPane(clientList);
		membersPanel.add(scrollPane, BorderLayout.CENTER);

		JPanel p = new JPanel(new BorderLayout());
		p.add(mainChatPanel, BorderLayout.WEST);
		p.add(membersPanel, BorderLayout.CENTER);

		contentPane.add(p, BorderLayout.CENTER);

		contentPane.setVisible(true);

		// attempt to connect to the defined remote host
		try {
			client = new Socket("localhost", 5000);
			dis = new DataInputStream(client.getInputStream());
			dos = new DataOutputStream(client.getOutputStream());			
			
			// Sets the user name of the thread
			clientThread.setName(clientName);			
			clientThread.start();
			
			downloadSocket = new ServerSocket(0);//0 used to create automatic port numbers every time run the program
															  
			//Check auto generated port number;			
			downloadport = downloadSocket.getLocalPort();
			System.out.println("Download Socket port number " + downloadport);			
			
			dos.writeInt(ServerConstants.FILE_PORT); //register download port to the server thread
			dos.writeInt(downloadport);		
			
			downloadThread = new Thread()
			{
				@Override
				public void run() {
					while(true)
					{
						try {
							Socket downloadClient = downloadSocket.accept();

							// defiine new IO streams different names
							ddis = new DataInputStream(downloadClient.getInputStream());
							ddos = new DataOutputStream(downloadClient.getOutputStream());							
										
							// read byte array from file to send to other client
							int mesgType = ddis.readInt();
							switch(mesgType){
							case ServerConstants.REMOTE_FETCH:
															
								System.err.println("Name of the current client "+clientName);
								
								int user = ddis.readInt();
								String fileRequestClient = ddis.readUTF();								
								String requestFile = ddis.readUTF();
								
								int fetchResult = JOptionPane.showConfirmDialog(
										mainChatPanel,
										"You have a file request from "
												+ fileRequestClient
												+ ". Do you have "
												+ requestFile + "?",
										"Remote File Fetch", JOptionPane.YES_NO_OPTION);
								if(fetchResult==JOptionPane.YES_OPTION){
								ddos.writeInt(ServerConstants.REMOTE_FETCH_SUCCESS);								
								chatHistory.append(fileRequestClient+":> "+requestFile+" file is requested"+"\n");
								System.out.println("message from send client btn "+requestFile);
								System.out.println("Selected user no: "+ user);
								System.out.println("Requesting client name " + fileRequestClient);
								
								//Sending file to the remote client
								JFileChooser chooser = new JFileChooser();
								int returnVal = chooser.showOpenDialog(contentPane);
								if (returnVal == JFileChooser.APPROVE_OPTION) {
								File myFile = new File (chooser.getSelectedFile().getAbsolutePath());
								
								byte [] mybytearray  = new byte [(int)myFile.length()];			          
								 
								fis = new FileInputStream(myFile);
								bis = new BufferedInputStream(fis);			          
								  
								bis.read(mybytearray,0,mybytearray.length);
								ddos.writeInt((int)myFile.length());//Size of the file
								ddos.writeUTF(chooser.getSelectedFile().getName());//Selected file name
								  
								OutputStream os = downloadClient.getOutputStream();
								System.out.println("Sending " + chooser.getSelectedFile().getAbsolutePath() + "(" + mybytearray.length + " bytes)");
								  
								os.write(mybytearray, 0, mybytearray.length);								  
								os.flush();
								System.out.println("Done.");
								
								if (bis != null) bis.close();	
								}
								chatHistory.append(clientName+":> "+ddis.readUTF()+"\n");//Received success message from download party
								}
								else{
									ddos.writeInt(ServerConstants.REMOTE_FETCH_ERROR);
									ddos.writeUTF(clientName+":> "+"File is not available!");															
								}							 
							break;							
							}												
						}catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}	
					}			
				}				
			};
			
			//Start the downloading thread
			downloadThread.start();	
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Change the OK button text to Register
		UIManager.put("OptionPane.okButtonText", "Register");

		// Register client window
		clientName = (String) JOptionPane.showInputDialog(frame,
				"Please Enter name: ", "Welcome CHAT SERVER",
				JOptionPane.PLAIN_MESSAGE, icon, possibilities,
				"Please input name..");

		// Client Registration
		if (clientName != null) {
			contentPane.setVisible(true);
			try {
				// Broadcast to all online user about the newly joined user
				dos.writeInt(ServerConstants.REGISTER_BROADCAST);
				dos.writeUTF(clientName + " has joined!" + "\n");
				dos.flush();

				// For adding the registered user to the JList <ArrayList>
				dos.writeInt(ServerConstants.CLIENT_JOIN);
				dos.writeUTF(clientName);
				dos.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// Exit if user chooses cancel button
		else {
			System.exit(0);
		}		
		// Shows the name of client using the chat
		setTitle(clientName);
		pack();

		// Menu bar for send and downloading files
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu mnNewMenu = new JMenu("File Sharing");
		mnNewMenu.setIcon(new ImageIcon("./iconImages/fileSharingIcon.png"));
		menuBar.add(mnNewMenu);

		JMenuItem mntmSendFileToServer = new JMenuItem("Send Files To server");
		mntmSendFileToServer.setIcon(new ImageIcon(
				"./iconImages/fileSendingIcon.png"));
		mnNewMenu.add(mntmSendFileToServer);

		JMenuItem mntmRequestFileFromClientServer = new JMenuItem("Request File From Client Server");
		mntmRequestFileFromClientServer.setIcon(new ImageIcon(
				"./iconImages/fileSendingIcon.png"));
		mnNewMenu.add(mntmRequestFileFromClientServer);		

		// Action listener to the send file menu item to server
		mntmSendFileToServer.addActionListener(this);
		mntmSendFileToServer.setActionCommand("mntmSendFileServer");

		// Action listener to the send file menu item to client
		mntmRequestFileFromClientServer.addActionListener(this);
		mntmRequestFileFromClientServer.setActionCommand("mntmRequestFileFromClientServer");		
		
		setVisible(true);		
	}

	/**
	 * This method performs the actions for the send button and exit button
	 */

	@Override
	public void actionPerformed(ActionEvent event) {
		// Get the selected client from the Jlist, -1 if no selected client
		int userSelected = clientList.getSelectedIndex();		
		try {

			// Select files from the directory
			if ("mntmSendFileServer".equals(event.getActionCommand())) {
				
				chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				chooser.showOpenDialog(null);
				File directory = chooser.getSelectedFile();				
				String fullPath = directory.getAbsolutePath();			
				
				File srcDir = new File(fullPath);	
		        if (!srcDir.isDirectory()) {	
		            System.out.println("Source directory is not valid ..Exiting the client");		            
		        }	
		        File[] files = srcDir.listFiles();	
		        int fileCount = files.length;	
		        if (fileCount == 0) {	
		            System.out.println("Empty directory ..Exiting the client");		            
		        }
		        
		        System.out.println("File count" +fileCount);//No of file in the folder in client side
		        for (int i = 0; i < fileCount; i++) {	        	
		            //Writing list of files to data base
		            System.out.println("File names "+ files[i].getName());
		            dos.writeInt(ServerConstants.CLIENT_PUBLISH);
					dos.writeUTF(files[i].getName());
					dos.writeUTF(clientName);
					chatHistory.append("Me:> Sent file to Server >  "
							+ files[i].getName() + "\n");
					dos.flush();  	            	
		        }
			}
			//Remote Fetch
			Object selectedValue = clientList.getSelectedValue();
			if ("mntmRequestFileFromClientServer".equals(event.getActionCommand())) {
				UIManager.put("OptionPane.okButtonText", "OK");
				if (userSelected == ServerConstants.NO_SELECTED_CLIENT){
					JOptionPane.showMessageDialog(mainChatPanel,
							"Please select client server to request file");
				} else {
				RemoteAddress address = remoteAddresses.get(selectedValue);
				System.out.println("remote peer :"+address.remotePeer+ "Remote peer port : "+address.peerPort);
				
				try{
					requestSocket = new Socket(address.remotePeer,address.peerPort);
					rdos = new DataOutputStream(requestSocket.getOutputStream());
					rdis = new DataInputStream(requestSocket.getInputStream());								
					
					rdos.writeInt(ServerConstants.REMOTE_FETCH);
					rdos.writeInt(userSelected);
					rdos.writeUTF(clientName);
					selectSeverClient = clientList.getSelectedValue().toString();
					
					rdos.writeUTF(chatInput.getText());		//Send file name to request					
					rdos.flush();
					
					int messageType = rdis.readInt();//Get the message type
					switch(messageType){
					case ServerConstants.REMOTE_FETCH_SUCCESS:
						// Received file from the client server
						
						int fileSize = rdis.readInt();//Read the selected file size
						String sentFileName = rdis.readUTF(); //Read the selected file name
						
						//Choosing the file location to save the file
						JFileChooser chooser = new JFileChooser();
						chooser.setSelectedFile(new File(sentFileName));
						int status = chooser
								.showSaveDialog(mainChatPanel);
						if (status == JFileChooser.APPROVE_OPTION) {
						File file = chooser.getSelectedFile();
					    byte [] mybytearray  = new byte [fileSize];
					    
					    InputStream is = requestSocket.getInputStream();
					    fos = new FileOutputStream(file);
					    bos = new BufferedOutputStream(fos);				     
					    
					    int bytesRead = is.read(mybytearray, 0, mybytearray.length);				    
					    bos.write(mybytearray);			  
					    bos.flush();
					    
					    System.out.println("File " + chooser.getSelectedFile()
						          + " downloaded (" + bytesRead + " bytes read)");				    
					   
					    if (fos != null) fos.close();
					    if (bos != null) bos.close();
					    rdos.writeUTF("File saved successfully!"); //send success message back to the client server
						chatHistory.append("File saved successfully!"+"\n");
						}
						break;
					
					case ServerConstants.REMOTE_FETCH_ERROR:
						chatHistory.append(rdis.readUTF()+"\n");
						break;
				}
					chatInput.setText("");//make the field blank after request the file											
				}
				catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				}
			}
			// Listener for search button
			if ("search".equals(event.getActionCommand())) {				
				dos.writeInt(ServerConstants.CLIENT_SEARCH);
				dos.writeUTF(chatInput.getText());// searching file name
				dos.writeUTF(clientName); // Searching client name
				chatHistory.append("Me:> Searching file...  "
						+ chatInput.getText() + "\n");
				chatInput.setText("");
				dos.flush();
			}
			
			// Listener for ping button
			if ("ping".equals(event.getActionCommand())) {
				dos.writeInt(ServerConstants.CLIENT_PING);
				dos.writeUTF("Ping to Server");// Ping message to server
				dos.writeUTF(clientName);// connected client
				chatHistory.append("Me: > Ping to Server" + "\n");
				dos.flush();
			}
			
			// Listener for send button if broadcast chat
			if ("send".equals(event.getActionCommand())) {
				// Listener for send button if broadcast chat
				if (userSelected == ServerConstants.NO_SELECTED_CLIENT) {
					dos.writeInt(ServerConstants.CHAT_BROADCAST); // determine
																	// the type
																	// of
																	// message
																	// to be
																	// sent
					dos.writeUTF(clientName + ":>" + chatInput.getText()); // message
																			// format
					// Append chatHistory
					chatHistory.append("Me:>  " + chatInput.getText() + "\n");
					chatInput.setText("");
					dos.flush(); // force the message to be sent (sometimes data
									// can be buffered)
				}
				// Listener for send button if private chat
				else {
					dos.writeInt(ServerConstants.PRIVATE_MESSAGE);
					dos.writeInt(userSelected); // determine the type of message
												// to be sent
					// For server window display: broadcast message
					dos.writeUTF(clientName + ":>" + chatInput.getText()); // message
																			// format
					// For server window display: private message
					dos.writeUTF("Private message from " + clientName + " to "
							+ selectedValue + ":>"
							+ chatInput.getText()); // message format
					// For Clients' window display: private message
					chatHistory.append("Private message to "
							+ selectedValue + ":>"
							+ chatInput.getText() + "\n");
					chatInput.setText("");
					dos.flush(); // force the message to be sent (sometimes data
									// can be buffered)
				}
			}
			// Listener for exit button
			if ("exit".equals(event.getActionCommand())) {
				dos.writeInt(ServerConstants.CLIENT_LEAVE);
				dos.writeUTF(clientName);
				dos.flush();

				processing = false;
				try {
					client.close();
					requestSocket.close();
				} catch (Exception e) {
					System.out.println("Client exited");
					//e.printStackTrace();
				}

				this.dispose();
			}

		}catch (IOException e) {
			System.err.println("Client exited");
		} 
		clientList.clearSelection();
	}	
	
	
	class RemoteAddress
	{
		int peerPort;
		String remotePeer;
	};
	
	HashMap <String,RemoteAddress> remoteAddresses = new HashMap<String,RemoteAddress>();
	
	/*
	 * process messages from the server, requested by the client
	 */
	@Override
	public void run() {
		while (processing) {
			
			try {
				// receive a message from the server to determine the message
				// type
				int messageType = dis.readInt();
				// decode message and process
				switch (messageType) {
				case ServerConstants.CHAT_BROADCAST:
					chatHistory.append(dis.readUTF() + "\n");
					break;

				case ServerConstants.PRIVATE_MESSAGE:
					chatHistory.append(dis.readUTF() + "\n");
					break;

				case ServerConstants.CLIENT_JOIN:
					int sz = dis.readInt();

					// Populating the Jlist
					listModel.clear();
					for (int i = 0; i < sz; i++) {
						String userData = dis.readUTF();
						listModel.addElement(userData);
						
						RemoteAddress address = new RemoteAddress();

						address.remotePeer = dis.readUTF();
						address.peerPort = dis.readInt();
						
						String remoteClient =dis.readUTF();					
						remoteAddresses.put(userData, address);
					}
					break;

				case ServerConstants.CLIENT_LEAVE:
					String exitMessage = dis.readUTF();
					int newSize = dis.readInt();
					String exitClient = dis.readUTF();
					chatHistory.append(exitMessage + "\n");
					listModel.removeElement(exitClient);
					break;			
				
				case ServerConstants.REGISTER_BROADCAST:
					chatHistory.append(dis.readUTF() + "\n");
					break;

				case ServerConstants.CHAT_MESSAGE:
					break;
					
				case ServerConstants.CLIENT_SEARCH:
					String readMsg = dis.readUTF(); // Reading Server message
					chatHistory.append("Server: > Searched IP address is "
							+ readMsg + "\n");
					break;
					
				case ServerConstants.SERVER_PING:
					String serverPingMsg = dis.readUTF();
					chatHistory.append("Server: > " + serverPingMsg + "\n");
					break;				
				}
			} catch (IOException e) {
				System.err.println("Socket closed");
				// e.printStackTrace();
			}			
	}
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Client client = new Client();
		client.setVisible(true);
	}	
}

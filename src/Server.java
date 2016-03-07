

import java.awt.BorderLayout;
import java.awt.Container;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Font;



/**
 * This Server.Class is for create server'
 * @author Madushani Dilanka
 */

public class Server extends JFrame {
	private static final long serialVersionUID = -2291453973624020582L;
	ServerSocket serverSocket;
	JTextArea systemLog = new JTextArea(5, 60);
	ArrayList<ServerThread> connectedClients = new ArrayList<ServerThread>();
	public final int Default_Port = 5000;
	protected int port;
	
	
	public Server(int port) {
		setTitle("Server");
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		if (port==0){
		      port=Default_Port;
		    }
		    this.port=port;			
		
		// construct ServerSocket
		try {
			serverSocket = new ServerSocket(port);
	
		} catch (IOException e) {
			e.printStackTrace();
		}

		// setup minimalist user interface
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());
		systemLog.setForeground(new Color(0, 128, 0));
		systemLog.setEditable(false);

		// add a system log to the user interface
		JScrollPane scrollPane = new JScrollPane(systemLog);
		contentPane.add(scrollPane, BorderLayout.WEST);
		
		JButton btnExit = new JButton("Exit");
		btnExit.setFont(new Font("Tahoma", Font.BOLD, 12));
		btnExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				System.exit(0);
			}
		});
		btnExit.setBackground(new Color(255, 0, 0));
		btnExit.setForeground(Color.WHITE);
		getContentPane().add(btnExit, BorderLayout.EAST);

		pack();
		setVisible(true);
	}

	public void start() {
		try {
			while (true) // keep accepting new clients
			{
				Socket remoteClient = serverSocket.accept(); // block and wait
						        
				// construct a new server thread, to handle each client socket
				ServerThread st = new ServerThread(remoteClient, this,
						connectedClients);
				st.start();
				connectedClients.add(st);				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		Server server = new Server(0);		
		server.start();
	}

	public JTextArea getSystemLog() {
		return systemLog;
	}

	public void setSystemLog(JTextArea systemLog) {
		this.systemLog = systemLog;
	}
}

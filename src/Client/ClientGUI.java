package Client;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class ClientGUI extends JFrame{
	
	private static final long serialVersionUID = 1L;
	public final static int port = 2628;
	private String server;
	private Socket clientSocket;
	private PrintWriter out;
	private BufferedReader in;
	
	JTextArea ta = new JTextArea(20, 40);
	Container cp = getContentPane();
	
	public ClientGUI (String server, int timeout){
		try{
			clientSocket = new Socket(server, port);
			//in/out - przes³anie danych u¿ytkownika do serwera
			//in/out - wypisanie w pole tesktowe uczestników wyœcigu oraz danych, które ich ewentualnie dotycz¹
			/*in = new BufferedReader (
                new InputStreamReader(clientSocket.getInputStream(), "UTF8"));
		       out = new PrintWriter (
		                new OutputStreamWriter(clientSocket.getOutputStream(), "UTF8"),
		                             true);
		
		       String resp = in.readLine(); // po³¹czenie nawi¹zane - info o tym
		       System.out.println(resp);
		       if (!resp.startsWith("220")) {
		         cleanExit(1); // je¿eli dostêp niemo¿liwy
		         }*/
			clientSocket.setSoTimeout(timeout);
		} catch(UnknownHostException exc) {
	         System.err.println("Uknown host " + server);
	         System.exit(2);
	    } catch(Exception exc) {
	         exc.printStackTrace();
	         System.exit(3);
	    }
		
		Font f = new Font("Dialog", Font.BOLD, 14);
	    ta.setFont(f);
	    cp.add(new JScrollPane(ta));
	    final JTextField tf = new JTextField();
	    tf.setFont(f);
	    tf.setBorder(BorderFactory.createLineBorder(Color.blue, 2));
	    cp.add(tf, "South");
	    
	    tf.addActionListener( new ActionListener() {
	    	public void actionPerformed(ActionEvent e) {
	    		sendUser(tf.getText());
	    	}
	    });
	    
	    addWindowListener( new WindowAdapter() {
	    	public void windowClosing(WindowEvent e) {
	    		dispose();
	    		cleanExit(0);
	    	}
	    });
	    
	    pack();
	    setVisible(true);
	    
	    SwingUtilities.invokeLater( new Runnable() {
	    	public void run() {
	    		tf.requestFocus();
	    	}
	    });
	}
	
	public void sendUser(String user){
		UniqueNumber number = new UniqueNumber();
		int x = number.create();
		//dodaæ autonumeracjê
		try{
			String resp = "";
			String answer = "";
			//zlecenie dla serwera
			out.println(x +" " +user);
			//odpowiedŸ serwera - jest na to wymaganie w zadaniu
			//...
			while (resp != null && !resp.startsWith("250")) {
		        resp = in.readLine();
		        answer += resp + "\n";
		        if (resp.startsWith("552")) break;  // s³owo nie znalezione
		    }
			ta.setText(answer);
		} catch(SocketTimeoutException exc) {
		       ta.setText("Za d³ugie oczekiwanie na odpowiedŸ");
	    } catch(Exception exc) {
	          exc.printStackTrace();
	    }
		
	}
	
	private void cleanExit(int code) {
	    try {
	      out.close();
	      in.close();
	      clientSocket.close();
	    } catch(Exception exc) {}
	    System.exit(code);
	}
	
	public static void main(String[] args) {
	    /*String host = args[0];
	    int port = Integer.parseInt(args[1]);
	    String[] names = { "Asia", "Adam", "Jacek" };
	    for (int i=0; i<names.length; i++)
	      new PhoneBookClients(host, port, names[i]);*/
	}

}

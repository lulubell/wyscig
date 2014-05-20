package Client;
/*import java.net.*;
import java.io.*;
import java.lang.reflect.*;
	
public class Main {

	  public static void main(String[] args) {

		  	//String host = "time.nist.gov";
		  String host = args[0];
		    int port = 13;
		    Socket socket = new Socket();

		    try {
		      // Utworzenie adresów
		      InetAddress inetadr = InetAddress.getByName(host);
		      InetSocketAddress conadr = new InetSocketAddress(inetadr, port);

		      // Po³aczenie z serwerem
		      // Okreœlenie maksymalnego czasu oczekiwania na po³¹czenie
		      socket.connect(conadr, 200);


		      // Pobranie strumienia wejœciowego gniazda
		      // Nak³adamy buforowanie
		      BufferedReader br = new BufferedReader(
		                            new InputStreamReader(
		                             socket.getInputStream()
		                             )
		                          );
		      // Okreslenie maksymalnego czasu oczekiwania na odczyt danych z serwera
		      socket.setSoTimeout(200);

		      // Czego mo¿emy siê dowiedzieæ o stanie gniazda?
		      report(socket);

		      // Odczyt odpowiedzi serwera (data i czas)
		      String line;
		      while ((line = br.readLine()) != null) {
		        System.out.println(line);
		      }

		      // Zamkniêcie strumienia i gniazda
		      br.close();
		      socket.close();
		    } catch (UnknownHostException exc) {
		        System.out.println("Nieznany host: " + host);
		    } catch (Exception exc) {
		         exc.printStackTrace();
		    }
		  }

		  // Dynamiczne wo³anie metod z klasy Socket
		  static void report(Socket s) throws Exception {
		    Method[] methods = (java.net.Socket.class).getMethods();
		    Object[] args = {};
		    for (int i=0; i<methods.length; i++) {
		      String name = methods[i].getName();
		      if ((name.startsWith("get") || name.startsWith("is")) &&
		          !name.equals("getChannel") &&
		          !name.equals("getInputStream") &&
		          !name.equals("getOutputStream")) {

		        System.out.println(name + "() = " +
		                           methods[i].invoke(s, args));
		      }
		    }
	  }
}*/

/*import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class Main extends JFrame
{
   public final static int port = 2628;
   private String server;
   private Socket clientSocket;
   private PrintWriter out;
   private BufferedReader in;
   private String database = "*";  // info ze wszystkich baz

   JTextArea ta = new JTextArea(20, 40);
   Container cp = getContentPane();



   public Main (String server, int timeout)   {

     try {
       clientSocket = new Socket (server, port);
       in = new BufferedReader (
                new InputStreamReader(clientSocket.getInputStream(), "UTF8"));
       out = new PrintWriter (
                new OutputStreamWriter(clientSocket.getOutputStream(), "UTF8"),
                             true);

       String resp = in.readLine(); // po³¹czenie nawi¹zane - info o tym
       System.out.println(resp);
       if (!resp.startsWith("220")) {
         cleanExit(1); // je¿eli dostêp niemo¿liwy
       }

       // Ustalenie maksymalnego czasu blokowania
       // na operacji czytania ze strumienia gniazda

       clientSocket.setSoTimeout(timeout);


     } catch(UnknownHostException exc) {
         System.err.println("Uknown host " + server);
         System.exit(2);
     } catch(Exception exc) {
         exc.printStackTrace();
         System.exit(3);
     }

     // wszystko posz³o dobrze - tworzymy i pokazujemy okno wyszukiwania

    Font f = new Font("Dialog", Font.BOLD, 14);
    ta.setFont(f);
    cp.add(new JScrollPane(ta));
    final JTextField tf = new JTextField();
    tf.setFont(f);
    tf.setBorder(BorderFactory.createLineBorder(Color.blue, 2));
    cp.add(tf, "South");

    tf.addActionListener( new ActionListener() {
      public void actionPerformed(ActionEvent e) {
         doSearch(tf.getText());
      }
    });

    addWindowListener( new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        dispose();
        cleanExit(0);
      }
    });

    pack();
    show();

    // Ustalenie fokusu na polu wprowadzania szukanych s³ów
    SwingUtilities.invokeLater( new Runnable() {
      public void run() {
        tf.requestFocus();
      }
    });
  }

  // Wyszukiwanie

  public void doSearch(String word) {
    try {
      String resp = "",
            defin = "Uzyskano nastêpuj¹ce definicje:\n";

      // Zlecenie dla serwera
      out.println("DEFINE " + database + " " + word);

      // Czytamy odpowiedŸ
      // Kod 250 na pocz¹tku wiersza oznacza koniec definicji
      while (resp != null && !resp.startsWith("250")) {
        resp = in.readLine();
        defin += resp + "\n";
        if (resp.startsWith("552")) break;  // s³owo nie znalezione
      }
      ta.setText(defin);
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

    int timeout = 0;
    String server = "dict.org";
    try {
      timeout = Integer.parseInt(args[0]);
      server = args[1];
    } catch(NumberFormatException exc) {
      server = args[0];
    } catch(ArrayIndexOutOfBoundsException exc) {}

    new Main(server, timeout);
  }

}*/

/*import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;
import java.util.regex.*;


class DictGui extends JFrame implements ActionListener {

  public final static int port = 2628;
  private String server;
  private SocketChannel channel;

  private JTextArea ta = new JTextArea(20, 20);
  private JTextField tf = new JTextField(20);
  private JLabel infoLab = new JLabel("Nie by³o szukania");
  private JButton paste = new JButton("Wklej definicjê");
  private Container cp = getContentPane();
  private ReadDef rd;

  public DictGui(String server) {
    this.server = server;

    // Otwarcie o po³¹czenie kana³u
    // metoda connect - zdefiniowana u koñca klasy
    try {
      channel = SocketChannel.open();
      channel.configureBlocking(false);
      connect();
    } catch(UnknownHostException exc) {
        System.err.println("Uknown host " + server);
        System.exit(1);
    } catch(IOException exc) {
        exc.printStackTrace();
        System.exit(2);
    }

    // Konfiguracja GUI
    Font f = new Font("Dialog", Font.BOLD, 14);
    ta.setFont(f);
    tf.setFont(f);
    tf.setBorder(BorderFactory.createLineBorder(Color.orange, 1));
    infoLab.setPreferredSize(new Dimension(200,30));
    JPanel p = new JPanel();
    p.setBorder(BorderFactory.createLineBorder(Color.red, 2));
    p.add(tf);
    p.add(infoLab);
    p.add(paste);
    cp.add(new JScrollPane(ta));
    cp.add(p, "South");

    tf.addActionListener(this);
    paste.addActionListener(this);

    // Przy zamykaniu aplikacji
    // zamykamy kana³ i gniazdo
    addWindowListener( new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        dispose();
        try {
         channel.close();
         channel.socket().close();
        } catch(Exception exc) {}
        System.exit(0);
      }
    });

    pack();
    show();
  }

  // Obs³uga akcji
  public void actionPerformed(ActionEvent e) {
    // Klikniêcie w przycisk "Wklej definicjê"
    // definicjê przechowuje dla nas obiekr klasy ReadDef
    if (e.getSource() == paste && rd != null) {
       ta.insert(rd.getResult(), ta.getCaretPosition());
    }
    else {  // ENTER na polu tekstowym tf - start w¹tku komuniakcji z serwerem
     if (!channel.isConnected()) try {
       connect();
     } catch(Exception exc) {
        exc.printStackTrace();
        return;
     }
     rd = new ReadDef(this, channel, tf.getText());
     rd.start();
    }
  }

  // £¹czenie kana³u z serwerem
  private void connect() throws UnknownHostException, IOException {
    if (!channel.isOpen()) channel = SocketChannel.open();
    channel.connect(new InetSocketAddress(server, port));
    System.out.print("£¹cze siê ...");
    while (!channel.finishConnect()) {
      try { Thread.sleep(200); } catch(Exception exc) { return; }
      System.out.print(".");
    }
    System.out.println("\nPo³¹czony.");
  }

  // Metoda wykorzystywana przez ReadDef
  // do pokazywania postepów komuniakcji z serwerem
  public void setInfo(String s) {
    infoLab.setText(s);
  }
}

class ReadDef extends Thread {

  private static Charset charset  = Charset.forName("ISO-8859-2");
  private static ByteBuffer inBuf = ByteBuffer.allocateDirect(1024);
  private static Matcher matchCode =
                 Pattern.compile("(\n250 ok)|(552 no match)").matcher("");
  private SocketChannel channel;
  private DictGui gui;
  private String word;


  public ReadDef(DictGui gui, SocketChannel ch, String wordToSearch) {
    this.gui = gui;
    channel = ch;
    word = wordToSearch;
  }

  private StringBuffer result;

  public void run() {
    result = new StringBuffer("Wyniki wyszukiwania:\n");
    int count = 0, rcount = 0;
    try {
      CharBuffer cbuf = CharBuffer.wrap("DEFINE * " + word + "\n");
      ByteBuffer outBuf = charset.encode(cbuf);
      channel.write(outBuf);

      while (true) {
        inBuf.clear();
        int readBytes = channel.read(inBuf);
        if (readBytes == 0) {
          gui.setInfo("Czekam ... " + ++count);
          Thread.sleep(200);
          continue;
        }
        else if (readBytes == -1) {
          gui.setInfo("Kana³ zamkniêty");
          channel.close();
          break;
        }
        else {
          inBuf.flip();
          cbuf = charset.decode(inBuf);
          result.append(cbuf);
          matchCode.reset(cbuf);
          if (matchCode.find()) break;
          else gui.setInfo("Czytam ... " + ++rcount);
        }
      }
    } catch(Exception exc) {
         exc.printStackTrace();
         return;
    }
    gui.setInfo("Czeka³em: " + count + " / Czyta³em: " + rcount + ". Gotowe.");
  }

  public String getResult() {
    if (result == null) return "Brak wyników wyszukiwania";
    return result.toString();
  }
}

class Main {
  public static void main(String[] args) {
    String server = "dict.org";
    new DictGui(server);
  }
}*/

import java.net.*;
import java.io.*;

public class PhoneBookClients extends Thread {

  private Socket sock = null;
  private PrintWriter out = null;
  private BufferedReader in = null;
  private String nameToSearch;

  public PhoneBookClients(String host, int port, String name ) {
    try {
      sock = new Socket(host, port);
      out = new PrintWriter(sock.getOutputStream(), true);
      in = new BufferedReader(
               new InputStreamReader(
                   sock.getInputStream()));

      nameToSearch = name;

    } catch (Exception exc) {
        exc.printStackTrace();
        System.exit(4);
    }
    start();
  }

  public void run() {
    try {
      for (int i=1; i <= 5; i++) {
        find(nameToSearch);
        Thread.sleep(500);
      }
      out.println("bye");
    } catch (Exception exc) {
        exc.printStackTrace();
    }
  }

  private void find(String name) throws IOException {
    out.println("get " + name);
    String resp = in.readLine();
    boolean ok = resp.startsWith("0");
    String tel = ok ? in.readLine() : " - not found";
    System.out.println(name + " - tel. " + tel);
  }
}
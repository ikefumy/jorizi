import java.io.*;
import java.net.*;

class ClientProcThread extends Thread{
    private int number;
	private Socket incoming;
	private InputStreamReader myIsr;
	private BufferedReader myIn;
	private PrintWriter myOut;

	public ClientProcThread(int n, Socket i, InputStreamReader isr, BufferedReader in, PrintWriter out) {
		number = n;
		incoming = i;
		myIsr = isr;
		myIn = in;
		myOut = out;
	}

    public void run() {
		try {
			myOut.println("Hello, client No." + number + "! Enter 'Bye' to exit.");
			while (true) {
				String str = myIn.readLine();
				System.out.println("Received from client No." + number + " Messages: " + str);
				if (str != null) {
					if (str.toUpperCase().equals("BYE")) {
						myOut.println("Good bye!");
						break;
					}
					MyServer.SendAll(str);
				}
			}
		} catch (Exception e) {
			
			System.out.println("Disconnect from client No." + number);
			MyServer.SetFlag(number, false);
		}
	}
}


public class MyServer {
    private static int maxConnection=100;
	private static Socket[] incoming;
	private static boolean[] flag;
	private static InputStreamReader[] isr;
	private static BufferedReader[] in;
	private static PrintWriter[] out;
	private static ClientProcThread[] myClientProcThread;
	private static int member;

    public static void SendAll(String str){
		for(int i = 1 ; i <= member ; i++){
			if(flag[i] == true){
				out[i].println(str);
				out[i].flush();
				System.out.println("Send messages to client No." + i);
			}
		}	
	}

    public static void SetFlag(int n, boolean value){
		flag[n] = value;
	}

    public static void main(String[] args){
        incoming = new Socket[maxConnection];
		flag = new boolean[maxConnection];
		isr = new InputStreamReader[maxConnection];
		in = new BufferedReader[maxConnection];
		out = new PrintWriter[maxConnection];
		myClientProcThread = new ClientProcThread[maxConnection];
		int n = 1;
		member = 0;
        
		try {
            ServerSocket server = new ServerSocket(10000);
            System.out.println("The server has launched!");
			while (true) {
				incoming[n] = server.accept();
				flag[n] = true;
				System.out.println("Accept client No." + n);
				
				isr[n] = new InputStreamReader(incoming[n].getInputStream());
				in[n] = new BufferedReader(isr[n]);
				out[n] = new PrintWriter(incoming[n].getOutputStream(), true);
				
				myClientProcThread[n] = new ClientProcThread(n, incoming[n], isr[n], in[n], out[n]);
				myClientProcThread[n] .start();
				member = n;
				n++;
			}
		} catch (Exception e) {
			System.err.println("error: " + e);
		}
    }
}
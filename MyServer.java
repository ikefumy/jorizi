import java.io.*;
import java.net.*;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.Container;
import java.awt.BorderLayout;
import java.awt.event.*;
import java.util.regex.Pattern;

class ClientProcThread extends Thread{
    private int number;
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
				
				if (str != null) {
					System.out.println("Received from client No." + number + " Messages: " + str);
					if (str.toUpperCase().equals("BYE")) {
						myOut.println("Good bye!");
						break;
					}
					// strが数字の場合（削除した列数）はもう一方の相手にだけ送信する
					if(checkString(str)){
						MyServer.SendOther(str, number);
					}
					else{
						MyServer.SendAll(str);
					}
				}
			}
		} catch (Exception e) {
			
			System.out.println("Disconnect from client No." + number);
			MyServer.SetFlag(number, false);
		}
	}

	// 文字列が数字であるか判定
	public static boolean checkString(String str) {
		boolean res = true;
		Pattern pattern = Pattern.compile("^[0-9]+$|-[0-9]+$");
		res = pattern.matcher(str).matches();
		return res;
	}
}


public class MyServer extends JFrame implements ActionListener, KeyListener {
  private static int maxConnection=100;
	private static Socket[] incoming;
	private static boolean[] flag;
	private static InputStreamReader[] isr;
	private static BufferedReader[] in;
	private static PrintWriter[] out;
	private static ClientProcThread[] myClientProcThread;
	private static int member;
	// JLabel label;

    public MyServer() {
		setTitle("Server");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocation(200, 200);
		setSize(12*26+10, 26*23+25);

		// STARTボタンの表示
		// label = new JLabel("");
		JButton btn = new JButton("START");
		btn.addActionListener(this);
		JPanel p = new JPanel();
		p.add(btn);
		// p.add(label);
		Container contentPane = getContentPane();
    	contentPane.add(p, BorderLayout.CENTER);
		
		// キーボード入力
		addKeyListener(this);
        
    }
	// キーボード入力があったとき
	@Override
	public void keyTyped(KeyEvent e1) {
		SendAll(String.valueOf(e1.getKeyChar()));
		System.out.println(e1.getKeyChar());
	}

	@Override
	public void keyPressed(KeyEvent e1) {
	}

	@Override
	public void keyReleased(KeyEvent e1) {
	}

	// STARTボタンが押されたとき
	@Override
	public void actionPerformed(ActionEvent e){
		// スタートボタンが押されたら全クライアントのゲームを同時に開始
		SendAll("start");
		// フォーカスをボタンからキーボードに変更
		this.requestFocus();
	}

    public static void SendAll(String str){
		for(int i = 1 ; i <= member ; i++){
			if(flag[i] == true){
				out[i].println(str);
				out[i].flush();
				System.out.println("Send messages to client No." + i);
			}
		}	
	}

	// 指定したクライアント以外に送信
	public static void SendOther(String str, int number){
		for(int i = 1 ; i <= member ; i++){
			if(flag[i] == true && i != number){
				out[i].println(str);
				out[i].flush();
				System.out.println("other Send messages to client No." + i);
			}
		}	
	}

    public static void SetFlag(int n, boolean value){
		flag[n] = value;
	}

    public static void main(String[] args){
        MyServer ms = new MyServer();
        ms.setVisible(true);
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
				myClientProcThread[n].start();
				out[n].println(String.valueOf(n));
				member = n;
				n++;
			}
		} catch (Exception e) {
			System.err.println("error: " + e);
		}
    }
}
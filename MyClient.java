import java.io.*;
import java.net.*;

import javax.swing.*;
import java.lang.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

public class MyClient extends JFrame {
	private Container c;
	PrintWriter out;
	public int mypattern;
	public static Tetris game;

    public MyClient() {
		setTitle("Player");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocation(200, 200);
		setSize(12*26+10, 26*23+25);
		c = getContentPane();

		game = new Tetris();
		game.init();
		c.add(game);

        addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent e1) {
				if(mypattern == 1){
				switch (e1.getKeyChar()) {
					case 'w':
						game.rotate(-1);
						break;
					case 'e':
						game.rotate(+1);
						break;
					case 'a':
						game.move(-1);
						break;
					case 'd':
						game.move(+1);
						break;
					case 's':
						int numClears = game.dropDown();
						game.score += 1;
						out.println(numClears);
						out.flush();
						break;
					
					}
				}
			if(mypattern == 2){
				switch (e1.getKeyChar()) {
				case 'i':
					game.rotate(-1);
					break;
				case 'o':
					game.rotate(+1);
					break;
				case 'j':
					game.move(-1);
					break;
				case 'l':
					game.move(+1);
					break;
				case 'k':
					int numClears = game.dropDown();
					game.score += 1;
					out.println(numClears);
					out.flush();
					break;
			}
			}
			}			
			public void keyPressed(KeyEvent e1) {
			}
			
			public void keyReleased(KeyEvent e1) {
			}
		});

        
        Socket socket = null;
        try {
			socket = new Socket("localhost", 10000);
		} catch (UnknownHostException e) {
			System.err.println("UnknownHostException: " + e);
		} catch (IOException e) {
			 System.err.println("IOException: " + e);
		}
		
        MesgRecvThread mrt = new MesgRecvThread(socket, game);
		mrt.start();
    }

    public class MesgRecvThread extends Thread {
        Socket socket;
        Tetris game;
		byte[] data = new byte[1024];
		public static int num;

		public MesgRecvThread(Socket s, Tetris g){
			socket = s;
            game = g;
		}
		
		
		public void run() {
			try{
				InputStreamReader sisr = new InputStreamReader(socket.getInputStream());
				BufferedReader br = new BufferedReader(sisr);
				out = new PrintWriter(socket.getOutputStream(), true);
				String myNumberStr = br.readLine();
				int myNumberInt = Integer.parseInt(myNumberStr);
				if (myNumberInt % 2 == 1){
					mypattern = 1;
				}else{
					mypattern = 2;
				}
				while(true){


					String inputLine = br.readLine();
					if(inputLine != null){
						System.out.println("inputLine: " + inputLine);
					}else{
						break;
					}
					
				}
				socket.close();
			}catch (IOException e){
				System.err.println("IOException: " + e);
			}
        }
    }

    public static void main(String[] args) {
        MyClient net = new MyClient();
        net.setVisible(true);
		new Thread() {
			@Override public void run() {
				while (true) {
					try {
						Thread.sleep(1000);
						game.dropDown();
					} catch ( InterruptedException e ) {}
				}
			}
		}.start();
    }
    
}





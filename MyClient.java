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

    public MyClient() {
		setTitle("Player");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocation(200, 200);
		setSize(12*26+10, 26*23+25);
		c = getContentPane();

		Tetris game = new Tetris();
		game.init();
		c.add(game);

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
		
		public MesgRecvThread(Socket s, Tetris g){
			socket = s;
            game = g;
		}
		
		
		public void run() {
			try{
				InputStreamReader sisr = new InputStreamReader(socket.getInputStream());
				BufferedReader br = new BufferedReader(sisr);
				out = new PrintWriter(socket.getOutputStream(), true);
				while(true){
					// Make the falling piece drop every second
					try{
						Thread.sleep(1000);
						game.dropDown();
					}catch (InterruptedException e){
						System.err.println("InterruptedException: " + e);
					}

					String inputLine = br.readLine();
					if(inputLine != null){
						System.out.println("inputLine: " + inputLine);
                        switch (inputLine) {
                            case "w":
                                game.rotate(-1);
                                break;
                            case "e":
                                game.rotate(+1);
                                break;
                            case "a":
                                game.move(-1);
                                break;
                            case "d":
                                game.move(+1);
                                break;
                            case "s":
                                int numClears = game.dropDown();
                                game.score += 1;
                                out.println(numClears);
                                out.flush();
                                break;
                        }
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
    }
    
}





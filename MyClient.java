import java.io.*;
import java.net.*;

import javax.swing.*;
import java.lang.*;
import java.awt.*;
import java.util.*;

public class MyClient extends JFrame {
	private Container c;
    private int player_number;
	PrintWriter out;
    private boolean gameStart = false;
    private boolean gameEnd = false;

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
        try {
            InputStreamReader sisr = new InputStreamReader(socket.getInputStream());
            BufferedReader br = new BufferedReader(sisr);
            String num = br.readLine();
            player_number = Integer.valueOf(num);
            System.out.println("your player number is " + player_number);
        } catch (IOException e) {
			System.err.println("IOException: " + e);
        }
        CheckGameOver cgo = new CheckGameOver(game);
        MesgRecvThread mrt = new MesgRecvThread(socket, game, player_number);
        FallPieceThread fpt = new FallPieceThread(game);
        cgo.start();
        mrt.start();
        fpt.start();
    }

    public class CheckGameOver extends Thread {
        Tetris game;
        public CheckGameOver(Tetris g) {
            game = g;
        }

        public void run() {
            while (true) {
                if ((game.isGameOver())) {
                    out.println("end");
                }
            }
        }
    }


    // ミノを毎秒降下させるためのスレッド
    public class FallPieceThread extends Thread {
        Tetris game;

        public FallPieceThread(Tetris g) {
            game = g;
        }

        public void run() {
            while (true) {
                boolean flag = gameStart && !gameEnd;
                if(flag){
                    // Make the falling piece drop every second
                    try{
                        Thread.sleep(1000);
                        int numClears = game.dropDown();
                        out.println(numClears);
                    }catch (InterruptedException e){
                        System.err.println("InterruptedException: " + e);
                    }
                }
            }
        }
    }

    // サーバーからの入力に応じてテトリスを動かすスレッド
    public class MesgRecvThread extends Thread {
        Socket socket;
        Tetris game;
        int num;
		boolean fevermode1 = true;
		boolean fevermode2 = true;
		
		public MesgRecvThread(Socket s, Tetris g, int number){
			socket = s;
            game = g;
            num = number;
		}
		
		public void run() {
			try{
				InputStreamReader sisr = new InputStreamReader(socket.getInputStream());
				BufferedReader br = new BufferedReader(sisr);
				out = new PrintWriter(socket.getOutputStream(), true);
				while(true){
					String inputLine = br.readLine();
					if(inputLine != null){
                        // inputLineが数字 -> その数分妨害ブロック行を追加
                        if(checkString(inputLine)){
                            try{
                                int inputLineInt = Integer.parseInt(inputLine);
                                game.addRow(inputLineInt);
                            }
                            catch (NumberFormatException e){
                                e.printStackTrace();
                            }
                        }
                        // inputLineが文字列 -> キーボード入力に従ってミノを操作
                        else{
                            System.out.println("inputLine: " + inputLine);
                            if (num % 2 == 0) {
                                switch (inputLine) {
                                    case "w" :
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
				    case "u":
								if(fevermode2){
											fpt.stopRunning();

											FallPieceThread fpt1 = new FallPieceThread(game, 500);
										    fpt1.start();
											fevermode2 = false;
											
											TimerTask task = new TimerTask() {
												public void run(){
													fpt1.stopRunning();
													FallPieceThread fpt2 = new FallPieceThread(game, 1000);
													fpt2.start();
												}
											};
											Timer timer = new Timer();
											timer.schedule(task, 30000);
											
										}

                                }
                            } else {
                                switch (inputLine) {
                                    case "i" :
                                        game.rotate(-1);
                                        break;
                                    case "o":
                                        game.rotate(+1);
                                        break;
                                    case "j":
                                        game.move(-1);
                                        break;
                                    case "l":
                                        game.move(+1);
                                        break;
                                    case "k":
                                        int numClears = game.dropDown();
                                        game.score += 1;
                                        out.println(numClears);
                                        out.flush();
                                        break;
									case "q":
									    if(fevermode1){
											fpt.stopRunning();
											FallPieceThread fpt1 = new FallPieceThread(game, 500);
										    fpt1.start();
											fevermode1 = false;
											
											TimerTask task = new TimerTask() {
												public void run(){
													fpt1.stopRunning();
													FallPieceThread fpt2 = new FallPieceThread(game, 1000);
													fpt2.start();
												}
											};
											Timer timer = new Timer();
											timer.schedule(task, 30000);
										}
                                }
                            }
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

        // 文字列が数字であるか判定
        public static boolean checkString(String str) {
            boolean res = true;
            Pattern pattern = Pattern.compile("^[0-9]+$|-[0-9]+$");
            res = pattern.matcher(str).matches();
            return res;
        }
    }

    public static void main(String[] args) {
        MyClient net = new MyClient();
        net.setVisible(true);
    }
    
}
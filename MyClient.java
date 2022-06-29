import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import java.util.regex.Pattern;
import java.util.Timer;
import java.util.TimerTask;

import java.awt.event.*;
import java.beans.FeatureDescriptor;

public class MyClient extends JFrame {
	private Container c;
    private int player_number;
	PrintWriter out;
    private boolean gameStart = false;
    private boolean gameEnd = false;
    Socket socket = null;
    boolean fevermode = true;
    boolean TripleSpeedFlag = false;

    public MyClient() {
        
        try {
			socket = new Socket("192.168.1.2", 8080);
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
        
        setTitle("Player" + player_number);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocation(200, 200);
		setSize(12*26+10, 26*23+25);
		c = getContentPane();
        
		Tetris game = new Tetris();
		game.init();
		c.add(game);


        CheckGameOver cgo = new CheckGameOver(game);
        FallPieceThread fpt = new FallPieceThread(game, 1000);
        MesgRecvThread mrt = new MesgRecvThread(socket, game, player_number, fpt);
        cgo.start();
        fpt.start();
        mrt.start();

         //クライアントからの入力
         addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent e1) {
				switch (e1.getKeyChar()) {
					case 'i' :
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
                    case 'u':
                            if(fevermode && !TripleSpeedFlag){
                                try {
                                    out = new PrintWriter(socket.getOutputStream(), true);
                                    out.println("fev");
                                    fevermode = false;                            
                                } catch (IOException e) {
                                    System.err.println("IOException: " + e);
                                }
                            }
				} 
			}
			
			public void keyPressed(KeyEvent e1) {
			}
			
			public void keyReleased(KeyEvent e1) {
			}
		});
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
        int dropInterval;
        private boolean running = true;

        public FallPieceThread(Tetris g, int interval) {
            game = g;
            dropInterval = interval;
        }

    
        public void run() {
            while (running) {
                boolean flag = gameStart && !gameEnd ;
                if(!flag){
                    System.out.println("Push start");
                }
                if(flag){
                    // Make the falling piece drop every second
                    try{
                        Thread.sleep(dropInterval);
                        int numClears = game.dropDown();
                        out.println(numClears);
                    }catch (InterruptedException e){
                        System.err.println("InterruptedException: " + e);
                    }
                }
            }
        }

        public void stopRunning() {
            running = false;
        }
    }


    // サーバーからの入力に応じてテトリスを動かすスレッド
    public class MesgRecvThread extends Thread {
        Socket socket;
        Tetris game;
        int num;
        FallPieceThread fpt;
		boolean fevermode1 = true;
		boolean fevermode2 = true;
        FeverMessage fm = new FeverMessage();
        
		
		public MesgRecvThread(Socket s, Tetris g, int number, FallPieceThread f){
			socket = s;
            game = g;
            num = number;
            fpt = f;
		}
		
		public void run() {
			try{
				InputStreamReader sisr = new InputStreamReader(socket.getInputStream());
				BufferedReader br = new BufferedReader(sisr);
				out = new PrintWriter(socket.getOutputStream(), true);
                while(true){
					String inputLine = br.readLine();
                    String startStr = "start";
                    String endStr = "end";
					if(inputLine != null){
                        if(startStr.equals(inputLine)){
                            gameStart = true;
                        }
                        else if(endStr.equals(inputLine)){
                            gameEnd = true;
                            break;
                        } else if (gameStart && !gameEnd) {
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
                                            if(fevermode2 && !TripleSpeedFlag){
                                                fevermode2 = false;
                                                TripleSpeedFlag = true;
                                                TripleSpeedFall dsf = new TripleSpeedFall(fpt, game, fm);
                                            }
                                            break;
                                        case "fev":
                                                TripleSpeedFlag = true;
                                                TripleSpeedFall dsf = new TripleSpeedFall(fpt, game, fm);
                                                break;      
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
                                            if(fevermode1 && !TripleSpeedFlag){
                                                fevermode1 = false;
                                                TripleSpeedFlag = true;
                                                TripleSpeedFall dsf = new TripleSpeedFall(fpt, game, fm);
                                            }
                                            break;
                                        case "fev":
                                            TripleSpeedFlag = true;
                                            TripleSpeedFall dsf = new TripleSpeedFall(fpt, game, fm);
                                            break;
                                    }
                                }
                            }
                        }
                    }
				}
                socket.close();
			}catch (IOException e){
				System.err.println("IOException: " + e);
			}
        }

        // 文字列が数字であるか判定
        public boolean checkString(String str) {
            boolean res = true;
            Pattern pattern = Pattern.compile("^[0-9]+$|-[0-9]+$");
            res = pattern.matcher(str).matches();
            return res;
        }
    }

    //3倍速落下
    public class TripleSpeedFall{
        int count = 0;
        public TripleSpeedFall(FallPieceThread fpt, Tetris game, FeverMessage fm){
            fpt.stopRunning();
            fm.feverVisible(TripleSpeedFlag);
            FallPieceThread fpt1 = new FallPieceThread(game, 333);
            fpt1.start();
            TimerTask task1 = new TimerTask() {
                public void run(){
                    fpt1.stopRunning();
                    FallPieceThread fpt2 = new FallPieceThread(game, 1000);
                    fpt2.start();
                    TripleSpeedFlag = false;
                    fm.feverVisible(TripleSpeedFlag);
                }
            };
            TimerTask task2 = new TimerTask(){
                public void run(){
                    fm.timebar(count);
                    count++;
                }
            };
            Timer timer = new Timer();
            timer.schedule(task1, 30000);
            timer.schedule(task2, 0, 1000);

        }
    }
    
    //ferverメッセージの表示
    public class FeverMessage extends JFrame{
        Container c;
        JProgressBar bar = new JProgressBar(0,29);;
        
        public FeverMessage(){

            this.setTitle("FerverMode");
            this.setSize(400, 200);
            this.setLocationRelativeTo(null);
            this.c = getContentPane();
            JPanel labelPanel = new JPanel();
            JPanel labelPanel2 = new JPanel();
            
            JLabel l = new JLabel("Fever Mode!!");        
            l.setFont(new Font("Arial", Font.PLAIN, 50));
            l.setForeground(Color.RED);
            
            JLabel l2 = new JLabel("Show remaining time");        
            l2.setFont(new Font("Arial", Font.PLAIN, 25));
            l2.setForeground(Color.BLACK);

            labelPanel.add(l);
            c.add(labelPanel, BorderLayout.PAGE_START);

            labelPanel2.add(l2);
            c.add(labelPanel2, BorderLayout.PAGE_END);

            UIManager.put("ProgressBar.foreground", Color.WHITE);
		    UIManager.put("ProgressBar.background", Color.BLUE);
            bar.setForeground(Color.WHITE);
            bar.setBackground(Color.BLUE);

            bar.setValue(0);
            JPanel barPanel = new JPanel();
            barPanel.add(bar);
            c.add(barPanel, BorderLayout.CENTER);

        }

        public void feverVisible(boolean flag){
            this.setVisible(flag);
        }

        public void timebar(int count){
            bar.setValue(count);
        }
    }


    public static void main(String[] args) {
        MyClient net = new MyClient();
        net.setVisible(true);
    }
    
}

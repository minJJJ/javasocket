package socket;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.sql.*;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

public class Server{

    ArrayList<PrintWriter> clientList;	//client 하나 당 printwriter 하나
    JTextArea textArea;
    JScrollPane scrollPane;
    
    //db와의 connection을 반환
    public Connection makeConnection()
    {
    	String url="jdbc:mysql://localhost/socketdb?&useSSL=false";
    	Connection con=null;
    	try
    	{
    		Class.forName("com.mysql.jdbc.Driver");
    		con=DriverManager.getConnection(url,"minjae","1111");
    	}
    	catch(ClassNotFoundException | SQLException e)
    	{
    		textArea.append("mysql 연결 오류");
    		e.getStackTrace();
    	}
    	finally
    	{
    		return con;
    	}
    }
    public static void main(String[] args) throws SQLException {

        new Server().setView();

    }

    // 서버 화면 구성
    public void setView() throws SQLException{

        JFrame frame = new JFrame("Server");
        
        textArea = new JTextArea(20,42); 

        JPanel panel = new JPanel();
        JButton btn = new JButton("종료");
        btn.addActionListener(new ExitButtonListener());

        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        textArea.setVisible(true);

        scrollPane = new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

        panel.add(scrollPane);
        panel.add(btn);

        frame.getContentPane().add(BorderLayout.CENTER, panel);
        frame.setSize(500, 430);
        frame.setVisible(true);
        startServer();

    }

    // 서버 
    public void startServer(){

        clientList = new ArrayList<PrintWriter>();
        ServerSocket serverSocket;
    	
        try{
            serverSocket = new ServerSocket(5000);
            textArea.append("서버가 생성되었습니다.\n");	

            while(true){		//무한반복문 돌면서 클라이언트 접속,대화 등 관리

                Socket socket = serverSocket.accept();
                OutputStream os = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(os);
                tell("["+socket.getInetAddress()+":"+socket.getPort()+"]이 접속했습니다.");
                clientList.add(writer);
                Thread thread = new Thread(new ClientHandler(socket, writer));
                thread.start();

                textArea.append("["+socket.getInetAddress()+":"+socket.getPort()+"]이 접속했습니다.\n");
                scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
                textArea.append("총 인원수 "+ clientList.size() +"명 입니다.\n");
                scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());

            }

        }catch(Exception e){

            e.printStackTrace();

        }

    }

    //여러명이 접속하기위해 스레드사용
    public class ClientHandler implements Runnable {

        Socket socket;
        BufferedReader reader;
        PrintWriter pw;
        InputStreamReader isReader;
        String message;
    	Connection con=makeConnection();
    	
        public ClientHandler(Socket socket, PrintWriter pw){

            try{

            this.socket = socket;
            this.pw = pw;
            isReader = new InputStreamReader(this.socket.getInputStream());
            reader = new BufferedReader(isReader);

            }catch(Exception e){

                e.printStackTrace();

            }

        }

        //클라이언트로부터 입력받은 내용을 처리한다
        public void run() {
        	
            try{
            	Statement stmt= con.createStatement();
                while((message = reader.readLine()) != null){//입력이 들어옴

                	//클라이언트 접속 종료 처리
                    if(message.length()>=3 && message.substring(0, 3).equals("bye")){

                        clientList.remove(this.pw);
                        this.pw.close();
                        this.socket.close();

                        tell("["+socket.getInetAddress()+":"+socket.getPort()+"] 이 퇴장했습니다.");

                        textArea.append("["+socket.getInetAddress()+":"+socket.getPort()+"]이 퇴장했습니다.\n");
                        scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
                        textArea.append("총 인원수 "+ clientList.size() +"명 입니다.\n");
                        scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());

                        break;

                    }else{//채팅 내용 전송 처리
                    	String sql="insert into client(ip,message, date) values"+"(\'"+socket.getInetAddress()+":"+socket.getPort()+"\',\'"+message.substring(3)+"\',"+"now()"+");";
                    	if(stmt.executeUpdate(sql)==1)
                    	{
                    		System.out.println("db 추가 성공");
                    	}
                    	else
                    	{
                    		System.out.println("db 추가 오류");
                    	}
                        tell("["+socket.getInetAddress()+":"+socket.getPort()+"] : " + message.substring(3));
                        textArea.append("["+socket.getInetAddress()+":"+socket.getPort()+"] : " + message.substring(3) + "\n");
                        scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());

                    }
                }

            }catch(Exception e){

                e.printStackTrace();

            }
        }
    }

    // 서버에 접속한 클라이언트 모두에게 채팅 내용을 전송
    public void tell(String message){

        Iterator<PrintWriter> it = clientList.iterator();

        while(it.hasNext()){

            try {

                PrintWriter writer = (PrintWriter)it.next();
                writer.println(message);
                writer.flush();

            } catch (Exception e) {

                e.printStackTrace();

            }
        }

    }

    public class ExitButtonListener implements ActionListener{

        public void actionPerformed(ActionEvent arg0) {

            try{

                System.exit(0);

            }catch(Exception e){

                e.printStackTrace();

            }

        }

    }


}
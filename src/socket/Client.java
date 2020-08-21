package socket;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.Statement;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

public class Client implements KeyListener{

    Socket socket;
    BufferedReader reader;
    PrintWriter pw;
    InputStreamReader streamReader;
    Thread readerThread;

    JFrame frame;
    JTextArea textArea;
    JPanel panel;
    JTextField textField;

 
    
    public static void main(String[] args) throws IOException, SQLException{

        new Client().setView();

    }

    //채팅 클라이언트 화면을 구성
    public void setView() throws SQLException{
    	        frame = new JFrame("Client");
        textArea = new JTextArea(20,42);
        panel = new JPanel();

        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        textArea.setVisible(true);

       
        JScrollPane jScrollPane = new JScrollPane(textArea);
        jScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        jScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

        textField = new JTextField(20);
        textField.addKeyListener(this);
        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(new SendButtonListener());
        JButton exitButton = new JButton("Exit");
        exitButton.addActionListener(new ExitButtonListener());

        panel.add(jScrollPane);
        panel.add(textField);
        panel.add(sendButton);
        panel.add(exitButton);

        frame.setVisible(false);

        textArea.append("접속했습니다.\n");

        frame.getContentPane().add(panel);
        frame.setSize(500, 430);
        frame.setVisible(true);

        connectServer();


    }

    private void connectServer(){

        try {
        	
            socket = new Socket("localhost",5000);
            streamReader = new InputStreamReader(socket.getInputStream());
            reader = new BufferedReader(streamReader);
            pw = new PrintWriter(socket.getOutputStream());

            readerThread = new Thread(new IncomingReader());
            readerThread.start();

            System.out.println("서버 접속 성공");

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

    public class SendButtonListener implements ActionListener{

        public void actionPerformed(ActionEvent arg0) {

            try {

                pw.println("say"+textField.getText());
                pw.flush();

            } catch (Exception e) {

                e.printStackTrace();

            }

            textField.setText("");
            textField.requestFocus();

        }

    }

    public class ExitButtonListener implements ActionListener{

        public void actionPerformed(ActionEvent arg0) {

            try {

                pw.println("bye");
                pw.flush();

                System.exit(0);

            } catch (Exception e) {

                e.printStackTrace();

            }

        }

    }

    //스레드(서버에서 받는 내용 표시)
     public class IncomingReader implements Runnable {

          public void run() {

           String message;

           try {

            while ((message = reader.readLine()) != null) {

                textArea.append(message + "\n");

            }    

           } catch (Exception e) {e.printStackTrace();}

          }

}

     
    public void keyPressed(KeyEvent e) {}

    public void keyReleased(KeyEvent e) {

        int key=e.getKeyCode();

        if(key==10){

        try {

            pw.println("say"+textField.getText());
            pw.flush();

        } catch (Exception ex) {

            ex.printStackTrace();

        }

        textField.setText("");
        textField.requestFocus();

        }

    }

    public void keyTyped(KeyEvent e) {

        

    }

}
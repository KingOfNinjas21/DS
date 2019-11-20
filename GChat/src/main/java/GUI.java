import java.io.FileNotFoundException;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Random;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import util.LogFileWriter;

public class GUI extends Application {

    private static String nickname;
    private InetAddress ip;
    private int port;

    static volatile boolean finished = false;
    public static void main(String[] args) {
        launch(args);
    }
    private Thread send, recive;
    private MulticastSocket socket;
    private State state = State.Login;
    private enum State {Login, ChatMode, Finnished}

    private Logger logger = Logger.getLogger("Info Logger");

    @Override
    public void start(Stage primaryStage) {
        // Add a handler to persist logs to the filesystem
        logger.addHandler(LogFileWriter.getInstance("logs.txt"));
        
        //Login Scene
        Label nick = new Label();
        Label lPort = new Label();
        Label lIp = new Label();

        TextField inNickname = new TextField();
        TextField inPort = new TextField();
        TextField inIp = new TextField();

        Button bLogin = new Button();

        nick.setText("Choose Nickname:");
        lPort.setText("Enter Port");
        lIp.setText("Enter Ip");
        bLogin.setText("Start");

        Pane login = new VBox();
        login.getChildren().add(nick);
        login.getChildren().add(inNickname);
        login.getChildren().add(lIp);
        login.getChildren().add(inIp);
        login.getChildren().add(lPort);
        login.getChildren().add(inPort);
        login.getChildren().add(bLogin);

        //chat scene
        TextArea chatField = new TextArea();
        Button bSend = new Button();
        Button bLoggout = new Button();
        TextField msg = new TextField();

        chatField.setMaxWidth(390);
        chatField.setMaxHeight(320);

        bSend.setText("SEND");
        bLoggout.setText("Logout");
        msg.setPromptText("Enter message here");
        Pane chat = new VBox();
        chat.getChildren().add(chatField);
        chat.getChildren().add(msg);
        chat.getChildren().add(bSend);
        chat.getChildren().add(bLoggout);

        // set scene
        primaryStage.setScene(new Scene(login, 400, 350));
        primaryStage.show();



        //Login to chat
        System.out.println(state);
        bLogin.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                nickname = inNickname.getText();
                try {
                    ip = InetAddress.getByName(inIp.getText());
                } catch (Exception e) {
                    System.out.println(e);//handle it your self
                }

                port = Integer.parseInt(inPort.getText());

                if(connect()) {
                    primaryStage.setScene(new Scene(chat, 300, 350));
                    state = State.ChatMode;
                    //read msg
                    try {
                        recive = new Thread(new ReadThread(socket, ip, port, chatField, msg));
                        recive.start();
                    } catch (Exception e) {
                        System.out.println("Couldn't open thread");
                    }
                    System.out.println(state);
                }

            }
        });



        //send chat msg
        bSend.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(msg != null && !msg.getText().isEmpty()){
                    String messege = nickname + " : " + msg.getText() + "\n";
                    send(messege.getBytes());
                    System.out.println("send");
                }

            }
        });


        //Logout
        bLoggout.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                disconnect();
                primaryStage.close();
            }

        });

    }

    private void disconnect(){
        state = State.Finnished;
        try{
            String s = nickname + " left the chat";
            send(s.getBytes());

            send.join();
            socket.leaveGroup(ip);
            socket.close();
            logger.log(Level.INFO, "Connection closed");
        }

        catch(Exception e){
            System.out.println("Couldn't close socket");
            logger.log(Level.INFO, "Could not close Socket");
        }
        System.out.println(state);
    }

    private boolean connect(){
        try {
            socket = new MulticastSocket(port);
            // Since we are deploying
            socket.setTimeToLive(0);
            //this on localhost only (For a subnet set it as 1)
            socket.joinGroup(ip);
            logger.log(Level.INFO, "Connected to group ip " + ip + " on port " + port);
            return true;
        }catch(Exception e){
            System.out.println("Couldn't create socket");
            logger.log(Level.INFO, "Could not create Socket");
            return false;
        }


    }


    private void send(byte[] data){
        send = new Thread("Send Threat"){
            public void run() {
                DatagramPacket packet = new DatagramPacket(data, data.length, ip, port);
                try {
                    socket.send(packet);
                    logger.log(Level.INFO, "Messege was send");
                } catch (IOException ex) {
                    ex.printStackTrace();
                    logger.log(Level.INFO, "Sending failed");
                }
            }

         };
        send.start();

    }


    public byte[] makeRandomPackage(int range){
        byte[] array = new byte[(int)(Math.random()*range)];
        new Random().nextBytes(array);
        return array;
    }

    class ReadThread implements Runnable {
        private static final int MAX_LEN = 1000;
        private MulticastSocket socket;
        private InetAddress group;
        private int port;
        private TextArea textArea;
        private TextField textField;

        ReadThread(MulticastSocket socket, InetAddress group, int port,TextArea textArea, TextField textField) {
            this.socket = socket;
            this.group = group;
            this.port = port;
            this.textArea = textArea;
            this.textField = textField;
        }

        @Override
        public void run() {
            while (!state.equals(State.Finnished)) {
                byte[] buffer = new byte[ReadThread.MAX_LEN];
                DatagramPacket datagram = new
                        DatagramPacket(buffer, buffer.length, group, port);
                String message;
                try {

                    socket.receive(datagram);

                    message = new
                            String(buffer, 0, datagram.getLength(), "UTF-8");

                    textArea.appendText(message);
                    textField.clear();

                    if(!message.startsWith(nickname))
                        logger.log(Level.INFO, "Message received");

                } catch (IOException e) {
                    System.out.println("Socket closed!");
                    logger.log(Level.INFO, "Socket closed");
                }
            }
        }
    }
}

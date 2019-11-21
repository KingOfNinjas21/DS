import controller.Controller;
import controller.ControllerListener;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.stage.WindowEvent;
import util.LogFileWriter;

public class GUI extends Application implements ControllerListener {
    private static final Logger logger = Logger.getLogger(GUI.class.getName());
    private TextArea chatField;
    
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        logger.addHandler(LogFileWriter.getInstance());
        
        // Listen to Controller Events
        Controller.getInstance().addListener(this);
        
        // Build Login State Scene
        Label nick = new Label();
        Label lPort = new Label();
        Label lIp = new Label();

        TextField inNickname = new TextField();
        TextField inPort = new TextField();
        TextField inIp = new TextField();

        Button bLogin = new Button();

        nick.setText("Nickname:");
        lPort.setText("Group Port:");
        lIp.setText("Group Address:");
        bLogin.setText("Go");

        Pane login = new VBox();
        login.getChildren().add(nick);
        login.getChildren().add(inNickname);
        login.getChildren().add(lIp);
        login.getChildren().add(inIp);
        login.getChildren().add(lPort);
        login.getChildren().add(inPort);
        login.getChildren().add(bLogin);

        // Build Chat State Scene
        chatField = new TextArea();
        Button bSend = new Button();
        Button bLoggout = new Button();

        TextField msg = new TextField();

        chatField.setMaxWidth(390);
        chatField.setMaxHeight(320);

        bSend.setText("Send");
        bLoggout.setText("Logout");
        msg.setPromptText("Type your message here...");
        Pane chat = new VBox();
        chat.getChildren().add(chatField);
        chat.getChildren().add(msg);
        chat.getChildren().add(bSend);
        chat.getChildren().add(bLoggout);

        // Set Initial Scene
        primaryStage.setScene(new Scene(login, 400, 350));
        primaryStage.show();

        // Login Action
        bLogin.setOnAction((ActionEvent e) -> {
            String nickname = inNickname.getText();
            String group = inIp.getText();
            int port = Integer.parseInt(inPort.getText());
            if (Controller.getInstance().connect(nickname, group, port)) {
                primaryStage.setScene(new Scene(chat, 300, 350));
            }
        });

        // Send Command/Message Action
        bSend.setOnAction((ActionEvent e) -> {
            if (msg != null && !msg.getText().isEmpty()) {
                Controller.getInstance().send(msg.getText());
                msg.setText("");
            }
        });

        // Logout Action
        bLoggout.setOnAction((ActionEvent e) -> {
            Controller.getInstance().disconnect();
            primaryStage.close();
        });
        
        // Exit on Close Window
        primaryStage.setOnCloseRequest((WindowEvent e) -> {
            Controller.getInstance().disconnect();
            Platform.exit();
            System.exit(0);
        });
    }

    @Override
    public void onReceiveMessage(String message) {
        if (chatField != null) {
            chatField.appendText(message + "\n");
        }
    }
}

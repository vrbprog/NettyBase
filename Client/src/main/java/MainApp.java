import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class MainApp extends Application {

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    private Stage primaryStage;
    private NettyClient client;

    public LoginController getLoginController() {
        return loginController;
    }
    private LoginController loginController;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        client = new NettyClient(this);
        new Thread(client).start();
        showLoginView();
    }

    @Override
    public void stop() {
        client.clientClose();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public void showLoginView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/login.fxml"));
            AnchorPane root = loader.load();

            loginController = loader.getController();
            loginController.setMainApp(this);

            primaryStage.setTitle("Clouds Storage");
            primaryStage.setResizable(false);
            primaryStage.setScene(new Scene(root));
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showRegisterView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/register.fxml"));
            AnchorPane root = loader.load();

            RegisterController registerController = loader.getController();
            registerController.setMainApp(this);

            primaryStage.setTitle("Clouds Storage");
            primaryStage.setResizable(false);
            primaryStage.setScene(new Scene(root));
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showFileManager() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/fileManager.fxml"));
            AnchorPane root = loader.load();

            FileManagerController fileManagerController = loader.getController();
            fileManagerController.setMainApp(this);

            primaryStage.setTitle("Clouds Storage");
            primaryStage.setResizable(false);
            primaryStage.setScene(new Scene(root));
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public NettyClient getClient() {
        return client;
    }

}

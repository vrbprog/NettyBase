import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

// Класс контроллера окна логирования
public class LoginController implements Initializable {
    private MainApp mainApp;

    @FXML
    private PasswordField passField;

    @FXML
    private TextField fieldEmail;

    @FXML
    private Button buttonLogin;

    @FXML
    private Label labCreateAccount;

    @FXML
    private Label labServerError;

    @FXML
    private Label labErrorAuth;

    @FXML
    void onButtonLogin(ActionEvent event) {

        mainApp.getClient().sendCommand(String.format("<command=login,email=%s,password=%s>",
                fieldEmail.getText().toLowerCase(),passField.getText()));
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        labCreateAccount.setOnMouseClicked(mouseEvent -> createAccount());
        buttonLogin.setOnMouseEntered(mouseEvent -> buttonLogin.setStyle("-fx-background-color: #7EA9FF"));
        //buttonLogin.setOnMouseExited(mouseEvent -> buttonLogin.setStyle("-fx-text-fill: #FFFFFF; -fx-background-color:  #4567E5"));
        buttonLogin.setOnMouseExited(mouseEvent -> buttonLogin.setStyle("-fx-background-color:  #4567E5"));
        labCreateAccount.setOnMouseEntered(mouseEvent -> labCreateAccount.setStyle("-fx-underline: true"));
        labCreateAccount.setOnMouseExited(mouseEvent -> labCreateAccount.setStyle("-fx-underline: false"));

    }

    private void createAccount() {
        mainApp.showRegisterView();
    }

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    public void errorAuth(){
        labErrorAuth.setVisible(true);
    }

}

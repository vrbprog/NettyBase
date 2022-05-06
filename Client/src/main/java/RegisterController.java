import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.apache.commons.validator.routines.EmailValidator;

import java.net.URL;
import java.util.ResourceBundle;

// Класс контроллера окна регистрации
public class RegisterController implements Initializable {
    private MainApp mainApp;

    @FXML
    private Label labLogin;

    @FXML
    private Label labError;

    @FXML
    private TextField fieldEmail;

    @FXML
    private TextField fieldNick;

    @FXML
    private PasswordField fieldFirstPass;

    @FXML
    private PasswordField fieldSecondPass;

    @FXML
    private Button buttonRegister;

    @FXML
    void onButtonRegister(ActionEvent event) {
        if(!isAlphanumeric(fieldNick.getText())){
            fieldNick.clear();
            labError.setText("Nickname must have alphanumeric format");
        }
        else if(!EmailValidator.getInstance().isValid(fieldEmail.getText())){
            fieldEmail.clear();
            labError.setText("Incorrect email format");
        }
        else if(fieldFirstPass.getText().equals(fieldSecondPass.getText())) {
            mainApp.getClient().sendCommand(String.format("<command=signup,name=%s,email=%s,password=%s>",
                    fieldNick.getText().toLowerCase(), fieldEmail.getText().toLowerCase(), fieldFirstPass.getText()));
        }
        else {
            fieldFirstPass.clear();
            fieldSecondPass.clear();
            labError.setText("Fields for passwords don't equals");
        }
    }

    private boolean isAlphanumeric(String str)
    {
        char[] charArray = str.toCharArray();
        for(char c:charArray)
        {
            if (!Character.isLetterOrDigit(c))
                return false;
        }
        return true;
    }

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        labLogin.setOnMouseClicked(mouseEvent -> makeLogin());
        buttonRegister.setOnMouseEntered(mouseEvent -> buttonRegister.setStyle("-fx-background-color: #7EA9FF"));
        buttonRegister.setOnMouseExited(mouseEvent -> buttonRegister.setStyle("-fx-background-color:  #4567E5"));
        labLogin.setOnMouseEntered(mouseEvent -> labLogin.setStyle("-fx-underline: true"));
        labLogin.setOnMouseExited(mouseEvent -> labLogin.setStyle("-fx-underline: false"));
        fieldNick.setOnMouseClicked(mouseEvent -> labError.setText(""));
        fieldEmail.setOnMouseClicked(mouseEvent -> labError.setText(""));
        fieldFirstPass.setOnMouseClicked(mouseEvent -> labError.setText(""));
        fieldSecondPass.setOnMouseClicked(mouseEvent -> labError.setText(""));
    }

    private void makeLogin() {
        mainApp.showLoginView();
    }

    public void showErrorMassage(String error){
        labError.setText(error);
    }
}

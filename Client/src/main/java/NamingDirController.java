import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.io.File;
import java.io.IOException;

public class NamingDirController {

    private MainApp mainApp;

    @FXML
    private TextField fieldNewDir;

    @FXML
    private Button butCreate;

    @FXML
    private Button butCancel;

    @FXML
    void onButtonCreate(ActionEvent event) {

        if(fieldNewDir.getText().length() > 0) {
            if (isAlphanumeric(fieldNewDir.getText())) {
                showCreateDirButton();

                String newDir = mainApp.getFileManagerController().getMyRepoPath() + File.separator + fieldNewDir.getText();
                mainApp.getClient().sendCommand(String.format("<command=makedir,path=%s>", newDir));
            } else {
                fieldNewDir.clear();
                errorNamingDir();
            }
        }
    }

    @FXML
    void onButtonCancel(ActionEvent event) {
        showCreateDirButton();
    }

    private void showCreateDirButton(){
        Node[] nodeNamingDir = new Node[1];

        FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/createDir.fxml"));
        try {
            nodeNamingDir[0] = loader.load();
            mainApp.getFileManagerController().createDirController = loader.getController();
            mainApp.getFileManagerController().createDirController.setMainApp(mainApp);

            mainApp.getFileManagerController().hBoxCreateDir.getChildren().remove(0);
            mainApp.getFileManagerController().hBoxCreateDir.getChildren().add(nodeNamingDir[0]);

            mainApp.getFileManagerController().labNamingDir.setVisible(false);

        } catch (IOException e) {
            e.printStackTrace();
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

    private void errorNamingDir(){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Error naming new directory");
        alert.setContentText("Name directory must have alphanumerics format!");

        alert.showAndWait();
    }

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }
}

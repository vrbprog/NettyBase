import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;

import java.io.IOException;

public class CreateDirController {

    private MainApp mainApp;

    @FXML
    private Button butCreateDir;

    @FXML
    void onButtonCreateDir(ActionEvent event) {

        Node[] nodeNamingDir = new Node[1];

        FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/namingDir.fxml"));
        try {
            nodeNamingDir[0] = loader.load();
            mainApp.getFileManagerController().namingDirController = loader.getController();
            mainApp.getFileManagerController().namingDirController.setMainApp(mainApp);

            mainApp.getFileManagerController().hBoxCreateDir.getChildren().remove(0);
            mainApp.getFileManagerController().hBoxCreateDir.getChildren().add(nodeNamingDir[0]);

            mainApp.getFileManagerController().labNamingDir.setVisible(true);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }


}

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.util.ResourceBundle;

// Контроллер виджета для строки списка файлов кастомного ListView
public class FileItemController implements Initializable {

    @FXML
    private Label itemName;

    @FXML
    private ImageView itemIcon;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    public void setFileItemInfo(String fileName, String fileIconURL) {
        itemName.setText(fileName);
        itemIcon.setImage(new Image(String.valueOf(getClass().getResource(fileIconURL))));
    }
}

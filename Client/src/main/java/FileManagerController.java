import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import model.FileModel;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

// Класс контроллера окна менеджера фалов
public class FileManagerController implements Initializable {
    private MainApp mainApp;
    private Path myPath;
    private Path upLimitPath;

    @FXML
    private ScrollPane clientsFilesScroll;

    @FXML
    private ScrollPane serverFilesScroll;

    @FXML
    private TextField fieldUserDir;

    @FXML
    private TextField fieldServerDir;

    @FXML
    private Button butUserDir;

    @FXML
    private Button butUpload;

    @FXML
    void onButtonUserDir(ActionEvent event) {
        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle("Select user directory");
        File selectFile = dirChooser.showDialog(mainApp.getPrimaryStage());
        if (selectFile != null) {
            fieldUserDir.setText(selectFile.getAbsolutePath());
            myPath = Path.of(selectFile.getAbsolutePath());
            upLimitPath = myPath.toAbsolutePath();
            initClientListFiles(getListFiles(myPath, true));
        }
    }

    @FXML
    void onButtonUpload(ActionEvent event) {
        System.out.println(clientsFilesScroll.isFocused());
    }

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        myPath = Path.of("Client");
        upLimitPath = myPath.toAbsolutePath();
        fieldUserDir.setText(myPath.toAbsolutePath().toString());
        initClientListFiles(getListFiles(myPath, true));

        List<FileModel> serverDir = new ArrayList<>();
        initServerListFiles(serverDir);
    }

    private List<FileModel> getListFiles(Path path, boolean isTop) {
        File f = new File(path.toAbsolutePath().toString());
        String[] filesList = f.list();

        List<FileModel> unsortedDir = new ArrayList<>();
        List<FileModel> sortedDir = new ArrayList<>();
        if (!isTop && (!upLimitPath.equals(path))) {
            sortedDir.add(new FileModel("...", "img/up.png", true, true));
        }

        for (String s : filesList) {
            unsortedDir.add(new FileModel(s, getIcon(path, s), checkDir(path, s), false));
        }

        for (int i = 0; i < unsortedDir.size(); i++) {
            if (unsortedDir.get(i).isDir()) {
                sortedDir.add(unsortedDir.get(i));
                unsortedDir.remove(i--);
            }
        }
        for (int i = 0; i < unsortedDir.size(); i++) {
            sortedDir.add(unsortedDir.get(i));
            unsortedDir.remove(i--);
        }

        return sortedDir;
    }

    private boolean checkDir(Path path, String name) {
        return Files.isDirectory(Path.of(path.toString() + File.separator + name));
    }

    private String getIcon(Path path, String name) {
        if (Files.isDirectory(Path.of(path.toString() + File.separator + name))) {
            return "img" + File.separator + "folder.png";
        } else return "img" + File.separator + "file.png";
    }

    private void initClientListFiles(List<FileModel> list) {
        initListFiles(clientsFilesScroll, list);
    }

    private void initServerListFiles(List<FileModel> list) {
        initListFiles(serverFilesScroll, list);
    }

    private void initListFiles(ScrollPane scroll, List<FileModel> list) {

        boolean[] select = new boolean[list.size()];
        AnchorPane clientsPane = new AnchorPane();
        clientsPane.setStyle("-fx-background-color:  #FFFFFF");
        clientsPane.setPrefWidth(398);
        int numItem = 11;
        int itemHeight = 40;
        if (list.size() > numItem) {
            clientsPane.setPrefHeight(itemHeight * list.size());
        } else {
            clientsPane.setPrefHeight(itemHeight * numItem - 2);
        }

        VBox vBoxClientsFiles = new VBox();
        if (list.size() > numItem) {
            vBoxClientsFiles.setPrefSize(398, itemHeight * list.size());
        } else {
            vBoxClientsFiles.setPrefSize(398, itemHeight * numItem - 2);
        }

        Node[] nodes = new Node[list.size()];

        for (int i = 0; i < nodes.length; i++) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/listItem.fxml"));
                nodes[i] = loader.load();

                FileItemController itemController = loader.getController();
                itemController.setFileItemInfo(list.get(i).getFileName(), list.get(i).getIconURL());

            } catch (IOException e) {
                e.printStackTrace();
            }

            final int j = i;
            nodes[i].setOnMouseEntered(mouseEvent -> {
                if (!select[j]) {
                    nodes[j].setStyle("-fx-background-color:  #4567E5");
                }
            });
            nodes[i].setOnMouseExited(mouseEvent -> {
                if (!select[j]) {
                    nodes[j].setStyle("-fx-background-color:  #FFFFFF");
                }
            });
            nodes[i].setOnMousePressed(mouseEvent -> {
                if (!select[j]) {
                    nodes[j].setStyle("-fx-background-color:  #7EA9FF");
                } else {
                    nodes[j].setStyle("-fx-background-color:  #FFFFFF");
                }
                select[j] = !select[j];
            });
            nodes[i].setOnMouseClicked(MouseEvent -> {
                if (MouseEvent.getClickCount() % 2 == 0) {
                    if (checkDir(myPath, list.get(j).getFileName())) {
                        if (!list.get(j).isUpperDir()) {
                            myPath = Path.of(myPath.toString() + File.separator + list.get(j).getFileName());
                            fieldUserDir.setText(myPath.toAbsolutePath().toString());
                            initClientListFiles(getListFiles(myPath, false));
                        } else {
                            int endPath = myPath.toAbsolutePath().toString().lastIndexOf(File.separator);
                            myPath = Path.of(myPath.toAbsolutePath().toString().substring(0, endPath));
                            fieldUserDir.setText(myPath.toAbsolutePath().toString());
                            initClientListFiles(getListFiles(myPath, false));
                        }
                    }
                }
            });

            vBoxClientsFiles.getChildren().add(nodes[i]);
        }

        clientsPane.getChildren().add(vBoxClientsFiles);
        scroll.setContent(clientsPane);
    }
}

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
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
    private Path myRepoPath;
    private List<FileModel> currentClientDir;
    private List<FileModel> currentServerDir;
    private Node[] clientsNodes;
    private Node[] serverNodes;
    private boolean[] selectClientNodes;
    private boolean[] selectServerNodes;
    public CreateDirController createDirController;
    public NamingDirController namingDirController;
    private int levelLimit;
    private int currentLevel = 0;

    @FXML
    private ScrollPane clientsFilesScroll;

    @FXML
    private ScrollPane serverFilesScroll;

    @FXML
    public HBox hBoxCreateDir;

    @FXML
    private TextField fieldUserDir;

    @FXML
    private TextField fieldServerDir;

    @FXML
    private ProgressBar progressSize;

    @FXML
    public Label labNamingDir;

    @FXML
    private Label labUsedSize;

    @FXML
    private Label labLimit;

    @FXML
    private Text textPersent;

    @FXML
    private Button butUserDir;

    @FXML
    private Button butUpload;

    @FXML
    private Button butDelete;

    @FXML
    private Button butDownload;

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
    void onButtonUpload(ActionEvent event) throws IOException {
        for (int i = 0; i < currentClientDir.size(); i++) {
            if (currentClientDir.get(i).isSelect()) {
                if (!currentClientDir.get(i).isDir()) {

                    mainApp.getClient().sendCommand(String.format("<command=upload,path=%s,size=%d>",
                            myRepoPath + File.separator + currentClientDir.get(i).getFileName(),
                            Files.size(Path.of(myPath.toAbsolutePath() + File.separator + currentClientDir.get(i).getFileName()))));

                    mainApp.getClient().sendFile(myPath.toAbsolutePath() + File.separator + currentClientDir.get(i).getFileName());
                }
                currentClientDir.get(i).setSelect(false);
                clientsNodes[i].setStyle("-fx-background-color:  #FFFFFF");
                selectClientNodes[i] = false;
            }
        }
    }

    @FXML
    void onButtonDownload(ActionEvent event) {
        for (int i = 0; i < currentServerDir.size(); i++) {
            if (currentServerDir.get(i).isSelect()) {
                if (!currentServerDir.get(i).isDir()) {

                    mainApp.getClient().sendCommand(String.format("<command=download,path=%s>",
                            myRepoPath + File.separator + currentServerDir.get(i).getFileName()));
                }
            }

            currentServerDir.get(i).setSelect(false);
            serverNodes[i].setStyle("-fx-background-color:  #FFFFFF");
            selectServerNodes[i] = false;
        }
    }

    @FXML
    void onButtonDelete(ActionEvent event) {
        for (int i = 0; i < currentServerDir.size(); i++) {
            if (currentServerDir.get(i).isSelect()) {
                //if (!currentServerDir.get(i).isDir()) {
                    mainApp.getClient().sendCommand(String.format("<command=delete,path=%s>",
                            myRepoPath + File.separator + currentServerDir.get(i).getFileName()));
                //}
            }

            currentServerDir.get(i).setSelect(false);
            serverNodes[i].setStyle("-fx-background-color:  #FFFFFF");
            selectServerNodes[i] = false;
        }
    }

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        myPath = Path.of("Client").toAbsolutePath();
        upLimitPath = myPath;
        fieldUserDir.setText(myPath.toAbsolutePath().toString());
        initClientListFiles(getListFiles(myPath, true));

        List<FileModel> serverDir = new ArrayList<>();
        initServerListFiles(serverDir);
        initCreateDirNode();
    }

    public void updateClientListFiles() {
        initClientListFiles(getListFiles(myPath.toAbsolutePath(), false));
    }

    private List<FileModel> getListFiles(Path path, boolean isTop) {
        File f = new File(path.toAbsolutePath().toString());
        String[] filesList = f.list();

        List<FileModel> unsortedDir = new ArrayList<>();

        List<FileModel> sortedDir = new ArrayList<>();
        if (!isTop && (!upLimitPath.equals(path))) {
            sortedDir.add(new FileModel("...", "img/up.png", true, true, false));
        }

        for (String s : filesList) {
            unsortedDir.add(new FileModel(s, getIcon(path, s), checkDir(path, s), false, false));
        }
        return ClientExecutor.sortingListFile(unsortedDir, sortedDir);
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
        currentClientDir = list;
        initListFiles(clientsFilesScroll, list, false);
    }

    public void initServerListFiles(List<FileModel> list) {
        currentServerDir = list;
        initListFiles(serverFilesScroll, list, true);
    }

    public void initCreateDirNode() {
        Node[] nodeCreateDir = new Node[1];

        FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/createDir.fxml"));
        try {
            nodeCreateDir[0] = loader.load();
            createDirController = loader.getController();

            hBoxCreateDir.getChildren().removeAll();
            hBoxCreateDir.getChildren().add(nodeCreateDir[0]);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initListFiles(ScrollPane scroll, List<FileModel> list, boolean isServerList) {

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
                    list.get(j).setSelect(true);
                } else {
                    nodes[j].setStyle("-fx-background-color:  #FFFFFF");
                    list.get(j).setSelect(false);
                }
                select[j] = !select[j];
            });
            nodes[i].setOnMouseClicked(MouseEvent -> {
                if (MouseEvent.getClickCount() % 2 == 0) {
                    if (list.get(j).isDir()) {
                        // Обрабатываем клик на строке с директорией на клиентской файловой системе
                        if (!isServerList) {
                            // Возвращаемся в родительскую директорию
                            if (list.get(j).isUpperDir()) {
                                myPath = myPath.getParent();
                                fieldUserDir.setText(myPath.toAbsolutePath().toString());
                                initClientListFiles(getListFiles(myPath, false));
                            }
                            // Открываем директорию в пользовательском каталоге
                            else {
                                myPath = Path.of(myPath.toString() + File.separator + list.get(j).getFileName());
                                fieldUserDir.setText(myPath.toAbsolutePath().toString());
                                initClientListFiles(getListFiles(myPath, false));
                            }
                        }
                        // Обрабатываем клик на строке с директорией в репозитории клиента
                        else {
                            // Открываем директоррию в репозитории
                            if (!list.get(j).isUpperDir()) {
                                mainApp.getClient().sendCommand(String.format("<command=getlist,path=%s>",
                                        myRepoPath + File.separator + list.get(j).getFileName()));
                                currentLevel++;
                            }
                            // Переходим в родительскую директорию в репозитории
                            else {
                                mainApp.getClient().sendCommand(String.format("<command=getlist,path=%s>",
                                        myRepoPath.getParent()));
                                currentLevel--;
                            }
                        }
                    }
                }
            });

            vBoxClientsFiles.getChildren().add(nodes[i]);
        }

        clientsPane.getChildren().add(vBoxClientsFiles);
        clientsPane.setFocusTraversable(false);
        vBoxClientsFiles.setFocusTraversable(false);
        scroll.setContent(clientsPane);
        if (isServerList) {
            serverNodes = nodes;
            selectServerNodes = select;
        } else {
            clientsNodes = nodes;
            selectClientNodes = select;
        }

        if(currentLevel > 0) {
            createDirController.butCreateDir.setDisable(currentLevel == levelLimit);
        }
    }

    public void setMyRepoPath(Path myRepoPath) {
        this.myRepoPath = myRepoPath;
        fieldServerDir.setText(myRepoPath.toString());
    }

    public Path getMyRepoPath() {
        return myRepoPath;
    }

    public Path getMyPath() {
        return myPath;
    }

    public void setCurrentUserSize(String size) {
        labUsedSize.setText(size + " Kb");
    }

    public void setCurrentUserLimit(String limit) {
        labLimit.setText(limit + " Kb");
    }

    public void setProgress(double progress) {
        progressSize.setProgress(progress);
    }

    public void setPersent(String per) {
        textPersent.setText(per);
    }

    public void setUserProfile(String userSize, String limitSize, String level) {

        setCurrentUserSize(userSize);
        setCurrentUserLimit(limitSize);
        setProgress(Double.parseDouble(userSize) / Double.parseDouble(limitSize));
        setPersent(String.format("%3.2f %%",
                100 * Double.parseDouble(userSize) / Double.parseDouble(limitSize)));
        setUserLevelLimit(Integer.parseInt(level));
    }

//    private boolean isNotEmptyDirectory(String dir) throws IOException {
//        Path path = Path.of(dir);
//        try (var entries = Files.list(path)) {
//            return entries.findFirst().isPresent();
//        }
//    }

    public void setUserLevelLimit(int limit){
        levelLimit = limit;
    }

    public String getUserLevelLimit(){
        return String.valueOf(levelLimit);
    }

    public void setUserCurrentLevel(int limit){
        levelLimit = limit;
    }
}

import io.netty.channel.ChannelHandlerContext;
import javafx.application.Platform;
import model.FileModel;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

// Класс обработчика выполняемых команд после получения ответов от сервера
public class ClientExecutor {

    public static StateChannelRead Execute(Map<String, String> params, ChannelHandlerContext ctx, MainApp mainApp) {
        String res;
        switch (getCommand(params)) {
            case NONE:
                System.out.println("No command");
                return StateChannelRead.WAIT_META_DATA;

            case LOGIN:
                System.out.println("Login");
                res = params.get("result");
                if ("successful".equals(res)) {
                    Platform.runLater(() -> {
                        mainApp.showFileManager(params.get("used"), params.get("limit"), params.get("level"));
                    });
                } else if ("user_not_fined".equals(res)) {
                    Platform.runLater(() -> {
                        mainApp.getLoginController().errorAuth();
                    });
                }
                return StateChannelRead.WAIT_META_DATA;

            case SIGN_UP:
                res = params.get("result");
                if ("successful".equals(res)) {
                    Platform.runLater(() -> {
                        mainApp.showFileManager(params.get("used"), params.get("limit"), params.get("level"));
                    });
                } else {
                    if ("busy_name".equals(res)) {
                        showErrorRegister(mainApp, "User with this name is already registered");
                    } else if ("busy_email".equals(res)) {
                        showErrorRegister(mainApp, "User with this email is already registered");
                    }
                }
                return StateChannelRead.WAIT_META_DATA;

            case FILE_LIST:
                final String curDir = params.get("path");
                if (curDir.length() > 0) {
                    Platform.runLater(() -> {
                        mainApp.getFileManagerController().setMyRepoPath(Path.of(curDir));
                    });
                    res = params.get("list");
                    boolean isDir;
                    List<FileModel> listDir = new ArrayList<>();
                    Map<String, String> filesList = new HashMap<>();
                    if (res != null) {
                        if (curDir.contains(File.separator)) {
                            listDir.add(new FileModel("...",
                                    "img" + File.separator + "up.png", true, true, false));
                        }
                        String[] keyValueParamsArray = res.split("&");
                        for (String rawKeyValue : keyValueParamsArray) {
                            String[] keyValueArr = rawKeyValue.split(":");
                            if (keyValueArr.length > 1) {
                                filesList.put(keyValueArr[0], keyValueArr[1]);
                                isDir = "d".equals(keyValueArr[1]);
                                listDir.add(new FileModel(keyValueArr[0], getIcon(isDir), isDir, false, false));
                            }
                        }
                    } else {
                        listDir.add(new FileModel("...",
                                "img" + File.separator + "up.png", true, true, false));
                    }
                    Platform.runLater(() -> {
                        List<FileModel> sortedList = new ArrayList<>();
                        mainApp.getFileManagerController().initServerListFiles(sortingListFile(listDir, sortedList));
                        mainApp.getFileManagerController().setUserProfile(params.get("used"), params.get("limit"),
                                mainApp.getFileManagerController().getUserLevelLimit());
                    });
                }
                return StateChannelRead.WAIT_META_DATA;

            case DOWNLOAD:
                return StateChannelRead.CREATE_FILE;
        }
        return StateChannelRead.WAIT_META_DATA;
    }

    private static void showErrorRegister(MainApp mainApp, String error) {
        Platform.runLater(() -> {
            mainApp.getRegisterController().showErrorMassage(error);
        });
    }

    public static List<FileModel> sortingListFile(List<FileModel> list, List<FileModel> sortedList) {

        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).isDir()) {
                sortedList.add(list.get(i));
                list.remove(i--);
            }
        }
        for (int i = 0; i < list.size(); i++) {
            sortedList.add(list.get(i));
            list.remove(i--);
        }

        return sortedList;
    }

    // Определение типа принятой от сервера команды
    private static CommandType getCommand(Map<String, String> params) {
        String com = params.get("command");
        if ("login".equals(com)) return CommandType.LOGIN;
        else if ("signup".equals(com)) return CommandType.SIGN_UP;
        else if ("filelist".equals(com)) return CommandType.FILE_LIST;
        else if ("download".equals(com)) return CommandType.DOWNLOAD;
        else return CommandType.NONE;
    }

    private static String getIcon(boolean isDir) {
        if (isDir) {
            return "img" + File.separator + "folder.png";
        } else return "img" + File.separator + "file.png";
    }
}

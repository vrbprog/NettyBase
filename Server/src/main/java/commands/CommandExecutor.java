package commands;

import dataBase.ConfigDB;
import dataBase.DataBaseHandler;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import model.User;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

// Класс обработчика выполняемых команд
public class CommandExecutor {
    private static final int FILE_BLOCK_SIZE = 1024 * 1024;

    public static StateChannelRead Execute(Map<String, String> params, DataBaseHandler db, ChannelHandlerContext ctx, User user) {
        final String SERVER_DIR = "Server" + File.separator + "Repositories" + File.separator;
        ResultSet res;
        try {
            switch (getCommand(params)) {
                case NONE:
                    System.out.println("No command");
                    return StateChannelRead.WAIT_META_DATA;

                // Команда авторизации
                case LOGIN:
                    // Проверка получения параметров имейла и пароля
                    if (checkEmailAndPassword(params, user)) {
                        // Выборка пользователя
                        res = db.getUserFromBase(user.getEmail(), user.getPassword());
                        if (res.next()) {
                            // Выборка из
                            user.setName(res.getString(ConfigDB.USER_NAME));
                            user.setUsedSize(res.getInt(ConfigDB.USER_USED_SIZE));
                            user.setLimitSize(res.getInt(ConfigDB.USER_LIMIT_SIZE));
                            user.setLevelSubSir(res.getInt(ConfigDB.USER_DIR_LEV));
                            log("User " + user.getName() + " login on server", user);
                            //sendAnswerToClient(ctx, "<command=login,result=successful>");
                            sendAnswerToClient(ctx, String.format("<command=login,result=successful,used=%d,limit=%d,level=%d>",
                                    user.getUsedSize(), user.getLimitSize(), user.getLevelSubSir()));
                            sendAnswerToClient(ctx, createUserListRepository(user.getName(), user));
                            // Отсутствие пользователя с введенными параметрами в БД
                        } else {
                            log("User with define params not fined", user);
                            sendAnswerToClient(ctx, "<command=login,result=user_not_fined>");
                        }
                        // Отсутствие полей для логирования в метаданных
                    } else {
                        log("Not define authorisation params", user);
                        sendAnswerToClient(ctx, "<command=login,result=not_define_authorisation_params>");
                    }
                    return StateChannelRead.WAIT_META_DATA;

                // Команда регистрации
                case SIGN_UP:
                    // Проверка наличия всех полей
                    if (checkUserParams(params, user)) {
                        res = db.CheckUserName(user.getName());
                        // Проверка на наличие пользователя с данным именем
                        if (res.next()) {
                            log("SignUp: User name " + "\"" + user.getName() + "\"" + " is already in the database", user);
                            sendAnswerToClient(ctx, "<command=signup,result=busy_name>");
                        } else {
                            res = db.CheckUserEmail(user.getEmail());
                            // Проверка на наличие пользователя с данным имейлом
                            if (res.next()) {
                                log("SignUp: User email " + "\"" + user.getEmail() + "\"" + " is already in the database", user);
                                sendAnswerToClient(ctx, "<command=signup,result=busy_email>");
                            }
                            // Успешная регистрация пользователя
                            else {
                                try {
                                    db.insertUserToBase(user); // Внесение пользователя в БД
                                    user.setLimitSize(ConfigDB.DEFAULT_LIMIT_SIZE);
                                    user.setUsedSize(0);
                                    user.setLevelSubSir(ConfigDB.DEFAULT_DIR_LEV);
                                    log("SignUp: User " + "\"" + user.getName() + "\"" + " registered in the database", user);
                                    sendAnswerToClient(ctx, String.format("<command=signup,result=successful,used=%d,limit=%d,level=%d>",
                                            user.getUsedSize(), user.getLimitSize(), user.getLevelSubSir()));
                                    createRepository(user.getName()); // Выделение пользователю места на сервере
                                    sendAnswerToClient(ctx, createUserListRepository(user.getName(), user));
                                } catch (SQLException throwables) {
                                    log("Error write to database", user);
                                    throwables.printStackTrace();
                                }
                            }
                        }
                    }
                    return StateChannelRead.WAIT_META_DATA;

                // Команда загрузки файла на сервер
                case UP_LOAD:
                    log("User " + user.getName() + " upload file "
                            + params.get("path") + " on Serer", user);

                    if (checkAuthorization(user, params))
                        return StateChannelRead.CREATE_FILE;
                    else
                        return StateChannelRead.WAIT_META_DATA;

                // Команда запроса списка фалов в указанной директории
                case GET_LIST:
                    if (checkAuthorization(user, params))
                        sendAnswerToClient(ctx, createUserListRepository(params.get("path"), user));
                    return StateChannelRead.WAIT_META_DATA;

                // Команда создания новой директории
                case MAKE_DIR:

                    String dirPath;
                    if (checkAuthorization(user, params)) {
                        System.out.print("MAKE_DIR ");
                        System.out.println(params.get("path"));

                        dirPath = SERVER_DIR + params.get("path");
                        Path newDir = Path.of(dirPath);
                        Files.createDirectories(newDir);
                        updateCurrentListRepository(dirPath, ctx, user);
                    }
                    return StateChannelRead.WAIT_META_DATA;

                // Команда удаления файла
                case DELETE:
                    if (checkAuthorization(user, params)) {
                        System.out.print("DELETE ");
                        System.out.println(params.get("path"));

                        String filePath = SERVER_DIR + params.get("path");
                        Path delPath = Path.of(filePath);


                        long delSize = Files.size(delPath);
                        int size = (int) delSize / 1024;
                        if (delSize % 1024 > 0) size++;
                        int newSize = user.getUsedSize() - size;

                        Files.delete(delPath);
                        user.setUsedSize(newSize);
                        db.updateUserCurrentSize(newSize, user);
                        updateCurrentListRepository(filePath, ctx, user);
                    }
                    return StateChannelRead.WAIT_META_DATA;

                // Команда загрузки файла из репозитория
                case DOWNLOAD:
                    if (checkAuthorization(user, params)) {
                        String fileName;
                        dirPath = params.get("path");
                        int endPath = dirPath.lastIndexOf(File.separator);
                        if (endPath > 0) {
                            fileName = dirPath.substring(endPath + 1);

                            String download = SERVER_DIR + params.get("path");
                            sendAnswerToClient(ctx, String.format("<command=download,path=%s,size=%d>",
                                    fileName, Files.size(Path.of(download))));

                            try (FileChannel inputChannel = FileChannel.open(Path.of(download))) {
                                ByteBuffer buf = ByteBuffer.allocate(FILE_BLOCK_SIZE);
                                while (inputChannel.read(buf) > 0) {
                                    buf.flip();
                                    ctx.writeAndFlush(Unpooled.wrappedBuffer(buf));
                                    buf.clear();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    return StateChannelRead.WAIT_META_DATA;
            }
        } catch (DirectoryNotEmptyException e) {
            System.out.println("Directory not empty!!!");
        } catch (SQLException | IOException throwable) {
            throwable.printStackTrace();
        }
        return StateChannelRead.WAIT_META_DATA;
    }

    // Создание листа корневого каталога клиента
    public static String createUserListRepository(String repoPath, User user) {
        final String SERVER_DIR = "Server" + File.separator + "Repositories" + File.separator;
        Path myPath = Path.of(SERVER_DIR + repoPath);

        File f = new File(myPath.toAbsolutePath().toString());
        String[] filesList = f.list();

        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < filesList.length; i++) {
            if (i > 0) stringBuffer.append("&");
            stringBuffer.append(filesList[i] + ":");
            if (Files.isDirectory(Path.of(myPath.toString() + File.separator + filesList[i]))) {
                stringBuffer.append("d");
            } else {
                stringBuffer.append("f");
            }
        }
        String listCommand = String.format("<command=filelist,path=%s,list=%s,used=%d,limit=%d>",
                repoPath, stringBuffer.toString(), user.getUsedSize(), user.getLimitSize());
        return listCommand;
    }

    // Создание репозитория для нового пользователя
    private static void createRepository(String dirName) {
        final String SERVER_DIR = "Server" + File.separator + "Repositories" + File.separator;
        Path myPath = Path.of(SERVER_DIR + dirName);
        try {
            Files.createDirectories(myPath);
            Files.createFile(Path.of(myPath.toString() + File.separator + "readme.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Определение типа принятой от клиента команды
    private static CommandType getCommand(Map<String, String> params) {
        String com = params.get("command");
        if ("login".equals(com)) return CommandType.LOGIN;
        else if ("signup".equals(com)) return CommandType.SIGN_UP;
        else if ("upload".equals(com)) return CommandType.UP_LOAD;
        else if ("getlist".equals(com)) return CommandType.GET_LIST;
        else if ("makedir".equals(com)) return CommandType.MAKE_DIR;
        else if ("delete".equals(com)) return CommandType.DELETE;
        else if ("download".equals(com)) return CommandType.DOWNLOAD;
        else return CommandType.NONE;
    }

    // Проверка получения параметров имейла и пароля
    private static boolean checkEmailAndPassword(Map<String, String> params, User user) {
        String email = params.get("email");
        String password = params.get("password");
        if (email != null && password != null) {
            user.setEmail(email);
            user.setPassword(password);
            return true;
        } else return false;
    }

    // Проверка получения параметров регистрации
    private static boolean checkUserParams(Map<String, String> params, User user) {
        String name = params.get("name");
        String email = params.get("email");
        String password = params.get("password");
        if (email != null && password != null && name != null) {
            user.setName(name);
            user.setEmail(email);
            user.setPassword(password);
            return true;
        } else return false;
    }

    // Логирование пользователей и выдача ответа клиентам
    private static void log(String serverLog, User user) {
        System.out.printf("%s %s\n", serverLog, user.getAddress());
    }

    public static void sendAnswerToClient(ChannelHandlerContext ctx, String clientsAnswer) {
        ctx.writeAndFlush(clientsAnswer);
    }

    // Обновление списка файлов текущей директории
    public static void updateCurrentListRepository(String path, ChannelHandlerContext ctx, User user) {
        int endPath = path.lastIndexOf(File.separator);
        String curDir = path.substring(0, endPath);
        endPath = curDir.indexOf(File.separator, 10);
        if (endPath > 0) {
            CommandExecutor.sendAnswerToClient(ctx,
                    CommandExecutor.createUserListRepository(curDir.substring(endPath + 1), user));
        }
    }

    // Получение корневой директории из параметра пути в команде
    private static Path getRootRepo(Path path) {
        Path next = path;
        while (true){
            if(next.getParent() != null)
                next = next.getParent();
            else break;
        }
        return next;
    }

    // Проверка соответствия имени текущего пользователя и имени репозитория в котором будут производиться действия
    private static boolean checkAuthorization(User user, Map<String, String> params) {
        return user.getName().equals(getRootRepo(Path.of(params.get("path"))).toString());
    }

}

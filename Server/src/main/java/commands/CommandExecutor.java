package commands;

import dataBase.ConfigDB;
import dataBase.DataBaseHandler;
import io.netty.channel.ChannelHandlerContext;
import model.User;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

// Класс обработчика выполняемых команд
public class CommandExecutor {

    public static StateChannelRead Execute(Map<String, String> params, DataBaseHandler db, ChannelHandlerContext ctx, User user) {
        ResultSet res;
        try {
            switch (getCommand(params)) {
                case NONE:
                    System.out.println("No command");
                    return StateChannelRead.WAIT_META_DATA;

                // Команда логирования
                case LOGIN:
                    // Проверка получения параметров имейла и пароля
                    if (checkEmailAndPassword(params, user)) {
                        // Выборка пользователя
                        res = db.getUserFromBase(user.getEmail(), user.getPassword());
                        if (res.next()) {
                            // Выборка из
                            user.setName(res.getString(ConfigDB.USER_NAME));
                            log("User " + user.getName() + " login on server",
                                    "<command=login,result=successful>", ctx, user);
                            //TODO Trance Top User directory
                        // Отсутствие пользователя с введенными параметрами в БД
                        } else {
                            log("User with define params not fined",
                                    "<command=login,result=user_not_fined>", ctx, user);
                        }
                    // Отсутствие полей для логирования в метаданных
                    } else {
                        log("Not define authorisation params",
                                "<command=login,result=not_define_authorisation_params>", ctx, user);
                    }
                    return StateChannelRead.WAIT_META_DATA;

                // Команда регистрации
                case SIGN_UP:
                    // Проверка наличия всех полей
                    if (checkUserParams(params, user)) {
                        res = db.CheckUserName(user.getName());
                        // Проверка на наличие пользователя с данным именем
                        if (res.next()) {
                            log("SignUp: User name " + "\"" + user.getName() + "\"" + " is already in the database",
                                    "<command=signup,result=busy_name>", ctx, user);
                        } else {
                            res = db.CheckUserEmail(user.getEmail());
                            // Проверка на наличие пользователя с данным имейлом
                            if (res.next()) {
                                log("SignUp: User email " + "\"" + user.getEmail() + "\"" + " is already in the database",
                                        "<command=signup,result=busy_email>", ctx, user);
                            }
                            // Успешная регистрация пользователя
                            else {
                                log("SignUp: User " + "\"" + user.getName() + "\"" + " registered in the database",
                                        "<command=signup,result=successful>", ctx, user);

                                db.insertUserToBase(user); // Внесение пользователя в БД
                                createRepository(user.getName()); // Выделение пользователю места на сервере
                            }
                        }
                    }
                    return StateChannelRead.WAIT_META_DATA;

                // Команда загрузки файла на сервер
                case UP_LOAD:

                    return StateChannelRead.READING_FILE;
            }
        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }
        return StateChannelRead.WAIT_META_DATA;
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
    private static void log(String serverLog, String clientAnswer, ChannelHandlerContext ctx, User user) {
        System.out.printf("%s %s\n", serverLog, user.getAddress());
        ctx.writeAndFlush(clientAnswer);
    }

}

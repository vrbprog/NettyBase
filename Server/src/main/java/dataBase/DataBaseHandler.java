package dataBase;

import model.User;

import java.sql.*;

// Класс с методами работы с БД
public class DataBaseHandler extends ConfigDB {

    private final static String SELECT_AUTH = "SELECT * FROM users WHERE email=? AND password=?";
    private final static String ADD_USER = "INSERT INTO " + ConfigDB.USER_TABLE + "(" +
            ConfigDB.USER_NAME + "," + ConfigDB.USER_EMAIL + "," + ConfigDB.USER_PASS + "," +
            ConfigDB.USER_DIR_LEV + "," + ConfigDB.USER_LIMIT_SIZE + "," + ConfigDB.USER_USED_SIZE + ")" +
            " VALUES(?,?,?,?,?,?)";
    private final static String CHECK_USER_NAME = "SELECT * FROM users WHERE name=?";
    private final static String CHECK_USER_EMAIL = "SELECT * FROM users WHERE email=?";
    private final static String UPDATE_USER_USED = "UPDATE " + ConfigDB.USER_TABLE + " SET " +
            ConfigDB.USER_USED_SIZE + "=?" + " WHERE " + ConfigDB.USER_NAME + "=?";
    private Connection dbConnection;

    // Подключение БД
    public Connection getDbConnection() throws SQLException, ClassNotFoundException {
        if (dbConnection == null) {
            Class.forName("org.sqlite.JDBC");
            dbConnection = DriverManager.getConnection(
                    "jdbc:sqlite:" + ConfigDB.DB_NAME);
        }
        return dbConnection;
    }

    // Выборка пользователя при авторизации
    public ResultSet getUserFromBase(String email, String pass){
        ResultSet rs = null;
        try {
            PreparedStatement prSt = getDbConnection().prepareStatement(SELECT_AUTH);
            prSt.setString(1,email);
            prSt.setString(2,pass);
            rs = prSt.executeQuery();
        } catch (SQLException | ClassNotFoundException throwable) {
            throwable.printStackTrace();
        }
        return rs;
    }

    // Регистрация пользователя в БД
    public void insertUserToBase(User user) throws SQLException {
        try {
            PreparedStatement prSt = getDbConnection().prepareStatement(ADD_USER);
            prSt.setString(1, user.getName());
            prSt.setString(2, user.getEmail());
            prSt.setString(3, user.getPassword());
            prSt.setInt(4, ConfigDB.DEFAULT_DIR_LEV);
            prSt.setInt(5, ConfigDB.DEFAULT_LIMIT_SIZE);
            prSt.setInt(6, 0);
            prSt.executeUpdate();
        } catch (ClassNotFoundException throwable) {
            throwable.printStackTrace();
        }
    }

    // Выборка из БД пользователя с заданным именем
    public ResultSet CheckUserName(String name){
        return checkUserFieldFromBase(name, CHECK_USER_NAME);
    }

    // Выборка из БД пользователя с заданным email
    public ResultSet CheckUserEmail(String email){
        return checkUserFieldFromBase(email, CHECK_USER_EMAIL);
    }

    // Выборка из БД поля с заданным значением
    public ResultSet checkUserFieldFromBase(String field, String select){
        ResultSet rs = null;
        try {
            PreparedStatement prSt = getDbConnection().prepareStatement(select);
            prSt.setString(1,field);
            rs = prSt.executeQuery();
        } catch (SQLException | ClassNotFoundException throwable) {
            throwable.printStackTrace();
        }
        return rs;
    }

    // Выборка из БД поля с заданным значением
    public void updateUserCurrentSize(Integer size, User user) throws SQLException {
        try {
            PreparedStatement prSt = getDbConnection().prepareStatement(UPDATE_USER_USED);
            prSt.setInt(1, size);
            prSt.setString(2, user.getName());
            prSt.executeUpdate();
        } catch (ClassNotFoundException throwable) {
            throwable.printStackTrace();
        }
    }

    public void close() throws SQLException {
        dbConnection.close();
    }
}

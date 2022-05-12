package dataBase;

// Класс с наименованием БД, таблиц и колонок в БД
public class ConfigDB {
    public static final String DB_NAME = "users.db";

    public static final String USER_TABLE = "users";
    public static final String USER_NAME = "name";
    public static final String USER_EMAIL = "email";
    public static final String USER_PASS = "password";
    public static final String USER_DIR_LEV = "subdir";
    public static final String USER_LIMIT_SIZE = "limitsize";
    public static final String USER_USED_SIZE = "used";
    public static final Integer DEFAULT_DIR_LEV = 3;
    public static final Integer DEFAULT_LIMIT_SIZE = 100_000;

}

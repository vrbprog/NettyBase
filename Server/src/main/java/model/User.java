package model;

import java.net.SocketAddress;

// Класс с параметрами подключившегося пользователя
public class User {
    private String name;
    private String email;
    private String password;
    private SocketAddress address;
    private Integer limitSize;
    private Integer usedSize;
    private Integer levelSubSir;

    public User() {
        this.name = "anonymous_user";
        this.email = "anonymous_email";
        this.password = "anonymous_password";
        this.usedSize = 0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public SocketAddress getAddress() {
        return address;
    }

    public void setAddress(SocketAddress address) {
        this.address = address;
    }

    public Integer getLimitSize() {
        return limitSize;
    }

    public void setLimitSize(Integer limitSize) {
        this.limitSize = limitSize;
    }

    public Integer getUsedSize() {
        return usedSize;
    }

    public void setUsedSize(Integer usedSize) {
        this.usedSize = usedSize;
    }

    public Integer getLevelSubSir() {
        return levelSubSir;
    }

    public void setLevelSubSir(Integer levelSubSir) {
        this.levelSubSir = levelSubSir;
    }
}

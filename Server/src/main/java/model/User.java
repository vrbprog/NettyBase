package model;

import java.net.SocketAddress;

// Класс с параметрами подключившегося пользователя
public class User {
    private String name;
    private String email;
    private String password;
    private SocketAddress address;

    public User() {
        this.name = "user";
        this.email = "email";
        this.password = "password";
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
}

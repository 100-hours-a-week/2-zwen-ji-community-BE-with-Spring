package com.example.project.dto.request;

public class LoginRequest {
    private String email;
    private String password;

    // 기본 생성자 (JSON 역직렬화에 필요)
    public LoginRequest() {}

    // Getter와 Setter
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
}
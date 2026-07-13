package com.example.dateon.Models;
import lombok.Data;
@Data
public class UserCreateDTO {
    private String name;
    private String mail;
    private double number;
    private String gender;
    private String dob;
    private String password;
}

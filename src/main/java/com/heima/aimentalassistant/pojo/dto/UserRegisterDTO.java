package com.heima.aimentalassistant.pojo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data

public class UserRegisterDTO {
    @NotBlank(message = "用户名不能为空")
    @Size(min=3,max=50,message = "用户名长度必须在3-50之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$",message = "用户名只能包含字母、数字和下划线")
    private String username;

    @Size(max=50,message = "昵称长度必须在50以下")
    private String nickname;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式错误")
    @Size(max=100,message = "邮箱长度必须在100以下")
    private String email;

    @Pattern(regexp = "^1[3-9]\\d{9}$",message = "手机号格式错误")
    private String phone;

    @NotBlank(message = "密码不能为空")
    @Size(min=6,max=20,message = "密码长度必须在6-20之间")
    private String password;

    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;


    private Integer gender;
    private Integer userType =1;

    private LocalDate birthday;


}

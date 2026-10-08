package com.heima.aimentalassistant.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.heima.aimentalassistant.common.enums.UserStatus;
import com.heima.aimentalassistant.common.enums.UserType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("user")
@Builder
public class User {
    @TableId("id")
    private Long id;

    @NotBlank(message = "用户名不能为空")
    @Size(min=3,max=50,message = "用户名长度必须在3-50之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$",message = "用户名只能包含字母、数字和下划线")
    private String username;

    private String password;


    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式错误")
    private String email;

    @Size(max=50,message = "昵称长度必须在50以下")
    private String nickname;

    @Size(max=255,message = "头像长度必须在255以下")
    private String avatar;

    @Pattern(regexp = "^1[3-9]\\d{9}$",message = "手机号格式错误")
    private String phone;
    private Integer gender;
    private LocalDate birthday;

    //用户类型 1-普通用户 2-管理员
    @TableField("user_type")
    private Integer userType;

    private Integer status;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;

    public Boolean isUser(){
        return UserType.USER.getCode().equals(this.userType);
    }
    public Boolean isActive(){

        return UserStatus.NORMAL.getCode().equals(this.status);
    }

    public String getUserTypeDisplayName(){
        try{
            return UserType.fromCode(userType).getDescription();
        }catch (Exception e){
            return "未知用户类型";
        }
    }

    public String getStatusDisplayName(){
        try{
            return UserStatus.fromCode(status).getDescription();
        }catch (Exception e){
            return "未知状态";
        }
    }

    public String getDisplayName(){
        return nickname != null && !nickname.trim().isEmpty()? nickname : username;
    }
}

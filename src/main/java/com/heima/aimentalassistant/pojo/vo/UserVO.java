package com.heima.aimentalassistant.pojo.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class UserVO {
    private String token;
    private String roleType;
    private UserInfo userInfo;

    @Builder
    @Data
    public static class UserInfo{
        private Long id;
        private String username;
        private String email;
        private String nickname;
        private String avatar;
        private String phone;
        private Integer gender;
        private String genderDisplayName;
        private LocalDate birthday;
        private Integer userType;
        private String userTypeDisplayName;
        private Integer status;
        private String statusDisplayName;
        private String displayName;
        private LocalDateTime createAt;
        private LocalDateTime updateAt;
    }

}

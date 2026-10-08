package com.heima.aimentalassistant.convert;

import com.heima.aimentalassistant.common.enums.UserStatus;
import com.heima.aimentalassistant.pojo.dto.UserRegisterDTO;
import com.heima.aimentalassistant.pojo.entity.User;
import com.heima.aimentalassistant.pojo.vo.UserVO;

import java.time.LocalDateTime;

public class UserConvert {

    /**
     * User实体转换为详情响应DTO
     * @param user User实体
     * @return 用户详情响应DTO
     */
    public static UserVO.UserInfo buildUserInfoResponse(User user) {
        return UserVO.UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .phone(user.getPhone())
                .gender(user.getGender())
                .genderDisplayName(getGenderDisplayName(user.getGender()))
                .birthday(user.getBirthday())
                .userType(user.getUserType())
                .userTypeDisplayName(user.getUserTypeDisplayName())
                .status(user.getStatus())
                .statusDisplayName(user.getStatusDisplayName())
                .displayName(user.getDisplayName())
                .createAt(user.getCreateAt())
                .updateAt(user.getUpdateAt())
                .build();
    }

    /**
     * 构建数据响应DTO
     * @param token JWT令牌
     * @param userInfo 用户信息
     * @return 数据响应DTO
     */
    public static UserVO buildDataResponse(String token, UserVO.UserInfo userInfo) {
        return UserVO.builder()
                .userInfo(userInfo)
                .token(token)
                .roleType(userInfo.getUserType().toString())
                .build();
    }

    /**
     * 获取性别显示名称
     * @param gender 性别代码
     * @return 性别显示名称
     */
    private static String getGenderDisplayName(Integer gender) {
        if (gender == null) {
            return "未知";
        }
        switch (gender) {
            case 1:
                return "男";
            case 2:
                return "女";
            default:
                return "未知";
        }
    }

    public static User buildUserEntity(UserRegisterDTO userRegisterDTO , String password) {
        return User.builder()
                .username(userRegisterDTO.getUsername())
                .email(userRegisterDTO.getEmail())
                .nickname(userRegisterDTO.getNickname())
                .phone(userRegisterDTO.getPhone())
                .gender(userRegisterDTO.getGender())
                .userType(userRegisterDTO.getUserType())
                .password(password)
                .birthday(userRegisterDTO.getBirthday())
                .status(UserStatus.NORMAL.getCode())
                .createAt(LocalDateTime.now())
                .updateAt(LocalDateTime.now())
                .build();
    }

}


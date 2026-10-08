package com.heima.aimentalassistant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.heima.aimentalassistant.common.enums.UserType;
import com.heima.aimentalassistant.common.utils.JWTUtils;
import com.heima.aimentalassistant.convert.UserConvert;
import com.heima.aimentalassistant.common.exception.BusinessException;
import com.heima.aimentalassistant.mapper.UserMapper;
import com.heima.aimentalassistant.pojo.dto.UserLoginDTO;
import com.heima.aimentalassistant.pojo.dto.UserRegisterDTO;
import com.heima.aimentalassistant.pojo.entity.User;
import com.heima.aimentalassistant.pojo.vo.UserVO;
import com.heima.aimentalassistant.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    public UserVO login(UserLoginDTO userLoginDTO) {
        //构造查询条件
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, userLoginDTO.getUsername())
                .or().eq(User::getEmail, userLoginDTO.getUsername());


        User user = userMapper.selectOne(queryWrapper);

        //判断用户是否存在
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        //验证密码是否正确
        String inputPassword = userLoginDTO.getPassword().trim();


        if (!passwordEncoder.matches(inputPassword, user.getPassword())) {
            throw new BusinessException("密码错误");
        }
        //检查用户状态是否正常
        if (!user.isActive()) {
            throw new BusinessException("用户已被禁用");
        }
        //生成JWT
        String jwt = JWTUtils.generateJWT(user.getUsername(), user.getId(), user.getUserType());

        UserVO.UserInfo userInfo = UserConvert.buildUserInfoResponse(user);
        return UserConvert.buildDataResponse(jwt, userInfo);
    }


    public UserVO register(UserRegisterDTO userRegisterDTO) {

        //验证密码是否一致
        if (!userRegisterDTO.getPassword().equals(userRegisterDTO.getConfirmPassword())) {
            throw new BusinessException("两次输入密码不一致");
        }

        //验证用户名是否存在
        User existingUser = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, userRegisterDTO.getUsername()));
        if (existingUser != null) {
            throw new BusinessException("用户名已存在");
        }

        //验证邮箱是否存在
        existingUser = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, userRegisterDTO.getEmail()));
        if (existingUser != null) {
            throw new BusinessException("邮箱已存在");
        }

        //用户类型
        if (!UserType.isValidCode(userRegisterDTO.getUserType())) {
            throw new BusinessException("用户类型错误");
        }

        //创建用户
        String password = passwordEncoder.encode(userRegisterDTO.getPassword().trim());
        User user = UserConvert.buildUserEntity(userRegisterDTO, password);
        userMapper.insert(user);

        //注册成功自动登录
        String jwt = JWTUtils.generateJWT(user.getUsername(), user.getId(), user.getUserType());
        UserVO.UserInfo userInfo = UserConvert.buildUserInfoResponse(user);
        return UserConvert.buildDataResponse(jwt, userInfo);

    }

    public  UserVO.UserInfo getUserById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return UserConvert.buildUserInfoResponse(user);
    }
}
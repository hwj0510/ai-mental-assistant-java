package com.heima.aimentalassistant.controller.user;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.heima.aimentalassistant.common.results.Result;
import com.heima.aimentalassistant.common.utils.JWTUtils;
import com.heima.aimentalassistant.pojo.dto.UserLoginDTO;
import com.heima.aimentalassistant.pojo.dto.UserRegisterDTO;
import com.heima.aimentalassistant.pojo.vo.UserVO;
import com.heima.aimentalassistant.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("api/user")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Result<UserVO> login(@Valid @RequestBody UserLoginDTO userLoginDTO) {
        log.info("========== 请求进入 POST /api/user/login ==========");
        log.info("[用户登录] 用户名: {}", userLoginDTO.getUsername());

        UserVO userVO = userService.login(userLoginDTO);
        log.info("[用户登录] 登录成功, userId: {}, username: {}", userVO.getUserInfo().getId(), userVO.getUserInfo().getUsername());
        log.info("========== 请求结束 POST /api/user/login ==========");

        return Result.success(userVO);
    }

    //注册接口
    @PostMapping("/add")
    public Result<UserVO> add(@Valid @RequestBody UserRegisterDTO userRegisterDTO) {
        log.info("========== 请求进入 POST /api/user/add ==========");
        log.info("[用户注册] 用户名: {}, 邮箱: {}", userRegisterDTO.getUsername(), userRegisterDTO.getEmail());

        UserVO result = userService.register(userRegisterDTO);
        log.info("[用户注册] 注册成功, userId: {}", result.getUserInfo().getId());
        log.info("========== 请求结束 POST /api/user/add ==========");

        return Result.success(result);
    }

    //获取用户信息接口
    @GetMapping("/current")
    public Result<UserVO.UserInfo> getCurrentUser() {
        log.info("========== 请求进入 GET /api/user/current ==========");

        String token = JWTUtils.getCurrentToken();
        DecodedJWT decodedJWT = JWTUtils.validateToken(token);
        Long userId = decodedJWT.getClaim("userId").asLong();
        log.info("[获取当前用户] userId: {}", userId);

        UserVO.UserInfo userInfo = userService.getUserById(userId);
        log.info("[获取当前用户] 查询成功, nickname: {}", userInfo.getNickname());
        log.info("========== 请求结束 GET /api/user/current ==========");

        return Result.success(userInfo);
    }
}
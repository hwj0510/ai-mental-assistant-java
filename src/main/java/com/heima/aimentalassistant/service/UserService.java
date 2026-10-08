package com.heima.aimentalassistant.service;

import com.heima.aimentalassistant.pojo.dto.UserLoginDTO;
import com.heima.aimentalassistant.pojo.dto.UserRegisterDTO;
import com.heima.aimentalassistant.pojo.vo.UserVO;
import jakarta.validation.Valid;
import reactor.core.publisher.Flux;

public interface UserService {
    UserVO login(UserLoginDTO userLoginDTO);

    UserVO register(@Valid UserRegisterDTO userRegisterDTO);

    UserVO.UserInfo getUserById(Long userId);


}

package com.heima.aimentalassistant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.heima.aimentalassistant.pojo.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}

package com.server.mapper;

import com.server.model.UserDTO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {

    UserDTO selectOneUser(UserDTO dto);

    String selectEncPwd(UserDTO dto);
}

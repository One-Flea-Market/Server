package com.server.mapper;

import com.server.model.UserDTO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {

    UserDTO selectOneUser(UserDTO dto);

    String selectEncPwd(UserDTO dto);

    /* 회원가입 */
    int joinUser(UserDTO dto);

    /* 이메일 중복 체크 */
    String emailCheck(String email);

    /* 닉네임 중복 체크 */
    String nameCheck(String name);



}

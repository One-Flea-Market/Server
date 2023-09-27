package com.server.service;

import com.server.mapper.UserMapper;
import com.server.model.UserDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    /* 로그인 시 유저가 DB에 있는지 검증 */
    public UserDTO getOneUser (UserDTO dto) {
        log.info("로그인 시도 후 유저 검증 시작");
        String pass = "admin";
        String encodedPass = passwordEncoder.encode(pass);

        if(passwordEncoder.matches(pass, encodedPass)){
            log.info("true");
            UserDTO userDTO = userMapper.selectOneUser(dto);
            log.info("로그인 시도 후 유저 검증 종료");
            return userDTO;
        } else {
            log.info("false");
            log.info("로그인 시도 후 유저 검증 종료");
            return null;
        }
    }
}

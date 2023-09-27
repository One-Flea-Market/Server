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
        log.info("로그인 시도 후 유저 검증 시작 {} ", dto);

        String encodedPwd = userMapper.selectEncPwd(dto);
        String rawPwd = dto.getStrPassword();

        if(passwordEncoder.matches(rawPwd, encodedPwd)) {
            log.info("로그인 시도 후 유저 검증 시작 {} ", passwordEncoder.matches(rawPwd, encodedPwd));
            dto.setStrPassword(encodedPwd);
            UserDTO rspDto = userMapper.selectOneUser(dto);
            log.info("로그인 시도 후 유저 검증 시작 {} ", rspDto);
            return rspDto;
        }
        return null;
    }
}

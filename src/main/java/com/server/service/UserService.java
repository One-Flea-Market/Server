package com.server.service;

import com.server.mapper.UserMapper;
import com.server.model.UserDTO;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private static final String FROM_ADDRESS = "hscapstone1@gmail.com"; // 이부분도 너희 캡스톤 쥐메일 ㄱㄱ
    @Autowired
    private JavaMailSender mailSender;

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

    public int joinUser(UserDTO dto) {
        log.info("회원가입 시도");

        dto.setStrRole("USER");
        dto.setStrPassword(passwordEncoder.encode(dto.getStrPassword()));

        int flag = 1;
        int result = userMapper.joinUser(dto);

        if(result >= 1) {
            flag = 0;
        } else {
            flag = 1;
        }
        return flag;
    }

    private int makeRandomNumber() {
        Random random = new Random();
        return random.nextInt(999999);
    }

    public String mailSender(String email) {
        int authNumber = makeRandomNumber();
        log.info("인증번호 : {}", authNumber);

            SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
            simpleMailMessage.setTo(email);
            simpleMailMessage.setFrom(UserService.FROM_ADDRESS);
            simpleMailMessage.setSubject("[ 원플리웹 ] 학교 메일 인증번호 입니다.");
            simpleMailMessage.setText(" 메일 인증번호 입니다. 인증번호 : " + authNumber);

            mailSender.send(simpleMailMessage);

            return  Integer.toString(authNumber);
    }
}

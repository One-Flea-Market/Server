package com.server.service;

import com.server.mapper.ProductMapper;
import com.server.mapper.UserMapper;
import com.server.model.ProductDTO;
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

import java.util.List;
import java.util.Random;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final ProductMapper productMapper;
    private final PasswordEncoder passwordEncoder;
    private static final String FROM_ADDRESS = "hscapstone1@gmail.com";
    @Autowired
    private JavaMailSender mailSender;

    /* 로그인 시 유저가 DB에 있는지 검증 */
    public UserDTO LoginUser(UserDTO dto) {
        log.info("로그인 시도");
        log.info("유저 검증 시작 (Request Email & PassWord) : {} ", dto);
        String encodedPwd = userMapper.selectEncPwd(dto);   // Encoded PassWord In DB
        String rawPwd = dto.getPassWord();                  // Request PassWord

        if(passwordEncoder.matches(rawPwd, encodedPwd)) {
            log.info("RawPassWord Matches EncodedPassWord : {} ", passwordEncoder.matches(rawPwd, encodedPwd));
            dto.setPassWord(encodedPwd);
            UserDTO rspDto = userMapper.selectOneUser(dto);
            log.info("Login User : {} ", rspDto);
            return rspDto;
        }
        log.info("Login Failed");
        return null;
    }

    public Boolean checkLogin(int userId) {
        Boolean result = userMapper.checkLogin(userId);
        log.info("result : {}", result);
        return result != null ? result : false;
    }

    /* 회원가입 */
    public int joinUser(UserDTO dto) {
        log.info("회원가입 시도");

        dto.setRole("USER");
        dto.setPassWord(passwordEncoder.encode(dto.getPassWord()));

        int flag = 1;
        int result = userMapper.joinUser(dto);

        if(result >= 1) {
            flag = 0;
        } else {
            flag = 1;
        }
        return flag;
    }

    /* 이메일 중복 체크 */
    public String emailCheck(String email) {
        String result = userMapper.emailCheck(email);
        log.info("result 로그 (userService) {}", result);
        return result;
    }

    /* 닉네임 중복 체크 */
    public String nameCheck(String username) {
        String result = userMapper.nameCheck(username);
        log.info("result 로그 (userService) {}", result);
        return result;
    }

    /* 이메일 인증 위한 랜덤 인증번호 생성 */
    private int makeRandomNumber() {
        Random random = new Random();
        return random.nextInt(999999);
    }

    /* 이메일 인증 */
    public String mailSender(String email) {
        int authNumber = makeRandomNumber();

        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setTo(email);
        simpleMailMessage.setFrom(UserService.FROM_ADDRESS);
        simpleMailMessage.setSubject("[ 원플리웹 ] 학교 메일 인증번호 입니다.");
        simpleMailMessage.setText(" 메일 인증번호 입니다. 인증번호 : " + authNumber);

        mailSender.send(simpleMailMessage);

        return Integer.toString(authNumber);
    }

    /* 마이페이지 정보 확인 */
    public UserDTO mypage(UserDTO dto) {

        UserDTO rspDto = userMapper.selectOneUser(dto);

        return rspDto;
    }

    public List<ProductDTO> getMyProduct(int id) {
        return userMapper.getMyProduct(id);
    }

    public Boolean getMyLikedProducts(int userId, int productSeq) {
        log.info("userId : {}", userId);
        log.info("productSeq : {}", productSeq);

        Boolean result = productMapper.getLikedByUser(userId, productSeq);

        return result != null ? result : false;
    }

    public int deleteCart(int userId, int productSeq) {
        log.info("userId : {}", userId);
        log.info("productSeq : {}", productSeq);

        int flag = 1;
        int result = productMapper.deleteCart(userId, productSeq);

        if(result >= 1) {
            flag = 0;
        } else {
            flag = 1;
        }
        return flag;
    }
}

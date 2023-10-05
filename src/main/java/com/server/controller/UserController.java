package com.server.controller;

import com.server.model.MessageRes;
import com.server.model.UserDTO;
import com.server.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.Charset;
import java.util.Objects;


@Slf4j
@RestController
@RequiredArgsConstructor
public class UserController {

    @Autowired
    private final UserService userService;

    /* 로그인 */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserDTO dto, HttpSession session) {

        UserDTO rspDto = userService.getOneUser(dto);
        if (rspDto != null) {
            /* session 생성 처리 필요 */
            session.setAttribute("dto", rspDto);
            session.setMaxInactiveInterval(1800);
            return new ResponseEntity<>(rspDto, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /* 회원가입 */
    @PostMapping("/sign-in")
    public ResponseEntity<?> join(@RequestBody UserDTO dto) {
        int flag = userService.joinUser(dto);

        return new ResponseEntity<>(flag, HttpStatus.OK);
    }

    /* 로그아웃 */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        /* session 만료처리 필요 */
        log.info("세션 {}",session.getAttribute("dto"));
        session.invalidate();
        return new ResponseEntity<>(null, HttpStatus.OK);
    }

    /* 세션 체크 */
    @GetMapping("/check")
    public ResponseEntity<?> sessionCheck(HttpServletRequest request) {
        boolean sessionFlagYN = false;
        request.getSession(false);
        log.info("세션 조회 {}",request.getSession(false));
        if(request.getSession(false) == null) {
            sessionFlagYN = false;
        } else {
            sessionFlagYN = true;
        }
        return new ResponseEntity<>(sessionFlagYN, HttpStatus.OK);
    }

    /* 이메일 인증 및 중복 체크 */
    @PostMapping("/sign-in/email")
    public ResponseEntity<MessageRes> emailCheck(@RequestParam String email) {
        MessageRes messageRes = new MessageRes();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", Charset.forName("UTF-8")));

        log.info("이메일 확인 {}", email);
        String result = userService.emailCheck(email);
        log.info("이메일 로그 확인 {}", result);

        if (Objects.equals(result, email)) {
            messageRes.setMessage("이미 존재하는 이메일입니다.");
            messageRes.setResult(false);

            return new ResponseEntity<>(messageRes, headers, HttpStatus.OK);
        } else {
            String systemAuthNumber = userService.mailSender(email);
            log.info("인증 번호 : {}", systemAuthNumber);

            messageRes.setResult(true);
            //messageRes.setAuth("systemAuthNumber");
            messageRes.setAuth(systemAuthNumber);

            return new ResponseEntity<>(messageRes, headers, HttpStatus.CREATED);
        }
    }

    /* 닉네임 중복 체크 */
    @PostMapping("/sign-in/username")
    public ResponseEntity<MessageRes> nameCheck(@RequestParam String name) {
        MessageRes messageRes = new MessageRes();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", Charset.forName("UTF-8")));

        log.info("닉네임 확인 {}", name);
        String result = userService.nameCheck(name);
        log.info("닉네임 로그 확인 {}", result);

        if (Objects.equals(result, name)) {
            messageRes.setMessage("이미 존재하는 닉네임입니다.");
            messageRes.setResult(false);

            return new ResponseEntity<>(messageRes, headers, HttpStatus.OK);
        } else {
            messageRes.setResult(true);
            messageRes.setAuth("사용 가능한 닉네임 입니다.");

            return new ResponseEntity<>(messageRes, headers, HttpStatus.CREATED);
        }
    }

    @GetMapping("/admin")
    public ResponseEntity<?> mypageView(@RequestParam String email, HttpSession session) {

        log.info("읽어온 email  {}", email);
        if (email == null) {
            log.info("mypage 호출 실패!!");
            return new ResponseEntity<>("{\"login\":false}", HttpStatus.NOT_FOUND);

        }

        UserDTO reqDto = (UserDTO) session.getAttribute("dto");

        log.info("읽어온 reqDto  {}", reqDto);

        if (reqDto != null && Objects.equals(reqDto.getStrEmail(), email)) {
            log.info("mypage 호출 성공!!");
            UserDTO userDTO = userService.mypage(reqDto);
            return new ResponseEntity<>(userDTO, HttpStatus.OK);

        } else {
            log.info("mypage 호출 실패!!");
            return new ResponseEntity<>("{\"login\":false}", HttpStatus.NOT_FOUND);
            }
    }
}

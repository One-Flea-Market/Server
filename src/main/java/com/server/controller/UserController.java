package com.server.controller;

import com.server.model.UserDTO;
import com.server.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequiredArgsConstructor
public class UserController {

    @Autowired
    private final UserService userService;

    @PostMapping("/api/login")
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

    @PostMapping("/api/join")
    public ResponseEntity<?> join(@RequestBody UserDTO dto) {
        int flag = userService.joinUser(dto);

        return new ResponseEntity<>(flag, HttpStatus.OK);
    }

    @PostMapping("/api/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        /* session 만료처리 필요 */
        log.info("세션 {}",session.getAttribute("dto"));
        session.invalidate();
        return new ResponseEntity<>(null, HttpStatus.OK);
    }

    @PostMapping("/api/sessionCheck")
    public ResponseEntity<?> sessionCheck(HttpServletRequest request) {
        String sessionFlagYN = "N";
        request.getSession(false);
        log.info("세션 조회 {}",request.getSession(false));
        if(request.getSession(false) == null) {
            sessionFlagYN = "N";
        } else {
            sessionFlagYN = "Y";
        }
        return new ResponseEntity<>(sessionFlagYN, HttpStatus.OK);
    }

    @PostMapping("/api/emailCheck")
    public ResponseEntity<?> emailCheck(@RequestParam String email) {
        log.info("이메일 확인 {}", email);
        String systemAuthNumber = userService.mailSender(email);
        log.info("인증 번호 : {}", systemAuthNumber);
        return new ResponseEntity<>(systemAuthNumber, HttpStatus.OK);
    }
}

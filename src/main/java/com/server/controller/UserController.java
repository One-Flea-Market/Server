package com.server.controller;

import com.server.model.UserDTO;
import com.server.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/api/login")
    public ResponseEntity<?> login(@RequestBody UserDTO dto) {

        UserDTO rspDto = userService.getOneUser(dto);
        if (rspDto != null) {
            /* session 생성 처리 필요 */
            return new ResponseEntity<>(rspDto, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping("/api/join")
    public ResponseEntity<?> join() {
        return new ResponseEntity<>(null, HttpStatus.OK);
    }

    @PostMapping("/api/logout")
    public ResponseEntity<?> logout() {
        /* session 만료처리 필요 */
        return new ResponseEntity<>(null, HttpStatus.OK);
    }

}

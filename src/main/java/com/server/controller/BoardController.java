package com.server.controller;

import com.server.model.BoardDTO;
import com.server.model.UserDTO;
import com.server.service.BoardService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    private final UserController userController;

    @PostMapping("/board/write")
    public ResponseEntity<?> boardWrite(@RequestBody BoardDTO dto, HttpServletRequest request) {

        HttpSession session = request.getSession();

        log.info("세션 {} ", userController.sessionCheck(request));
        /*
        if(session != null) {
            int flag = boardService.write(dto);
            return new ResponseEntity<>(flag, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(null, HttpStatus.OK);
        }*/
        return new ResponseEntity<>(null, HttpStatus.OK);
    }
}

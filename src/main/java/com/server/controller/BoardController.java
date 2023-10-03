package com.server.controller;

import com.server.model.BoardDTO;
import com.server.model.UserDTO;
import com.server.service.BoardService;
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

    @PostMapping("/board/write")
    public ResponseEntity<?> boardWrite(@RequestBody BoardDTO dto, HttpSession session) {
        String sessionFlagYN = "N";

        if(session.getAttribute("dto") == null) {
            sessionFlagYN = "N";
        } else { // 세션이 있으면 여길 탈듯
            /* 게시글 작성 서비스 호출 */
             UserDTO reqDto = (UserDTO) session.getAttribute("dto");
             log.info("session user : {}", session.getAttribute("dto"));
             dto.setUserId(reqDto.getId());
             int flag = boardService.write(dto);

             /* 게시글 작성완료는 0 아니면 1 */
             return new ResponseEntity<>(flag, HttpStatus.OK);
        }
        /* 세션이 없으면 N */
        return new ResponseEntity<>(sessionFlagYN, HttpStatus.OK);
    }
}

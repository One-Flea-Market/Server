package com.server.controller;

import com.server.model.BoardDTO;
import com.server.model.MessageResBoard;
import com.server.model.NoticeDTO;
import com.server.model.UserDTO;
import com.server.service.BoardService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.Charset;

@Slf4j
@RestController
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    /*  자유 게시판 조회
        객체안 board 원소의 배열의 길이는 몇개씩 보내도 상관없음(가능하면 10개 정도로 잘라서 보내기)
        next 는 앞서보여준 10개를 제외하고 보여줄 공지가 더있으면 true 없으면 false */
    @GetMapping("/board")
    public ResponseEntity<MessageResBoard> boardView() {
        MessageResBoard messageResBoard = new MessageResBoard();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", Charset.forName("UTF-8")));

        int page = 1;
        int pageSize = 10;
        int flag = 0;
        int count = boardService.getBoardCount();
        log.info("테이블 내 컬럼 개수 ( count ) : {}", count);

        while((page - 1) * pageSize <= count) {
            log.info("while 문 안의 page : {}", page);
            messageResBoard.setBoardList(boardService.getBoard(page, pageSize));
            log.info("BoardList : {}", messageResBoard.getBoardList());
            if (count >= (page - 1) * pageSize) {
                messageResBoard.setNext(true);
                flag = page * pageSize;
                page++;
                log.info(" flag : {}", flag);
                if (count <= flag) {
                    messageResBoard.setNext(false);
                }
            } else {
                messageResBoard.setNext(false);
            }
            log.info("컬럼이 더 있는가? : {}", messageResBoard.isNext());
        }
        log.info("while 문 밖의 page : {}", page);

        return new ResponseEntity<>(messageResBoard, headers, HttpStatus.OK);
    }

    /* 게시글 작성 */
    @PostMapping("/board/new")
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

    /*  게시글 고유 id로 게시글 조회
        url 에 있는 id로 게시글정보를 가져오며 가져온 게시글이 자신이 작성 한 경우  oneself 는 true 아니면 false */
    @GetMapping("/board/{id}")
    public ResponseEntity<BoardDTO> getBoardById(@PathVariable int id) {
        log.info("{}", id);
        BoardDTO dto = boardService.getBoardById(id);

        log.info("{}", dto);
        if (dto == null) {
            // 게시글이 없을 경우 404 Not Found 응답을 보냄
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // 게시글 정보를 BoardDTO 에 매핑하여 응답
        BoardDTO boardDTO = new BoardDTO();
        boardDTO.setStrTitle(dto.getStrTitle());
        boardDTO.setStrDate(dto.getStrDate());
        boardDTO.setStrContent(dto.getStrContent());
        boardDTO.setBoardSeq(dto.getBoardSeq());
        boardDTO.setUserId(dto.getUserId());



        return new ResponseEntity<>(boardDTO, HttpStatus.OK);
    }
}

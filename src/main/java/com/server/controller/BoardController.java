package com.server.controller;

import com.server.model.BoardCommentDTO;
import com.server.model.BoardDTO;
import com.server.model.ProductCommentDTO;
import com.server.response.MessageResBoard;
import com.server.model.UserDTO;
import com.server.service.BoardService;
import com.server.service.CommentService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/board")
public class BoardController {

    private final BoardService boardService;
    private final SqlSession sqlSession;
    private final CommentService commentService;

    /*  자유 게시판 조회
        객체안 board 원소의 배열의 길이는 몇개씩 보내도 상관없음(가능하면 10개 정도로 잘라서 보내기)
        next 는 앞서보여준 10개를 제외하고 보여줄 공지가 더있으면 true 없으면 false */
    @GetMapping("")
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
    @PostMapping("/new")
    public ResponseEntity<?> boardWrite(@RequestBody BoardDTO dto, HttpSession session) {
        String sessionFlagYN = "N";
        boolean result;

        if(session.getAttribute("dto") == null) {
            sessionFlagYN = "N";
        } else { // 세션이 있으면 여길 탈듯
            /* 게시글 작성 서비스 호출 */
             UserDTO reqDto = (UserDTO) session.getAttribute("dto");
             log.info("session user : {}", session.getAttribute("dto"));
             dto.setUserId(reqDto.getId());
             int flag = boardService.writeBoard(dto);

             if(flag == 0) {
                 result = true;
             } else {
                 result = false;
             }
             /* 게시글 작성완료는 0이면 true 아니면 false */
             return new ResponseEntity<>(result, HttpStatus.OK);
        }
        /* 세션이 없으면 N */
        return new ResponseEntity<>(sessionFlagYN, HttpStatus.OK);
    }

    /*  게시글 고유 id로 게시글 조회
        url 에 있는 id로 게시글정보를 가져오며 가져온 게시글이 자신이 작성 한 경우  oneself 는 true 아니면 false */
    @GetMapping("/{id}")
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

    /*  검색어로 게시글 조회
        */
    @GetMapping("/search/{search}")
    public ResponseEntity<MessageResBoard> getBoardBySearch(@PathVariable String search,
                                                             @RequestParam(defaultValue = "1") int page,
                                                             @RequestParam(defaultValue = "10") int pageSize) {
        MessageResBoard messageResBoard = new MessageResBoard();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", Charset.forName("UTF-8")));

        Map<String, Object> map = new HashMap<>();
        int offset = (page - 1) * pageSize;

        map.put("search", "%" + search.toUpperCase() + "%");
        map.put("offset", offset);
        map.put("pageSize", pageSize);

        List<BoardDTO> result = sqlSession.selectList("com.server.mapper.BoardMapper.getBoardBySearch", map);

        if (result.isEmpty()) {
            // 결과가 없을 때 처리
            messageResBoard.setNext(false);
        } else {
            if(result.size() < pageSize) {
                messageResBoard.setBoardList(result);
                messageResBoard.setNext(false);
            } else{
                // 결과가 있을 때 처리
                messageResBoard.setBoardList(result);
                messageResBoard.setNext(true);
            }
        }

        log.info("컬럼이 더 있는가? : {}", messageResBoard.isNext());
        log.info("현재 페이지: {}, 오프셋: {}, 총 검색된 결과 수: {}", page, offset, result.size());
        return new ResponseEntity<>(messageResBoard, headers, HttpStatus.OK);
    }

    /* 게시글 수정 */
    @PatchMapping("/{id}/modify")
    public ResponseEntity<MessageResBoard> modifyBoard(@PathVariable int id, @RequestBody Map<String, Object> map,
                                                       BoardDTO dto, HttpSession session) {

        MessageResBoard messageResBoard = new MessageResBoard();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", Charset.forName("UTF-8")));

        String sessionFlagYN = "N";

        if(session.getAttribute("dto") == null) {
            sessionFlagYN = "N";
        } else { // 세션이 있으면 여길 탈듯
            /* 게시글 수정 서비스 호출 */
            UserDTO reqDto = (UserDTO) session.getAttribute("dto");
            log.info("session user : {}", session.getAttribute("dto"));
            dto.setUserId(reqDto.getId());

            map.put("id", id);
            int flag = boardService.modifyBoard(map);

            if(flag == 0) {
                messageResBoard.setResult(true);
            } else {
                messageResBoard.setResult(false);
                messageResBoard.setMessage("게시글 수정에 실패했습니다.");
            }
            /* 게시글 작성완료는 0이면 true 아니면 false */
            return new ResponseEntity<>(messageResBoard, headers, HttpStatus.OK);
        }
        /* 세션이 없으면 N */
        log.info("session : {}", sessionFlagYN);
        return new ResponseEntity<>(messageResBoard, headers, HttpStatus.OK);
    }

    /* 게시글 삭제 */
    @DeleteMapping("/{id}/delete")
    public ResponseEntity<Object> deleteBoard(@PathVariable int id, HttpSession session) {

        log.info("게시글 id : {}", id);
            Map<String, Object> response = new HashMap<>();

            if(session.getAttribute("dto") == null) {
                log.info("실패");
                response.put("result", false);
                response.put("message", "로그인 상태가 아닙니다.");
            } else { // 세션이 있으면 여길 탈듯
                /* 상품 조회 서비스 호출 */
                UserDTO reqDto = (UserDTO) session.getAttribute("dto");
                log.info("session user : {}", session.getAttribute("dto"));
                int userId = reqDto.getId();    // 세션에서 가져온 유저 id
                int idUser = boardService.getUserIdByBoardSeq(id);  // 게시글 id를 이용해 게시글 테이블에서 가져온 유저 id

            log.info("로그인 중인 유저 Id : {}", userId);
            log.info("게시글 등록한 유저 Id : {}", idUser);

            if(userId == idUser) {
                int flag = boardService.deleteBoard(id);

                if(flag == 0) {
                    log.info("성공");
                    response.put("result", true);
                } else {
                    log.info("실패");
                    response.put("result", false);
                    response.put("message", "모종의 이유로 삭제에 실패했습니다.");
                }
            } else {
                log.info("실패");
                response.put("result", false);
                response.put("message", "본인이 게시한 게시글이 아닙니다.");
            }
        }
        return ResponseEntity.ok(response);
    }

    /* 댓글 조회 */
    @GetMapping("/{id}/comment")
    public ResponseEntity<List<BoardCommentDTO>> commentOnProduct(@PathVariable int id, HttpSession session) {
        log.info("상품 id : {}", id);
        Map<String, Object> response = new HashMap<>();

        if (session.getAttribute("dto") == null) {
            log.info("실패");
            response.put("result", false);
            response.put("message", "로그인 상태가 아닙니다.");
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); // 401 Unauthorized 응답을 보냄
        }

        UserDTO reqDto = (UserDTO) session.getAttribute("dto");
        List<BoardCommentDTO> reqDtoBoard = commentService.getBoardComment(id);
        log.info("session user : {}", session.getAttribute("dto"));
        log.info("Get Board Comment : {}", reqDtoBoard);

        int userId = reqDto.getId();    // 세션에서 가져온 유저 id

        if (reqDtoBoard.isEmpty()) {
            // 댓글이 없을 경우 404 Not Found 응답을 보냄
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        for (BoardCommentDTO boardComment : reqDtoBoard) {    // 검사
            int userIdByBoardComment = boardComment.getUserId(); // 댓글을 등록한 유저의 id를 가져옴.

            log.info("로그인 중인 유저 Id : {}", userId);
            log.info("댓글 등록한 유저 Id : {}", userIdByBoardComment);

            if (userId == userIdByBoardComment) {
                boardComment.setOnself(true);
            } else {
                boardComment.setOnself(false);
            }
        }
        // 댓글 리스트를 반환
        return new ResponseEntity<>(reqDtoBoard, HttpStatus.OK);
    }

    /* 댓글 작성 */
    @PostMapping("/{id}/comment/new")
    public ResponseEntity<?> commentBoardWrite(@PathVariable int id, @RequestBody BoardCommentDTO dto, HttpSession session) {
        log.info("게시글 id : {}", id);
        Map<String, Object> response = new HashMap<>();

        if(session.getAttribute("dto") == null) {
            log.info("실패");
            response.put("result", false);
            response.put("message", "로그인 상태가 아닙니다.");
        } else { // 세션이 있으면 여길 탈듯
            /* 상품 조회 서비스 호출 */
            UserDTO reqDto = (UserDTO) session.getAttribute("dto");
            log.info("session user : {}", session.getAttribute("dto"));
            log.info("get board : {}", boardService.getBoardById(id));
            int userId = reqDto.getId();    // 세션에서 가져온 유저 id
            int boardSeq = id;  // 게시글 id를 이용해 게시글 테이블에서 가져온 유저 id

            dto.setUserId(userId);
            dto.setBoardSeq(boardSeq);

            int flag = commentService.writeBoardComment(dto);

            if (flag == 0) {
                log.info("성공");
                response.put("result", true);
            } else {
                log.info("실패");
                response.put("result", false);
                response.put("message", "모종의 이유로 댓글 작성에 실패했습니다.");
            }
        }
        return ResponseEntity.ok(response);
    }

    /* 댓글 수정 */
    @PatchMapping("/{id}/comment/{commentId}/modify")
    public ResponseEntity<?> modifyBoardComment(@PathVariable int id, @PathVariable int commentId, @RequestBody Map<String, Object> map,
                                                BoardCommentDTO dto, HttpSession session) {
        log.info("게시글 id : {}", id);
        Map<String, Object> response = new HashMap<>();

        if(session.getAttribute("dto") == null) {
            log.info("실패");
            response.put("result", false);
            response.put("message", "로그인 상태가 아닙니다.");
        } else { // 세션이 있으면 여길 탈듯
            /* 상품 조회 서비스 호출 */
            UserDTO reqDto = (UserDTO) session.getAttribute("dto");
            log.info("session user : {}", session.getAttribute("dto"));
            log.info("get board : {}", boardService.getBoardById(id));
            int userId = reqDto.getId();    // 세션에서 가져온 유저 id
            int boardSeq = id;  // 게시글 id를 이용해 게시글 테이블에서 가져온 유저 id

            dto.setUserId(userId);
            dto.setBoardSeq(boardSeq);

            map.put("id", id);
            map.put("commentId", commentId);
            int flag = commentService.modifyBoardComment(map);

            if (flag == 0) {
                log.info("성공");
                response.put("result", true);
            } else {
                log.info("실패");
                response.put("result", false);
                response.put("message", "모종의 이유로 댓글 수정에 실패했습니다.");
            }
        }
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}/comment/{commentId}/delete")
    public ResponseEntity<?> deleteBoardComment(@PathVariable int id, @PathVariable int commentId,
                                                BoardCommentDTO dto, HttpSession session) {
        log.info("게시글 id : {}", id);
        log.info("댓글 id : {}", commentId);
        Map<String, Object> response = new HashMap<>();

        if(session.getAttribute("dto") == null) {
            log.info("실패");
            response.put("result", false);
            response.put("message", "로그인 상태가 아닙니다.");
        } else { // 세션이 있으면 여길 탈듯
            /* 상품 조회 서비스 호출 */
            UserDTO reqDto = (UserDTO) session.getAttribute("dto");
            log.info("session user : {}", session.getAttribute("dto"));
            log.info("get board : {}", boardService.getBoardById(id));
            int userId = reqDto.getId();    // 세션에서 가져온 유저 id
            int boardSeq = id;  // 게시글 id를 이용해 게시글 테이블에서 가져온 유저 id

            dto.setUserId(userId);
            dto.setBoardSeq(boardSeq);

            int flag = commentService.deleteBoardComment(id, commentId);

            if (flag == 0) {
                log.info("성공");
                response.put("result", true);
            } else {
                log.info("실패");
                response.put("result", false);
                response.put("message", "모종의 이유로 댓글 삭제에 실패했습니다.");
            }
        }
        return ResponseEntity.ok(response);
    }

}

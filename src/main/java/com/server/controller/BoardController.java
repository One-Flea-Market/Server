package com.server.controller;

import com.server.model.*;
import com.server.service.BoardService;
import com.server.service.CommentService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = {"https://client-p34zpc52f-capstone-team-market.vercel.app/", "https://localhost:3001"})
@RequestMapping("/board")
public class BoardController {

    private final BoardService boardService;
    private final CommentService commentService;

    /*  자유 게시판 조회
        객체안 board 원소의 배열의 길이는 몇개씩 보내도 상관없음(가능하면 10개 정도로 잘라서 보내기)
        next 는 앞서보여준 10개를 제외하고 보여줄 공지가 더있으면 true 없으면 false */
    @GetMapping("")
    public ResponseEntity<?> boardView(@RequestParam(defaultValue = "1") int page,
                                       @RequestParam(defaultValue = "10") int pageSize) {

        int count = boardService.getBoardCount();
        log.info("테이블 내 컬럼 개수 ( count ) : {}", count);

        // 서비스에서 게시글 목록을 가져옴
        List<BoardDTO> boardList = boardService.getBoard(page);

        // 응답 데이터 구성을 위한 리스트
        List<Map<String, Object>> responseList = new ArrayList<>();

        // 현재 페이지에서 보내줄 게시글 개수
        int boardCount = 0;

        for(BoardDTO listDto : boardList) {
            Map<String, Object> boardMap = new LinkedHashMap<>();
            boardMap.put("id", listDto.getId());
            boardMap.put("title", listDto.getTitle());
            boardMap.put("date", listDto.getDate());

            responseList.add(boardMap);

            boardCount++;

            if(boardCount >= pageSize) {
                page++;
                log.info("{}", page);
                break;
            }
        }
        log.info("boardList : {]", boardList);

        Map<String, Object> response = new HashMap<>();

        response.put("list", responseList);
        response.put("next", boardCount >= pageSize);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /* 게시글 작성 */
    @PostMapping("/new")
    public ResponseEntity<?> boardWrite(@RequestBody BoardRequestDTO requestDTO, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        if (session.getAttribute("dto") == null) {
            log.info("실패");
            response.put("result", false);
            response.put("message", "로그인 상태가 아닙니다.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED); // 401 Unauthorized 응답을 보냄
        } else { // 세션이 있으면 여길 탈듯
            /* 상품 등록 서비스 호출 */
            UserDTO reqDto = (UserDTO) session.getAttribute("dto");
            log.info("session user : {}", session.getAttribute("dto"));

            BoardDTO dto = new BoardDTO();
            dto.setTitle(requestDTO.getTitle());
            dto.setBody(requestDTO.getBody());
            dto.setDate(requestDTO.getDate());
            dto.setUserId(reqDto.getId());

            int flag = boardService.writeBoard(dto);

             if(flag == 0) {
                 response.put("result", true);
             } else {
                 response.put("result", false);
                 response.put("message", "실패");
             }
        }
        return ResponseEntity.ok(response);
    }

    /*  게시글 고유 id로 게시글 조회
        url 에 있는 id로 게시글정보를 가져오며 가져온 게시글이 자신이 작성 한 경우  oneself 는 true 아니면 false */
    @GetMapping("/{id}")
    public ResponseEntity<?> getBoardById(@PathVariable int id, BoardDTO dto, HttpSession session) {
        log.info("{}", id);
        Map<String, Object> response = new HashMap<>();

        if (session.getAttribute("dto") == null) {
            log.info("실패");
            response.put("result", false);
            response.put("message", "로그인 상태가 아닙니다.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED); // 401 Unauthorized 응답을 보냄
        }

        /* 상품 조회 서비스 호출 */
        UserDTO reqDto = (UserDTO) session.getAttribute("dto");
        log.info("session user : {}", session.getAttribute("dto"));
        dto.setUserId(reqDto.getId());

        // 상품안의 USER_ID 검색
        int userId = boardService.getUserIdByBoardSeq(id);

        log.info("로그인 중인 유저 Id : {}", reqDto.getId());
        log.info("상품 등록한 유저 Id : {}", userId);

        List<BoardDTO> productList = boardService.getBoardById(id);   // 상품 리스트 호출
        List<Map<String, Object>> responseList = new ArrayList<>();

        for (BoardDTO listDto : productList) {
            Map<String, Object> boardMap = new LinkedHashMap<>();
            boardMap.put("id", listDto.getId());
            boardMap.put("title", listDto.getTitle());
            boardMap.put("date", listDto.getDate());
            boardMap.put("body", listDto.getBody());
            boardMap.put("user", boardService.getNameByUserId(listDto.getUserId()));

            if (userId == reqDto.getId()) {
                boardMap.put("onself", true);
            }

            if(userId != reqDto.getId()) {
                boardMap.put("onself", false);
            }

            responseList.add(boardMap);
        }
        return new ResponseEntity<>(responseList, HttpStatus.OK);
    }

    /*  검색어로 게시글 조회
        */
    @GetMapping("/search/{search}")
    public ResponseEntity<?> getBoardBySearch(@PathVariable String search,
                                              @RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "10") int pageSize) {
        int count = boardService.getBoardCountBySearch(search);
        log.info("테이블 내 컬럼 개수 ( count ) : {}", count);

        // 서비스에서 상품 목록을 가져옴
        List<BoardDTO> boardList = boardService.getBoardBySearch(search, page);

        // 응답 데이터 구성을 위한 리스트
        List<Map<String, Object>> responseList = new ArrayList<>();

        // 현재 페이지에서 보내줄 상품 개수
        int boardCount = 0;

        for (BoardDTO listDto : boardList) {
            Map<String, Object> boardMap = new LinkedHashMap<>();                 // 상품 정보들을 저장해 출력할 Map 선언
            boardMap.put("id", listDto.getId());                  // 상품 고유 id 설정
            boardMap.put("title", listDto.getTitle());        // 상품 이름 설정
            boardMap.put("date", listDto.getDate());      // 상품 카테고리 설정

            // 응답 데이터에 상품 정보 추가
            responseList.add(boardMap);

            // 현재 페이지에서 보내준 상품 개수 증가
            boardCount++;

            // 만약 현재 페이지에서 보내줄 상품 개수가 pageSize와 같거나 크다면 루프 종료
            if (boardCount >= pageSize) {
                page++;
                log.info("{}", page);
                break;
            }
        }
        log.info("productList : {}", boardList);

        Map<String, Object> response = new HashMap<>();
        response.put("list", responseList);
        response.put("next", boardCount >= pageSize);

        log.info("while 문 밖의 page : {}", page);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /* 게시글 수정 */
    @PatchMapping("/{id}/modify")
    public ResponseEntity<?> modifyBoard(@PathVariable int id, @RequestBody Map<String, Object> map,
                                                       BoardDTO dto, HttpSession session) {

        log.info("{}", id);
        Map<String, Object> response = new HashMap<>();

        if (session.getAttribute("dto") == null) {
            log.info("실패");
            response.put("result", false);
            response.put("message", "로그인 상태가 아닙니다.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED); // 401 Unauthorized 응답을 보냄
        }
        /* 상품 조회 서비스 호출 */
        UserDTO reqDto = (UserDTO) session.getAttribute("dto");
        log.info("session user : {}", session.getAttribute("dto"));
        dto.setUserId(reqDto.getId());

        // 상품 안의 userId 조회
        int userId = boardService.getUserIdByBoardSeq(id);

        log.info("로그인 중인 유저 Id : {}", reqDto.getId());
        log.info("게시글 등록한 유저 Id : {}", userId);

        map.put("id", id);

        if (userId == reqDto.getId()) {
            int flag = boardService.modifyBoard(map);

            if (flag == 0) {
                response.put("result", true);
            } else {
                response.put("result", false);
                response.put("message", "게시글 수정에 실패했습니다.");
            }
        } else {
            response.put("result", false);
            response.put("message", "본인이 등록한 게시글이 아닙니다.");
        }
        return new ResponseEntity<>(response, HttpStatus.OK);

    }

    /* 게시글 삭제 */
    @DeleteMapping("/{id}/delete")
    public ResponseEntity<?> deleteBoard(@PathVariable int id, HttpSession session) {

        log.info("게시글 id : {}", id);
            Map<String, Object> response = new HashMap<>();

            if(session.getAttribute("dto") == null) {
                log.info("실패");
                response.put("result", false);
                response.put("message", "로그인 상태가 아닙니다.");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED); // 401 Unauthorized 응답을 보냄
            } else { // 세션이 있으면 여길 탈듯
                /* 상품 조회 서비스 호출 */
                UserDTO reqDto = (UserDTO) session.getAttribute("dto");
                log.info("session user : {}", session.getAttribute("dto"));
                int userId = reqDto.getId();    // 세션에서 가져온 유저 id
                Integer idUser = boardService.getUserIdByBoardSeq(id);  // 게시글 id를 이용해 게시글 테이블에서 가져온 유저 id

            log.info("로그인 중인 유저 Id : {}", userId);
            log.info("게시글 등록한 유저 Id : {}", idUser);

            if(idUser == 0){
                log.info("해당 id의 게시글 미존재");
                response.put("result", false);
                response.put("message", "게시글이 없습니다.");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

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
    public ResponseEntity<?> commentOnProduct(@PathVariable int id, HttpSession session) {
        log.info("게시글 id : {}", id);
        Map<String, Object> response = new HashMap<>();

        if (session.getAttribute("dto") == null) {
            log.info("실패");
            response.put("result", false);
            response.put("message", "로그인 상태가 아닙니다.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED); // 401 Unauthorized 응답을 보냄
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
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED); // 401 Unauthorized 응답을 보냄
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
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED); // 401 Unauthorized 응답을 보냄
        } else { // 세션이 있으면 여길 탈듯
            /* 상품 조회 서비스 호출 */
            UserDTO reqDto = (UserDTO) session.getAttribute("dto");
            log.info("session user : {}", session.getAttribute("dto"));
            log.info("get board : {}", boardService.getBoardById(id));
            int userId = reqDto.getId();    // 세션에서 가져온 유저 id
            int idUser = commentService.getUserIdByBCommentSeq(commentId);  // 게시글 id를 이용해 댓글 테이블에서 가져온 유저 id
            int boardSeq = id;

            log.info("userId : {}", userId);
            log.info("idUser : {}", idUser);

            dto.setUserId(userId);
            dto.setBoardSeq(boardSeq);

            map.put("id", id);
            map.put("commentId", commentId);
            log.info("id : {}", map.put("id", id));
            log.info("commentId : {}", map.put("commentId", commentId));

            if(idUser == 0){
                log.info("해당 id의 댓글 미존재");
                response.put("result", false);
                response.put("message", "댓글이 없습니다.");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

            if(userId == idUser) {
                int flag = commentService.modifyBoardComment(map);
                if(flag == 0) {
                    log.info("성공");
                    response.put("result", true);
                } else {
                    log.info("실패");
                    response.put("result", false);
                    response.put("message", "모종의 이유로 수정에 실패했습니다.");
                    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                }
            } else {
                response.put("result", false);
                response.put("message", "작성자 이외에는 수정할 수 없습니다.");
            }
        }
        return ResponseEntity.ok(response);
    }

    /* 댓글 삭제 */
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
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED); // 401 Unauthorized 응답을 보냄
        } else { // 세션이 있으면 여길 탈듯
            /* 상품 조회 서비스 호출 */
            UserDTO reqDto = (UserDTO) session.getAttribute("dto");
            log.info("session user : {}", session.getAttribute("dto"));
            log.info("get board : {}", boardService.getBoardById(id));
            int userId = reqDto.getId();    // 세션에서 가져온 유저 id
            int idUser = commentService.getUserIdByBCommentSeq(commentId); // 게시글 id를 이용해 댓글 테이블에서 가져온 유저 id
            int boardSeq = id;
            log.info("userId : {}", userId);
            log.info("idUser : {}", idUser);
            dto.setUserId(userId);
            dto.setBoardSeq(boardSeq);

            if(idUser == 0){
                log.info("해당 id의 댓글 미존재");
                response.put("result", false);
                response.put("message", "댓글이 없습니다.");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

            if(userId == idUser) {
                int flag = commentService.deleteBoardComment(id, commentId);
                if (flag == 0) {
                    log.info("성공");
                    response.put("result", true);
                } else {
                    log.info("실패");
                    response.put("result", false);
                    response.put("message", "모종의 이유로 삭제에 실패했습니다.");
                    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                }
            } else {
                response.put("result", false);
                response.put("message", "작성자 이외에는 삭제할 수 없습니다.");
            }
        }
        return ResponseEntity.ok(response);
    }

}

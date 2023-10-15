package com.server.service;

import com.server.mapper.BoardMapper;
import com.server.model.BoardDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class BoardService {

    private final BoardMapper boardMapper;

    /* 게시글 작성 */
    public int writeBoard(BoardDTO dto) {
        log.info("dto : {}", dto);

        int flag = 1;
        int result = boardMapper.writeBoard(dto);

        if(result >= 1) {
            flag = 0;
        } else {
            flag = 1;
        }
        return flag;
    }

    /* 게시글 개수 count */
    public int getBoardCount() {
        return boardMapper.getBoardCount();
    }

    /* 게시글 조회 서비스 */
    public List<BoardDTO> getBoard(int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        log.info("offset : {}", offset);
        return boardMapper.getBoard(offset, pageSize);
    }

    public int getUserIdByBoardSeq(int id) {
        return boardMapper.getUserIdByBoardSeq(id);
    }
    /* 게시글 조회 by 게시글 고유 id */
    public BoardDTO getBoardById(int id) {
        return boardMapper.getBoardById(id);
    }

    /* 게시글 조회 by 검색어 */
    public List<BoardDTO> getBoardBySearch(String search, int offset, int pageSize) {

        log.info("요청 받은 검색어: {}", search);
        log.info("offset : {}", offset);
        log.info("{}",boardMapper.getBoardBySearch(search, offset, pageSize));
        return boardMapper.getBoardBySearch(search, offset, pageSize);
    }

    public int modifyBoard(Map<String, Object> map) {

        int flag = 1;
        int result = boardMapper.modifyBoard(map);

        if(result >= 1) {
            flag = 0;
        } else {
            flag = 1;
        }
        return flag;
    }

    public int deleteBoard(int id) {
        int flag = 1;
        int result = boardMapper.deleteBoard(id);

        if(result >= 1) {
            flag = 0;
        } else {
            flag = 1;
        }
        return flag;
    }
}

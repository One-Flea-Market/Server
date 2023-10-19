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
    public List<BoardDTO> getBoard(int page) {
        int offset = (page - 1) * 10;
        log.info("offset : {}", offset);
        return boardMapper.getBoard(offset);
    }

    public Integer getUserIdByBoardSeq(int id) {
        Integer result = boardMapper.getUserIdByBoardSeq(id);

        return result != null ? result : 0;
    }

    /* 게시글 조회 by 게시글 고유 id */
    public List<BoardDTO> getBoardById(int id) {
        return boardMapper.getBoardById(id);
    }

    public String getNameByUserId(int id) {
        return boardMapper.getNameByUserId(id);
    }

    /* 게시글 조회 by 검색어 */
    public List<BoardDTO> getBoardBySearch(String search, int page) {

        int offset = (page - 1) * 10;
        log.info("offset : {}", offset);
        search = '%'+search+'%';
        return boardMapper.getBoardBySearch(search, offset);
    }

    public int getBoardCountBySearch(String search) {
        search = '%'+search+'%';
        return boardMapper.getBoardCountBySearch(search);
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

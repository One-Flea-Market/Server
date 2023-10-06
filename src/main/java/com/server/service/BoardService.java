package com.server.service;

import com.server.mapper.BoardMapper;
import com.server.model.BoardDTO;
import com.server.model.NoticeDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class BoardService {

    private final BoardMapper boardMapper;

    public int write(BoardDTO dto) {
        log.info("dto : {}", dto);

        int flag = 1;
        int result = boardMapper.write(dto);

        if(result >= 1) {
            flag = 0;
        } else {
            flag = 1;
        }
        return flag;
    }

    public int getBoardCount() {
        return boardMapper.getBoardCount();
    }

    public List<BoardDTO> getBoard(int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        log.info("offset : {}", offset);
        return boardMapper.getBoard(offset, pageSize);
    }

    public BoardDTO getBoardById(int id) {
        return boardMapper.getBoardById(id);
    }
}

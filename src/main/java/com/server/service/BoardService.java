package com.server.service;

import com.server.mapper.BoardMapper;
import com.server.model.BoardDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class BoardService {

    private final BoardMapper boardMapper;

    public int write(BoardDTO dto) {

        int flag = 1;
        int result = boardMapper.write(dto);

        if(result >= 1) {
            flag = 0;
        } else {
            flag = 1;
        }
        return flag;
    }

}

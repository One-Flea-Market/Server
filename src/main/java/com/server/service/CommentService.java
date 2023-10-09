package com.server.service;

import com.server.mapper.CommentMapper;
import com.server.model.BoardDTO;
import com.server.model.CommentDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService {

    private final CommentMapper commentMapper;

    public List<CommentDTO> getComment(int boardSeq) {
        return commentMapper.getComment(boardSeq);
    }

    public int writeComment(CommentDTO dto) {
        log.info("dto : {}", dto);

        int flag = 1;
        int result = commentMapper.writeComment(dto);

        if(result >= 1) {
            flag = 0;
        } else {
            flag = 1;
        }
        return flag;
    }

    public int modifyComment(Map<String, Object> map) {

        int flag = 1;
        int result = commentMapper.modifyComment(map);

        if(result >= 1) {
            flag = 0;
        } else {
            flag = 1;
        }
        return flag;
    }

    public int deleteComment(int id, int commentId) {
        int flag = 1;
        int result = commentMapper.deleteComment(id, commentId);

        if(result >= 1) {
            flag = 0;
        } else {
            flag = 1;
        }
        return flag;
    }
}

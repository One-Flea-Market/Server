package com.server.service;

import com.server.mapper.CommentMapper;
import com.server.model.BoardCommentDTO;
import com.server.model.ProductCommentDTO;
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

    /*------------------------------------- 게시판 댓글 ------------------------------------------------ */
    public List<BoardCommentDTO> getBoardComment(int boardSeq) {
        return commentMapper.getBoardComment(boardSeq);
    }

    public int writeBoardComment(BoardCommentDTO dto) {
        log.info("dto : {}", dto);

        int flag = 1;
        int result = commentMapper.writeBoardComment(dto);

        if(result >= 1) {
            flag = 0;
        } else {
            flag = 1;
        }
        return flag;
    }

    public int modifyBoardComment(Map<String, Object> map) {

        int flag = 1;
        int result = commentMapper.modifyBoardComment(map);

        if(result >= 1) {
            flag = 0;
        } else {
            flag = 1;
        }
        return flag;
    }

    public int deleteBoardComment(int id, int commentId) {
        int flag = 1;
        int result = commentMapper.deleteBoardComment(id, commentId);

        if(result >= 1) {
            flag = 0;
        } else {
            flag = 1;
        }
        return flag;
    }

    /*------------------------------------- 상품 댓글 ------------------------------------------------ */
    public List<ProductCommentDTO> getProductComment(int productSeq) {
        return commentMapper.getProductComment(productSeq);
    }

    public int writeProductComment(ProductCommentDTO dto) {
        log.info("dto : {}", dto);

        int flag = 1;
        int result = commentMapper.writeProductComment(dto);

        if(result >= 1) {
            flag = 0;
        } else {
            flag = 1;
        }
        return flag;
    }

    public int modifyProductComment(Map<String, Object> map) {

        int flag = 1;
        int result = commentMapper.modifyProductComment(map);

        if(result >= 1) {
            flag = 0;
        } else {
            flag = 1;
        }
        return flag;
    }

    public int deleteProductComment(int id, int commentId) {
        int flag = 1;
        int result = commentMapper.deleteProductComment(id, commentId);

        if(result >= 1) {
            flag = 0;
        } else {
            flag = 1;
        }
        return flag;
    }

    public int getUserIdByCommentSeq(int id) {
        return commentMapper.getUserIdByCommentSeq(id);
    }
}

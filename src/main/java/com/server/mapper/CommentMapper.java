package com.server.mapper;

import com.server.model.BoardCommentDTO;
import com.server.model.ProductCommentDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface CommentMapper {
    /* Board */
    int writeBoardComment(BoardCommentDTO dto);
    List<BoardCommentDTO> getBoardComment(int boardSeq);
    int modifyBoardComment(Map<String, Object> map);
    int deleteBoardComment(int id, int commentId);
    Integer getUserIdByPCommentSeq(int id);


    /* Product */
    int writeProductComment(ProductCommentDTO dto);
    List<ProductCommentDTO> getProductComment(int productSeq);
    int modifyProductComment(Map<String, Object> map);
    int deleteProductComment(int id, int commentId);
    Integer getUserIdByBCommentSeq(int id);
}

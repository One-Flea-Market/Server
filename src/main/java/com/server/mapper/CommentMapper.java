package com.server.mapper;

import com.server.model.BoardDTO;
import com.server.model.CommentDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface CommentMapper {
    int writeComment(CommentDTO dto);
   // int getBoardCount();
    List<CommentDTO> getComment(int boardSeq);
    //BoardDTO getBoardById(int id);
   // List<BoardDTO> getBoardBySearch(@Param("search") String search, @Param("offset") int offset, @Param("pageSize") int pageSize);
    int modifyComment(Map<String, Object> map);
    int deleteComment(int id, int commentId);
}

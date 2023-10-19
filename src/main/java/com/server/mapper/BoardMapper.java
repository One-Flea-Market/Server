package com.server.mapper;

import com.server.model.BoardDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface BoardMapper {
    int writeBoard(BoardDTO dto);
    int getBoardCount();
    List<BoardDTO> getBoard(@Param("offset") int offset);
    List<BoardDTO> getBoardById(int id);
    List<BoardDTO> getBoardBySearch(@Param("search") String search, @Param("offset") int offset);
    int modifyBoard(Map<String, Object> map);
    int getBoardCountBySearch(@Param("search") String search);
    int deleteBoard(int id);
    Integer getUserIdByBoardSeq(int id);
    String getNameByUserId(int id);
}

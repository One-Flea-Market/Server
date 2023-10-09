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
    List<BoardDTO> getBoard(@Param("offset") int offset, @Param("pageSize") int pageSize);
    BoardDTO getBoardById(int id);
    List<BoardDTO> getBoardBySearch(@Param("search") String search, @Param("offset") int offset, @Param("pageSize") int pageSize);
    int modifyBoard(Map<String, Object> map);
    int deleteBoard(int id);
}

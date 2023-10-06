package com.server.mapper;

import com.server.model.BoardDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BoardMapper {
    int write(BoardDTO dto);
    int getBoardCount();
    List<BoardDTO> getBoard(@Param("offset") int offset, @Param("pageSize") int pageSize);
    BoardDTO getBoardById(int id);
}

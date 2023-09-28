package com.server.mapper;

import com.server.model.BoardDTO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BoardMapper {
    int write(BoardDTO dto);
}

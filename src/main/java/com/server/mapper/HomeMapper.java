package com.server.mapper;

import com.server.model.NoticeDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface HomeMapper {
    int write(NoticeDTO dto);
    int getNoticeCount();
    NoticeDTO getNoticeById(int id);
}

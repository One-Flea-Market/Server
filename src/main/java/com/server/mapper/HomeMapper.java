package com.server.mapper;

import com.server.model.InquiryDTO;
import com.server.model.NoticeDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface HomeMapper {
    int write(NoticeDTO dto);
    int getNoticeCount();
    NoticeDTO getNoticeById(int id);
    List<NoticeDTO> getNotice(@Param("offset") int offset, @Param("pageSize") int pageSize);
    List<NoticeDTO> getHomeNotice();
    int postInquiry(InquiryDTO dto);
}

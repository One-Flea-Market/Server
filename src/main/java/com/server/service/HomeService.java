package com.server.service;

import com.server.mapper.HomeMapper;
import com.server.model.InquiryDTO;
import com.server.model.NoticeDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class HomeService {

    private final HomeMapper homeMapper;

    /* 공지사항 작성 */
    public int noticeWrite(NoticeDTO dto) {
        log.info("dto : {}", dto);

        int flag = 1;
        int result = homeMapper.write(dto);

        if(result >= 1) {
            flag = 0;
        } else {
            flag = 1;
        }
        return flag;
    }

    /* 공지사항 리스트 서비스 MyBatis */
    public List<NoticeDTO> getNotice(int page) {
        int offset = (page - 1) * 12;
        log.info("offset : {}", offset);
        return homeMapper.getNotice(offset);
    }

    /* 공지사항 리스트 카운트 */
    public int getNoticeCount() {
        return homeMapper.getNoticeCount();
    }

    /* 요청받은 id로 공지사항 조회 */
    public NoticeDTO getNoticeById(int id) {
        return homeMapper.getNoticeById(id);
    }

    public List<NoticeDTO> getHomeNotice() {
        return homeMapper.getHomeNotice();
    }

    public int postInquiry(InquiryDTO dto) {

        log.info("dto : {}", dto);

        int flag = 1;
        int result = homeMapper.postInquiry(dto);

        if(result >= 1) {
            flag = 0;
        } else {
            flag = 1;
        }
        return flag;
    }
}

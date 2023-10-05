package com.server.controller;


import com.server.model.MessageResF;
import com.server.model.NoticeDTO;
import com.server.repository.NoticeDAO;
import com.server.service.HomeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.Charset;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;

    /* 메인페이지 Carousel 이미지 */
    @GetMapping("/home/picture")
    public ResponseEntity<?> homeCarouselView() {

        return new ResponseEntity<>("Carousel 이미지", HttpStatus.OK);
    }

    /* 메인페이지 공지사항 리스트 */
    /* 메인 화면에서 보여지는 공지사항 리스트 (최신순으로 3개만 보내기) */
    @GetMapping("/home/notice")
    public ResponseEntity<?> homeNoticeView() {

        return new ResponseEntity<>("Home 화면 Notice 리스트", HttpStatus.OK);
    }

    /* 공지사항 작성 */
    @GetMapping("/notice")
    public List<NoticeDTO> noticeView() {
        boolean next = false;

        return homeService.getNotice();
    }

    /* 요청받은 id로 공지사항 조회 */
    @GetMapping("/notice/{id}")
    public ResponseEntity<NoticeDTO> getNoticeById(@PathVariable int id) {
        log.info("{}", id);
        NoticeDTO dto = homeService.getNoticeById(id);

        log.info("{}", dto);
        if (dto == null) {
            // 공지가 없을 경우 404 Not Found 응답을 보냄
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // 공지 정보를 NoticeDTO에 매핑하여 응답
        NoticeDTO noticeDTO = new NoticeDTO();
        noticeDTO.setStrNoticeTitle(dto.getStrNoticeTitle());
        noticeDTO.setStrNoticeDate(dto.getStrNoticeDate());
        noticeDTO.setStrNoticeContent(dto.getStrNoticeContent());
        noticeDTO.setNoticeSeq(dto.getNoticeSeq());

        return new ResponseEntity<>(noticeDTO, HttpStatus.OK);
    }

    @GetMapping("/notice1")
    public ResponseEntity<MessageResF> noticeView1() {
        MessageResF messageResF = new MessageResF();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", Charset.forName("UTF-8")));

        int page = 0;
        int count = homeService.getNoticeCount();
        log.info("count : {}", count);

        while(page < count) {
            page += 10;
            log.info("while 문 안의 page : {}", page);
            log.info("while 문 안의 count : {}", count);
            messageResF.setNoticeList(homeService.getNotice());
            if (count <= page) {
                messageResF.setNext(false);
            } else {
                messageResF.setNext(true);
            }
            return new ResponseEntity<>(messageResF, headers, HttpStatus.OK);
        }
        log.info("while 문 밖의 page : {}", page);
        return new ResponseEntity<>(messageResF, headers, HttpStatus.OK);
    }

    /* 메인페이지 Carousel 이미지 */
    /* 객체안 list원소의 배열의 길이는 12 즉 12개씩 보내야함 next는 12를 제외하고 보여줄 상품이 더있으면 true 없으면 false */
    @GetMapping("/home/product")
    public ResponseEntity<?> homeProductView() {

        return new ResponseEntity<>("Product 리스트", HttpStatus.OK);
    }
}

package com.server.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class HomeController {

    /* 메인페이지 Carousel 이미지 */
    @GetMapping("/home/picture")
    public ResponseEntity<?> homeCarouselView() {

        return new ResponseEntity<>("Carousel 이미지", HttpStatus.OK);
    }

    /* 메인페이지 공지사항 리스트 */
    /* 메인 화면에서 보여지는 공지사항 리스트 (최신순으로 3개만 보내기) */
    @GetMapping("/home/notice")
    public ResponseEntity<?> homeNoticeView() {

        return new ResponseEntity<>("Notice 리스트", HttpStatus.OK);
    }

    /* 메인페이지 Carousel 이미지 */
    /* 객체안 list원소의 배열의 길이는 12 즉 12개씩 보내야함 next는 12를 제외하고 보여줄 상품이 더있으면 true 없으면 false */
    @GetMapping("/home/product")
    public ResponseEntity<?> homeProductView() {

        return new ResponseEntity<>("Product 리스트", HttpStatus.OK);
    }
}

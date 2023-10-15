package com.server.controller;


import com.server.model.InquiryDTO;
import com.server.model.ProductDTO;
import com.server.model.UserDTO;
import com.server.response.MessageRes;
import com.server.response.MessageResNotice;
import com.server.model.NoticeDTO;
import com.server.response.MessageResProduct;
import com.server.service.HomeService;
import com.server.service.ProductService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;
    private final ProductService productService;
    private final SqlSession sqlSession;

    /* 메인페이지 Carousel 이미지 */
    @GetMapping("/home/picture")
    public ResponseEntity<?> homeCarouselView() {

        return new ResponseEntity<>("Carousel 이미지", HttpStatus.OK);
    }

    /* 메인페이지 공지사항 리스트 */
    /* 메인 화면에서 보여지는 공지사항 리스트 (최신순으로 3개만 보내기) */
    @GetMapping("/home/notice")
    public ResponseEntity<?> homeNoticeView() {
        List<NoticeDTO> result = homeService.getHomeNotice();
        log.info("가장 최근에 게시된 공지 : {}", result);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /* 메인페이지 상품 리스트 */
    /* 객체안 list원소의 배열의 길이는 12 즉 12개씩 보내야함 next는 12를 제외하고 보여줄 상품이 더있으면 true 없으면 false */
    @GetMapping("/home/product")
    public ResponseEntity<MessageResProduct> productView() {
        MessageResProduct messageResProduct = new MessageResProduct();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", Charset.forName("UTF-8")));

        int page = 1;
        int pageSize = 12;
        int flag = 0;
        int count = productService.getProductCount();
        log.info("테이블 내 컬럼 개수 ( count ) : {}", count);

        while((page - 1) * pageSize <= count) {
            log.info("while 문 안의 page : {}", page);
            messageResProduct.setProductList(productService.getProduct(page, pageSize));
            log.info("BoardList : {}", messageResProduct.getProductList());
            if (count >= (page - 1) * pageSize) {
                messageResProduct.setNext(true);
                flag = page * pageSize;
                page++;
                log.info(" flag : {}", flag);
                if (count <= flag) {
                    messageResProduct.setNext(false);
                }
            } else {
                messageResProduct.setNext(false);
            }
            log.info("컬럼이 더 있는가? : {}", messageResProduct.isNext());
        }
        log.info("while 문 밖의 page : {}", page);

        return new ResponseEntity<>(messageResProduct, headers, HttpStatus.OK);
    }

    /* 공지사항 작성 */
    @GetMapping("/notice/write")
    public ResponseEntity<?> noticeWrite() {
        boolean next = false;

        return new ResponseEntity<>("Notice 작성", HttpStatus.OK);
    }

    /* 요청받은 id로 공지사항 조회
       공지 id가 고유 id 이며 그 id로 요청을 함 */
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

    /* 객체안 notice 원소의 배열의 길이는 몇개씩 보내도 상관없음(가능하면 10개 정도로 잘라서 보내기)
        next 는 앞서보여준 10개를 제외하고 보여줄 공지가 더있으면 true 없으면 false */
    @GetMapping("/notice")
    public ResponseEntity<MessageResNotice> noticeView() {
        MessageResNotice messageResNotice = new MessageResNotice();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", Charset.forName("UTF-8")));

        int page = 1;
        int pageSize = 10;
        int flag = 0;
        int count = homeService.getNoticeCount();
        log.info("테이블 내 컬럼 개수 ( count ) : {}", count);

        while((page - 1) * pageSize <= count) {
            log.info("while 문 안의 page : {}", page);
            messageResNotice.setNoticeList(homeService.getNotice(page, pageSize));
            log.info("NoticeList : {}", messageResNotice.getNoticeList());
            if (count >= (page - 1) * pageSize) {
                messageResNotice.setNext(true);
                flag = page * pageSize;
                page++;
                log.info(" flag : {}", flag);
                if (count <= flag) {
                    messageResNotice.setNext(false);
                }
            } else {
                messageResNotice.setNext(false);
            }
            log.info("컬럼이 더 있는가? : {}", messageResNotice.isNext());
        }
        log.info("while 문 밖의 page : {}", page);

        return new ResponseEntity<>(messageResNotice, headers, HttpStatus.OK);
    }

    @PostMapping("/inquiry")
    public ResponseEntity<MessageRes> postInquiry(@RequestBody InquiryDTO dto, HttpSession session) {
        MessageRes messageRes = new MessageRes();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", Charset.forName("UTF-8")));

        String sessionFlagYN = "N";

        if (session.getAttribute("dto") == null) {
            sessionFlagYN = "N";
        } else { // 세션이 있으면 여길 탈듯
            /* 게시글 작성 서비스 호출 */
            UserDTO reqDto = (UserDTO) session.getAttribute("dto");
            log.info("session user : {}", session.getAttribute("dto"));
            dto.setUserId(reqDto.getId());
            int flag = homeService.postInquiry(dto);

            if (flag == 0) {
                messageRes.setResult(true);
                messageRes.setMessage("문의가 접수되었습니다.");
            } else {
                messageRes.setResult(false);
                messageRes.setMessage("문의 접수에 실패했습니다.");
            }
            /* 게시글 작성완료는 0이면 true 아니면 false */
            return new ResponseEntity<>(messageRes, headers, HttpStatus.OK);
        }
        /* 세션이 없으면 N */
        messageRes.setMessage("로그인이 되어있지 않습니다.");
        return new ResponseEntity<>(messageRes, headers, HttpStatus.OK);
    }

    @GetMapping("/transaction")
    public ResponseEntity<MessageResProduct> transactionView() {
        MessageResProduct messageResProduct = new MessageResProduct();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", Charset.forName("UTF-8")));

        int page = 1;
        int pageSize = 12;
        int flag = 0;
        int count = productService.getTransactionCount();
        log.info("테이블 내 컬럼 개수 ( count ) : {}", count);

        while((page - 1) * pageSize <= count) {
            log.info("while 문 안의 page : {}", page);
            messageResProduct.setProductList(productService.getTransaction(page, pageSize));
            log.info("TransactionList : {}", messageResProduct.getProductList());
            if (count >= (page - 1) * pageSize) {
                messageResProduct.setNext(true);
                flag = page * pageSize;
                page++;
                log.info(" flag : {}", flag);
                if (count <= flag) {
                    messageResProduct.setNext(false);
                }
            } else {
                messageResProduct.setNext(false);
            }
            log.info("컬럼이 더 있는가? : {}", messageResProduct.isNext());
        }
        log.info("while 문 밖의 page : {}", page);

        return new ResponseEntity<>(messageResProduct, headers, HttpStatus.OK);
    }

    @GetMapping("/transaction/search/{search}")
    public ResponseEntity<MessageResProduct> getTransactionBySearch(@PathVariable String search,
                                                                @RequestParam(defaultValue = "1") int page,
                                                                @RequestParam(defaultValue = "12") int pageSize) {
        MessageResProduct messageResProduct = new MessageResProduct();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", Charset.forName("UTF-8")));

        Map<String, Object> map = new HashMap<>();
        int offset = (page - 1) * pageSize;

        map.put("search", "%" + search.toUpperCase() + "%");
        map.put("offset", offset);
        map.put("pageSize", pageSize);

        List<ProductDTO> result = sqlSession.selectList("com.server.mapper.ProductMapper.getTransactionBySearch", map);

        if (result.isEmpty()) {
            // 결과가 없을 때 처리
            messageResProduct.setNext(false);
        } else {
            if(result.size() < pageSize) {
                messageResProduct.setProductList(result);
                messageResProduct.setNext(false);
            } else{
                // 결과가 있을 때 처리
                messageResProduct.setProductList(result);
                messageResProduct.setNext(true);
            }
        }

        log.info("컬럼이 더 있는가? : {}", messageResProduct.isNext());
        log.info("현재 페이지: {}, 오프셋: {}, 총 검색된 결과 수: {}", page, offset, result.size());
        return new ResponseEntity<>(messageResProduct, headers, HttpStatus.OK);
    }

    @GetMapping("/rental")
    public ResponseEntity<MessageResProduct> rentalView() {
        MessageResProduct messageResProduct = new MessageResProduct();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", Charset.forName("UTF-8")));

        int page = 1;
        int pageSize = 12;
        int flag = 0;
        int count = productService.getRentalCount();
        log.info("테이블 내 컬럼 개수 ( count ) : {}", count);

        while((page - 1) * pageSize <= count) {
            log.info("while 문 안의 page : {}", page);
            messageResProduct.setProductList(productService.getRental(page, pageSize));
            log.info("TransactionList : {}", messageResProduct.getProductList());
            if (count >= (page - 1) * pageSize) {
                messageResProduct.setNext(true);
                flag = page * pageSize;
                page++;
                log.info(" flag : {}", flag);
                if (count <= flag) {
                    messageResProduct.setNext(false);
                }
            } else {
                messageResProduct.setNext(false);
            }
            log.info("컬럼이 더 있는가? : {}", messageResProduct.isNext());
        }
        log.info("while 문 밖의 page : {}", page);

        return new ResponseEntity<>(messageResProduct, headers, HttpStatus.OK);
    }

    @GetMapping("/rental/search/{search}")
    public ResponseEntity<MessageResProduct> getRentalBySearch(@PathVariable String search,
                                                                    @RequestParam(defaultValue = "1") int page,
                                                                    @RequestParam(defaultValue = "12") int pageSize) {
        MessageResProduct messageResProduct = new MessageResProduct();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", Charset.forName("UTF-8")));

        Map<String, Object> map = new HashMap<>();
        int offset = (page - 1) * pageSize;

        map.put("search", "%" + search.toUpperCase() + "%");
        map.put("offset", offset);
        map.put("pageSize", pageSize);

        List<ProductDTO> result = sqlSession.selectList("com.server.mapper.ProductMapper.getRentalBySearch", map);

        if (result.isEmpty()) {
            // 결과가 없을 때 처리
            messageResProduct.setNext(false);
        } else {
            if(result.size() < pageSize) {
                messageResProduct.setProductList(result);
                messageResProduct.setNext(false);
            } else{
                // 결과가 있을 때 처리
                messageResProduct.setProductList(result);
                messageResProduct.setNext(true);
            }
        }

        log.info("컬럼이 더 있는가? : {}", messageResProduct.isNext());
        log.info("현재 페이지: {}, 오프셋: {}, 총 검색된 결과 수: {}", page, offset, result.size());
        return new ResponseEntity<>(messageResProduct, headers, HttpStatus.OK);
    }

}

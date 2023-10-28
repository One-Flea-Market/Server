package com.server.controller;


import com.server.model.InquiryDTO;
import com.server.model.ProductDTO;
import com.server.model.UserDTO;
import com.server.model.NoticeDTO;
import com.server.service.HomeService;
import com.server.service.ProductService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = {"https://client-p34zpc52f-capstone-team-market.vercel.app/", "https://localhost:3001"})
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
    public ResponseEntity<?> productView(@RequestParam(defaultValue = "1") int page,
                                         @RequestParam(defaultValue = "12") int pageSize) {
        int count = productService.getProductCount();
        log.info("테이블 내 컬럼 개수 ( count ) : {}", count);

        // 서비스에서 상품 목록을 가져옴
        List<ProductDTO> productList = productService.getProduct(page);

        // 응답 데이터 구성을 위한 리스트
        List<Map<String, Object>> responseList = new ArrayList<>();

        // 현재 페이지에서 보내줄 상품 개수
        int itemCount = 0;

        for (ProductDTO listDto : productList) {
            Map<String, Object> productMap = new LinkedHashMap<>();                 // 상품 정보들을 저장해 출력할 Map 선언
            productMap.put("id", listDto.getId());                  // 상품 고유 id 설정
            productMap.put("title", listDto.getTitle());        // 상품 이름 설정
            productMap.put("status", listDto.getStatus());      // 상품 카테고리 설정
            productMap.put("price", listDto.getPrice());        // 상품 가격 설정

            String linkAsString = listDto.getList();
            List<String> imageLinks = Arrays.asList(linkAsString.split(","));

            Random random = new Random();   // 대표 이미지를 랜덤하게 선정

            if(!imageLinks.isEmpty()){      // 대표 이미지 설정
                int randomIndex = random.nextInt(imageLinks.size());

                String representativeImage = imageLinks.get(randomIndex);
                productMap.put("image", representativeImage);
            }

            // 응답 데이터에 상품 정보 추가
            responseList.add(productMap);

            // 현재 페이지에서 보내준 상품 개수 증가
            itemCount++;

            // 만약 현재 페이지에서 보내줄 상품 개수가 pageSize와 같거나 크다면 루프 종료
            if (itemCount >= pageSize) {
                page++;
                log.info("{}", page);
                break;
            }
        }
        log.info("productList : {}", productList);

        Map<String, Object> response = new HashMap<>();
        response.put("list", responseList);
        response.put("next", itemCount >= pageSize);

        log.info("while 문 밖의 page : {}", page);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /* 공지사항 작성 */
    @GetMapping("/notice/write")
    public ResponseEntity<?> noticeWrite() {
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
            log.info("dto가 비어있음 (null)");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // 공지 정보를 NoticeDTO에 매핑하여 응답
        NoticeDTO noticeDTO = new NoticeDTO();
        noticeDTO.setTitle(dto.getTitle());
        noticeDTO.setDate(dto.getDate());
        noticeDTO.setBody(dto.getBody());
        noticeDTO.setId(dto.getId());

        return new ResponseEntity<>(noticeDTO, HttpStatus.OK);
    }

    /* 객체안 notice 원소의 배열의 길이는 몇개씩 보내도 상관없음(가능하면 10개 정도로 잘라서 보내기)
        next 는 앞서보여준 10개를 제외하고 보여줄 공지가 더있으면 true 없으면 false */
    @GetMapping("/notice")
    public ResponseEntity<?> noticeView(@RequestParam(defaultValue = "1") int page,
                                        @RequestParam(defaultValue = "10") int pageSize) {

        int count = homeService.getNoticeCount();
        log.info("테이블 내 컬럼 개수 ( count ) : {}", count);

        // 서비스에서 공지 목록을 가져옴
        List<NoticeDTO> noticeList = homeService.getNotice(page);

        // 응답 데이터 구성을 위한 리스트
        List<Map<String, Object>> responseList = new ArrayList<>();

        // 현재 페이지에서 보내줄 상품 개수
        int noticeCount = 0;

        for (NoticeDTO listDto : noticeList) {
            Map<String, Object> noticeMap = new LinkedHashMap<>();               // 공지 정보들을 저장해 출력할 Map 선언
            noticeMap.put("id", listDto.getId());                  // 공지 고유 id 설정
            noticeMap.put("title", listDto.getTitle());        // 공지 이름 설정
            noticeMap.put("date", listDto.getDate());          // 공지 작성날짜 설정

            // 응답 데이터에 상품 정보 추가
            responseList.add(noticeMap);

            // 현재 페이지에서 보내준 상품 개수 증가
            noticeCount++;

            // 만약 현재 페이지에서 보내줄 상품 개수가 pageSize와 같거나 크다면 루프 종료
            if (noticeCount >= pageSize) {
                page++;
                log.info("{}", page);
                break;
            }
        }
        log.info("while 문 밖의 page : {}", page);
        log.info("noticeList : {}", noticeList);

        Map<String, Object> response = new HashMap<>();
        response.put("list", responseList);
        response.put("next", noticeCount >= pageSize);

        log.info("while 문 밖의 page : {}", page);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/inquiry")
    public ResponseEntity<?> postInquiry(@RequestBody InquiryDTO dto, HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        if (session.getAttribute("dto") == null) {
            response.put("result", false);
            response.put("message", "로그인 상태가 아닙니다.");
            log.info("로그인 상태가 아님");
        } else { // 세션이 있으면 여길 탈듯
            /* 게시글 작성 서비스 호출 */
            UserDTO reqDto = (UserDTO) session.getAttribute("dto");
            log.info("session user : {}", session.getAttribute("dto"));
            dto.setUserId(reqDto.getId());
            int flag = homeService.postInquiry(dto);

            if (flag == 0) {
                response.put("result", true);
                response.put("message", "문의가 접수 되었습니다..");
            } else {
                response.put("result", false);
                response.put("message", "문의 접수에 실패했습니다.");
            }
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/transaction")
    public ResponseEntity<?> transactionView(@RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "12") int pageSize) {
        int count = productService.getTransactionCount();
        log.info("테이블 내 컬럼 개수 ( count ) : {}", count);

        // 서비스에서 상품 목록을 가져옴
        List<ProductDTO> productList = productService.getTransaction(page);

        // 응답 데이터 구성을 위한 리스트
        List<Map<String, Object>> responseList = new ArrayList<>();

        // 현재 페이지에서 보내줄 상품 개수
        int itemCount = 0;

        for (ProductDTO listDto : productList) {
            Map<String, Object> productMap = new LinkedHashMap<>();                 // 상품 정보들을 저장해 출력할 Map 선언
            productMap.put("id", listDto.getId());                  // 상품 고유 id 설정
            productMap.put("title", listDto.getTitle());        // 상품 이름 설정
            productMap.put("status", listDto.getStatus());      // 상품 카테고리 설정
            productMap.put("price", listDto.getPrice());        // 상품 가격 설정

            String linkAsString = listDto.getList();
            List<String> imageLinks = Arrays.asList(linkAsString.split(","));

            Random random = new Random();   // 대표 이미지를 랜덤하게 선정

            if(!imageLinks.isEmpty()){      // 대표 이미지 설정
                int randomIndex = random.nextInt(imageLinks.size());

                String representativeImage = imageLinks.get(randomIndex);
                productMap.put("image", representativeImage);
            }

            // 응답 데이터에 상품 정보 추가
            responseList.add(productMap);

            // 현재 페이지에서 보내준 상품 개수 증가
            itemCount++;

            // 만약 현재 페이지에서 보내줄 상품 개수가 pageSize와 같거나 크다면 루프 종료
            if (itemCount >= pageSize) {
                page++;
                log.info("{}", page);
                break;
            }
        }
        log.info("productList : {}", productList);

        Map<String, Object> response = new HashMap<>();
        response.put("list", responseList);
        response.put("next", itemCount >= pageSize);

        log.info("while 문 밖의 page : {}", page);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/transaction/search/{search}")
    public ResponseEntity<?> getTransactionBySearch(@PathVariable String search,
                                                    @RequestParam(defaultValue = "1") int page,
                                                    @RequestParam(defaultValue = "12") int pageSize) {
        int count = productService.getTransactionCount();
        log.info("테이블 내 컬럼 개수 ( count ) : {}", count);

        // 서비스에서 상품 목록을 가져옴
        List<ProductDTO> productList = productService.getTransactionBySearch(search, page);

        // 응답 데이터 구성을 위한 리스트
        List<Map<String, Object>> responseList = new ArrayList<>();

        // 현재 페이지에서 보내줄 상품 개수
        int itemCount = 0;

        for (ProductDTO listDto : productList) {
            Map<String, Object> productMap = new LinkedHashMap<>();                 // 상품 정보들을 저장해 출력할 Map 선언
            productMap.put("id", listDto.getId());                  // 상품 고유 id 설정
            productMap.put("title", listDto.getTitle());        // 상품 이름 설정
            productMap.put("status", listDto.getStatus());      // 상품 카테고리 설정
            productMap.put("price", listDto.getPrice());        // 상품 가격 설정

            String linkAsString = listDto.getList();
            List<String> imageLinks = Arrays.asList(linkAsString.split(","));

            Random random = new Random();   // 대표 이미지를 랜덤하게 선정

            if(!imageLinks.isEmpty()){      // 대표 이미지 설정
                int randomIndex = random.nextInt(imageLinks.size());

                String representativeImage = imageLinks.get(randomIndex);
                productMap.put("image", representativeImage);
            }

            // 응답 데이터에 상품 정보 추가
            responseList.add(productMap);

            // 현재 페이지에서 보내준 상품 개수 증가
            itemCount++;

            // 만약 현재 페이지에서 보내줄 상품 개수가 pageSize와 같거나 크다면 루프 종료
            if (itemCount >= pageSize) {
                page++;
                log.info("{}", page);
                break;
            }
        }
        log.info("productList : {}", productList);

        Map<String, Object> response = new HashMap<>();
        response.put("list", responseList);
        response.put("next", itemCount >= pageSize);

        log.info("while 문 밖의 page : {}", page);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/rental")
    public ResponseEntity<?> rentalView(@RequestParam(defaultValue = "1") int page,
                                        @RequestParam(defaultValue = "12") int pageSize) {
        int count = productService.getRentalCount();
        log.info("테이블 내 컬럼 개수 ( count ) : {}", count);

        // 서비스에서 상품 목록을 가져옴
        List<ProductDTO> productList = productService.getRental(page);

        // 응답 데이터 구성을 위한 리스트
        List<Map<String, Object>> responseList = new ArrayList<>();

        // 현재 페이지에서 보내줄 상품 개수
        int itemCount = 0;

        for (ProductDTO listDto : productList) {
            Map<String, Object> productMap = new LinkedHashMap<>();                 // 상품 정보들을 저장해 출력할 Map 선언
            productMap.put("id", listDto.getId());                  // 상품 고유 id 설정
            productMap.put("title", listDto.getTitle());        // 상품 이름 설정
            productMap.put("status", listDto.getStatus());      // 상품 카테고리 설정
            productMap.put("price", listDto.getPrice());        // 상품 가격 설정

            String linkAsString = listDto.getList();
            List<String> imageLinks = Arrays.asList(linkAsString.split(","));

            Random random = new Random();   // 대표 이미지를 랜덤하게 선정

            if(!imageLinks.isEmpty()){      // 대표 이미지 설정
                int randomIndex = random.nextInt(imageLinks.size());

                String representativeImage = imageLinks.get(randomIndex);
                productMap.put("image", representativeImage);
            }

            // 응답 데이터에 상품 정보 추가
            responseList.add(productMap);

            // 현재 페이지에서 보내준 상품 개수 증가
            itemCount++;

            // 만약 현재 페이지에서 보내줄 상품 개수가 pageSize와 같거나 크다면 루프 종료
            if (itemCount >= pageSize) {
                page++;
                log.info("{}", page);
                break;
            }
        }
        log.info("productList : {}", productList);

        Map<String, Object> response = new HashMap<>();
        response.put("list", responseList);
        response.put("next", itemCount >= pageSize);

        log.info("while 문 밖의 page : {}", page);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/rental/search/{search}")
    public ResponseEntity<?> getRentalBySearch(@PathVariable String search,
                                               @RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "12") int pageSize) {
        int count = productService.getRentalCount();
        log.info("테이블 내 컬럼 개수 ( count ) : {}", count);

        // 서비스에서 상품 목록을 가져옴
        List<ProductDTO> productList = productService.getRentalBySearch(search, page);

        // 응답 데이터 구성을 위한 리스트
        List<Map<String, Object>> responseList = new ArrayList<>();

        // 현재 페이지에서 보내줄 상품 개수
        int itemCount = 0;

        for (ProductDTO listDto : productList) {
            Map<String, Object> productMap = new LinkedHashMap<>();                 // 상품 정보들을 저장해 출력할 Map 선언
            productMap.put("id", listDto.getId());                  // 상품 고유 id 설정
            productMap.put("title", listDto.getTitle());        // 상품 이름 설정
            productMap.put("status", listDto.getStatus());      // 상품 카테고리 설정
            productMap.put("price", listDto.getPrice());        // 상품 가격 설정

            String linkAsString = listDto.getList();
            List<String> imageLinks = Arrays.asList(linkAsString.split(","));

            Random random = new Random();   // 대표 이미지를 랜덤하게 선정

            if(!imageLinks.isEmpty()){      // 대표 이미지 설정
                int randomIndex = random.nextInt(imageLinks.size());

                String representativeImage = imageLinks.get(randomIndex);
                productMap.put("image", representativeImage);
            }

            // 응답 데이터에 상품 정보 추가
            responseList.add(productMap);

            // 현재 페이지에서 보내준 상품 개수 증가
            itemCount++;

            // 만약 현재 페이지에서 보내줄 상품 개수가 pageSize와 같거나 크다면 루프 종료
            if (itemCount >= pageSize) {
                page++;
                log.info("{}", page);
                break;
            }
        }
        log.info("productList : {}", productList);

        Map<String, Object> response = new HashMap<>();
        response.put("list", responseList);
        response.put("next", itemCount >= pageSize);

        log.info("while 문 밖의 page : {}", page);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}

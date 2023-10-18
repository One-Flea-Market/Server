package com.server.controller;

import com.server.model.ProductDTO;
import com.server.response.MessageRes;
import com.server.model.UserDTO;
import com.server.response.MessageResProduct;
import com.server.service.ProductService;
import com.server.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.Charset;
import java.util.*;


@Slf4j
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final ProductService productService;

    /* 로그인 */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserDTO dto, HttpSession session) {

        UserDTO rspDto = userService.getOneUser(dto);
        if (rspDto != null) {
            /* session 생성 처리 필요 */
            session.setAttribute("dto", rspDto);
            session.setMaxInactiveInterval(1800);
            return new ResponseEntity<>(rspDto, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /* 회원가입 */
    @PostMapping("/sign-in")
    public ResponseEntity<?> join(@RequestBody UserDTO dto) {
        int flag = userService.joinUser(dto);

        return new ResponseEntity<>(flag, HttpStatus.OK);
    }

    /* 로그아웃 */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        /* session 만료처리 필요 */
        log.info("세션 {}",session.getAttribute("dto"));
        session.invalidate();
        return new ResponseEntity<>(null, HttpStatus.OK);
    }

    /* 세션 체크 */
    @GetMapping("/check")
    public ResponseEntity<?> sessionCheck(HttpServletRequest request) {
        boolean sessionFlagYN = false;
        request.getSession(false);
        log.info("세션 조회 {}",request.getSession(false));
        if(request.getSession(false) == null) {
            sessionFlagYN = false;
        } else {
            sessionFlagYN = true;
        }
        return new ResponseEntity<>(sessionFlagYN, HttpStatus.OK);
    }

    /* 이메일 인증 및 중복 체크 */
    @PostMapping("/sign-in/email")
    public ResponseEntity<MessageRes> emailCheck(@RequestParam String email) {
        MessageRes messageRes = new MessageRes();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", Charset.forName("UTF-8")));

        log.info("이메일 확인 {}", email);
        String result = userService.emailCheck(email);
        log.info("이메일 로그 확인 {}", result);

        if (Objects.equals(result, email)) {
            messageRes.setMessage("이미 존재하는 이메일입니다.");
            messageRes.setResult(false);

            return new ResponseEntity<>(messageRes, headers, HttpStatus.OK);
        } else {
            String systemAuthNumber = userService.mailSender(email);
            log.info("인증 번호 : {}", systemAuthNumber);

            messageRes.setResult(true);
            //messageRes.setAuth("systemAuthNumber");
            messageRes.setAuth(systemAuthNumber);

            return new ResponseEntity<>(messageRes, headers, HttpStatus.CREATED);
        }
    }

    /* 닉네임 중복 체크 */
    @PostMapping("/sign-in/username")
    public ResponseEntity<MessageRes> nameCheck(@RequestParam String name) {
        MessageRes messageRes = new MessageRes();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", Charset.forName("UTF-8")));

        log.info("닉네임 확인 {}", name);
        String result = userService.nameCheck(name);
        log.info("닉네임 로그 확인 {}", result);

        if (Objects.equals(result, name)) {
            messageRes.setMessage("이미 존재하는 닉네임입니다.");
            messageRes.setResult(false);

            return new ResponseEntity<>(messageRes, headers, HttpStatus.OK);
        } else {
            messageRes.setResult(true);
            messageRes.setAuth("사용 가능한 닉네임 입니다.");

            return new ResponseEntity<>(messageRes, headers, HttpStatus.CREATED);
        }
    }

    @GetMapping("/admin")
    public ResponseEntity<?> mypageView(UserDTO dto, HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        if(session.getAttribute("dto") == null) {   // dto 즉 세션이 null일 때
            log.info("로그인 필요");
            response.put("login", false);   // login : false 객체 설정
        } else {
            UserDTO reqDto = (UserDTO) session.getAttribute("dto");
            log.info("session user : {}", session.getAttribute("dto"));
            dto.setEmail(reqDto.getEmail());

            log.info("읽어온 email  {}", dto);
            log.info("읽어온 reqDto  {}", reqDto);

            if (reqDto != null && Objects.equals(reqDto.getEmail(), dto.getEmail())) {
                log.info("mypage 호출 성공");
                UserDTO userDTO = userService.mypage(reqDto);
                return new ResponseEntity<>(userDTO, HttpStatus.OK);
            }
        }
        return new ResponseEntity<>(response, HttpStatus.OK);   // 비로그인 객체 반환
    }

    @GetMapping("/admin/product")
    public ResponseEntity<?> myProductView(UserDTO dto, HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        if(session.getAttribute("dto") == null) {   // dto 즉 세션이 null일 때
            log.info("로그인 필요");
            response.put("login", false);   // login : false 객체 설정
        } else {
            UserDTO reqDto = (UserDTO) session.getAttribute("dto");
            log.info("session user : {}", session.getAttribute("dto"));
            int userId = reqDto.getId();

            // 서비스에서 상품 목록을 가져옴
            List<ProductDTO> productList = userService.getMyProduct(userId);

            // 응답 데이터 구성을 위한 리스트
            List<Map<String, Object>> responseList = new ArrayList<>();

            for (ProductDTO listDto : productList) {
                Map<String, Object> productMap = new LinkedHashMap<>();                 // 상품 정보들을 저장해 출력할 Map 선언
                productMap.put("id", listDto.getId());                  // 상품 고유 id 설정
                productMap.put("title", listDto.getTitle());        // 상품 이름 설정
                productMap.put("status", listDto.getStatus());      // 상품 카테고리 설정
                productMap.put("price", listDto.getPrice());        // 상품 가격 설정

                String linkAsString = listDto.getList();
                List<String> imageLinks = Arrays.asList(linkAsString.split(","));

                Random random = new Random();   // 대표 이미지를 랜덤하게 선정

                if (!imageLinks.isEmpty()) {      // 대표 이미지 설정
                    int randomIndex = random.nextInt(imageLinks.size());

                    String representativeImage = imageLinks.get(randomIndex);
                    productMap.put("image", representativeImage);
                }

                // 응답 데이터에 상품 정보 추가
                responseList.add(productMap);
            }
            log.info("productList : {}", productList);

            response.put("list", responseList);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/cart")
    public ResponseEntity<?> userLikeProduct(HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        if(session.getAttribute("dto") == null) {   // dto 즉 세션이 null일 때
            log.info("로그인 필요");
            response.put("login", false);   // login : false 객체 설정
        } else {
            UserDTO reqDto = (UserDTO) session.getAttribute("dto");
            log.info("session user : {}", session.getAttribute("dto"));
            int userId = reqDto.getId();

            // 서비스에서 상품 목록을 가져옴
            List<ProductDTO> productList = productService.getAllProduct();
            if(productList.isEmpty()){
                log.info("productList is empty");
            } else {
                // 결과가 있는 경우, products 리스트에 결과가 저장되어 있습니다.
                log.info("Number of products found: {}", productList.size());
            }
            log.info("productList 1 : {}", productList);

            // 응답 데이터 구성을 위한 리스트
            List<Map<String, Object>> responseList = new ArrayList<>();

            for (ProductDTO listDto : productList) {
                log.info("listDto : {}", listDto);
                Map<String, Object> productMap = new LinkedHashMap<>();                 // 상품 정보들을 저장해 출력할 Map 선언
                boolean onlike = userService.getMyLikedProducts(userId, listDto.getId());
                log.info("onlike : {}", onlike);

                if(onlike){
                    productMap.put("id", listDto.getId());              // 상품 고유 id 설정
                    productMap.put("title", listDto.getTitle());        // 상품 이름 설정
                    productMap.put("status", listDto.getStatus());      // 상품 카테고리 설정
                    productMap.put("price", listDto.getPrice());        // 상품 가격 설정

                    String linkAsString = listDto.getList();
                    List<String> imageLinks = Arrays.asList(linkAsString.split(","));

                    Random random = new Random();   // 대표 이미지를 랜덤하게 선정

                    if (!imageLinks.isEmpty()) {      // 대표 이미지 설정
                        int randomIndex = random.nextInt(imageLinks.size());

                        String representativeImage = imageLinks.get(randomIndex);
                        productMap.put("image", representativeImage);
                    }
                    // 응답 데이터에 상품 정보 추가
                    responseList.add(productMap);
                }
                log.info("productList 2 : {}", productList);
            }

            response.put("list", responseList);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}

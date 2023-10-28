package com.server.controller;

import com.server.model.ProductDTO;
import com.server.model.UserDTO;
import com.server.service.ProductService;
import com.server.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;


@Slf4j
@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = {"https://client-p34zpc52f-capstone-team-market.vercel.app/", "http://localhost:3000"})
public class UserController {

    private final UserService userService;
    private final ProductService productService;

    /* 로그인 */

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserDTO dto, HttpSession session, HttpServletResponse response) {

        Map<String, Object> responseBody = new HashMap<>();

        UserDTO rspDto = userService.getOneUser(dto);
        if (rspDto != null) {
            /* 세션 생성 및 유효시간 설정 */
            session.setAttribute("dto", rspDto);
            session.setMaxInactiveInterval(1800); // 1800초 (30분)

            /* 세션 정보를 쿠키로 설정하여 클라이언트에게 전송 */
            Cookie cookie = new Cookie("JSESSIONID", session.getId());
            cookie.setMaxAge(1800); // 1800초 (30분)
            cookie.setHttpOnly(true);
            cookie.setSecure(true);
            cookie.setPath("/"); // 쿠키 경로 설정
            response.addCookie(cookie);
            response.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
            response.setHeader("Access-Control-Allow-Credentials", "true");

            responseBody.put("result", true);
            return new ResponseEntity<>(responseBody, HttpStatus.OK);
        }
        responseBody.put("result", false);
        responseBody.put("message", "이메일 혹은 비밀번호가 틀립니다.");
        return new ResponseEntity<>(responseBody, HttpStatus.NOT_FOUND);
    }

    /* 회원가입 */
    @PostMapping("/sign-in")
    public ResponseEntity<?> join(@RequestBody UserDTO dto) {
        int flag = userService.joinUser(dto);

        Map<String, Object> response = new HashMap<>();

        if(flag == 0) {
            response.put("result", true);
        } else {
            response.put("result", false);
            response.put("message", "모종의 이유로 회원가입에 실패했습니다.");
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
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

        Map<String, Object> response = new HashMap<>();

        request.getSession(false);
        log.info("세션 조회 {}",request.getSession(false));
        if(request.getSession(false) == null) {
            response.put("login", false);
        } else {
            response.put("login", true);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /* 이메일 인증 및 중복 체크 */
    @PostMapping("/sign-in/email")
    public ResponseEntity<?> emailCheck(@RequestBody UserDTO requestDto) {

        Map<String, Object> response = new HashMap<>();

        log.info("이메일 확인 {}", requestDto.getEmail());
        String result = userService.emailCheck(requestDto.getEmail());
        log.info("이메일 로그 확인 {}", result);

        if (Objects.equals(result, requestDto.getEmail())) {
            response.put("message", "이미 등록되어 있는 이메일 입니다.");
            response.put("result", false);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            String systemAuthNumber = userService.mailSender(requestDto.getEmail());
            log.info("인증 번호 : {}", systemAuthNumber);

            response.put("result", true);
            response.put("Auth", systemAuthNumber);

            return new ResponseEntity<>(response, HttpStatus.OK);
        }
    }

    /* 닉네임 중복 체크 */
    @PostMapping("/sign-in/username")
    public ResponseEntity<?> nameCheck(@RequestBody UserDTO requestDto) {
        Map<String, Object> response = new HashMap<>();

        log.info("request username : {}", requestDto);

        String result = userService.nameCheck(requestDto.getUsername());
        log.info("result {}", result);

        if (Objects.equals(result, requestDto.getUsername())) {
            response.put("message", "이미 등록되어 있는 닉네임 입니다.");
            response.put("result", false);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            response.put("message", "사용 가능한 닉네임 입니다.");
            response.put("result", true);

            return new ResponseEntity<>(response, HttpStatus.OK);
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
            log.info("productList : {}", productList);

            // 응답 데이터 구성을 위한 리스트
            List<Map<String, Object>> responseList = new ArrayList<>();

            for (ProductDTO listDto : productList) {
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
            }

            response.put("list", responseList);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/cart/{id}")
    public ResponseEntity<?> deleteCart (@PathVariable int id, HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        if (session.getAttribute("dto") == null) {   // dto 즉 세션이 null일 때
            log.info("로그인 필요");
            response.put("login", false);   // login : false 객체 설정
        } else {
            UserDTO reqDto = (UserDTO) session.getAttribute("dto");
            log.info("session user : {}", session.getAttribute("dto"));
            int userId = reqDto.getId();
            int productSeq = id;

            int flag = userService.deleteCart(userId, productSeq);

            if(flag == 0) {
                log.info("성공");
                response.put("result", true);
            } else {
                log.info("실패");
                response.put("result", false);
                response.put("message", "모종의 이유로 삭제에 실패했습니다.");
            }
        }
        return ResponseEntity.ok(response);
    }
}

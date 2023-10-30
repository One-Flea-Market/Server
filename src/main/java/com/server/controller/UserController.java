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
import org.apache.catalina.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;


@Slf4j
@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = {"https://client-p34zpc52f-capstone-team-market.vercel.app/", "https://localhost:3001"})
public class UserController {

    private final UserService userService;
    private final ProductService productService;

    public static void addCookie(HttpServletResponse response, String name, String value) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .path("/")
                .sameSite("None")
                .httpOnly(true)
                .secure(true)
                .maxAge(1800)
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }

    /* 로그인 */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserDTO dto, HttpServletRequest request, HttpServletResponse response) {

        Map<String, Object> responseBody = new HashMap<>();

        UserDTO rspDto = userService.LoginUser(dto);
        if (rspDto != null) {
            /* 세션 생성 및 유효시간 설정 */
            HttpSession session = request.getSession();
            session.setAttribute("dto", rspDto);

            addCookie(response, "JSESSIONID", session.getId());
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
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        /* session 만료처리 필요 */
        HttpSession session = request.getSession();
        if(session != null) {
            UserDTO user = (UserDTO) session.getAttribute("dto");
            log.info("세션 : {}", user);
            session.removeAttribute("dto");
        }

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for(Cookie cookie : cookies) {
                if(cookie.getName().equals("JSESSIONID")) {
                    cookie.setMaxAge(0);
                    cookie.setPath("/");
                    response.addCookie(cookie);
                    break;
                }
            }
        }
        return new ResponseEntity<>(null, HttpStatus.OK);
    }

    /* 세션 체크 */
    @GetMapping("/check")
    public ResponseEntity<?> sessionCheck(HttpServletRequest request) {

        Map<String, Object> response = new HashMap<>();
        // 쿠키
        Cookie[] cookies = request.getCookies();
        String sessionId = null;
        Integer userId;
        Boolean loginCheck = null;
        UserDTO user = new UserDTO();

        log.info("request : {}", request);
        log.info("cookies : {}", cookies);

        if(cookies != null) {
            for (Cookie cookie : cookies) {
                log.info("cookie : {}", cookie);
                if(cookie.getName().equals("JSESSIONID")) {     // JSESSIONID 라는 이름을 가진 쿠키 조회
                    HttpSession session = request.getSession();
                    log.info("session : {} ", session);
                    user = (UserDTO) session.getAttribute("dto");   // user 객체에 dto 세션 내의 유저 정보 저장
                    log.info("user", user);
                    if(user == null) {  // user가 null 일 때, for문 탈출 ( 500 에러 위함 )
                        break;
                    }
                    userId = user.getId();  // user 객체에서 USER_ID 가져옴
                    log.info("userId : {}", userId);
                    sessionId = cookie.getValue();  // sessionId에 현재 쿠키 값 저장
                    log.info("sessionId : {}", sessionId);
                    loginCheck = userService.checkLogin(userId);    // 실제 로그인 체크 (USER_ID를 이용해 레코드가 한개라도 일치하는게 있으면 true)
                    log.info("loginCheck : {}", loginCheck);
                    break;
                }
            }
        }
        // log.info("loginCheck : {}", loginCheck);    // true or false

        if(sessionId != null) {     // sessionId not null
            if(loginCheck == true) {    // loginCheck is true
                log.info("Now Login User Info : {}", user);
                response.put("login", true);    // login : true
                log.info("Session Id (JSESSIONID) : {}", sessionId);
                return new ResponseEntity<>(response, HttpStatus.OK);
            }
        } else {
            response.put("login", false);       // login : false
            log.info("Session Id is Not Found (Null).");
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

    /* 마이 페이지 */
    @GetMapping("/admin")
    public ResponseEntity<?> mypageView(UserDTO dto, HttpServletRequest request) {

        Map<String, Object> response = new HashMap<>();

        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("JSESSIONID")) {
                    String sessionId = cookie.getValue();
                    log.info("Session Id (JSESSIONID) : {}", sessionId);

                    HttpSession session = request.getSession();
                    UserDTO reqDto = (UserDTO) session.getAttribute("dto");
                    log.info("session user : {}", session.getAttribute("dto"));
                    dto.setEmail(reqDto.getEmail());

                    log.info("읽어온 email  {}", dto);
                    log.info("읽어온 reqDto  {}", reqDto);

                    if (reqDto != null && Objects.equals(reqDto.getEmail(), dto.getEmail())) {
                        log.info("mypage 호출 성공");
                        UserDTO userDTO = userService.mypage(reqDto);
                        response.put("response", userDTO);
                        return new ResponseEntity<>(response, HttpStatus.OK);
                    }
                }
            }
        } else {
            response.put("login", false);
            log.info("Session Id is Not Found.");
        }
        return new ResponseEntity<>(response, HttpStatus.OK);   // 비로그인 객체 반환
    }

    @GetMapping("/admin/product")
    public ResponseEntity<?> myProductView(HttpServletRequest request) {

        Map<String, Object> response = new HashMap<>();

        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("JSESSIONID")) {
                    String sessionId = cookie.getValue();
                    log.info("Session Id (JSESSIONID) : {}", sessionId);

                    HttpSession session = request.getSession();
                    UserDTO reqDto = (UserDTO) session.getAttribute("dto");
                    log.info("session user : {}", session.getAttribute("dto"));

                    int userId = reqDto.getId();


                    // 서비스에서 상품 목록을 가져옴
                    List<ProductDTO> productList = userService.getMyProduct(userId);

                    // 응답 데이터 구성을 위한 리스트
                    List<Map<String, Object>> responseList = new ArrayList<>();

                    for (ProductDTO listDto : productList) {
                        Map<String, Object> productMap = new LinkedHashMap<>();     // 상품 정보들을 저장해 출력할 Map 선언
                        productMap.put("id", listDto.getId());                      // 상품 고유 id 설정
                        productMap.put("title", listDto.getTitle());                // 상품 이름 설정
                        productMap.put("status", listDto.getStatus());              // 상품 카테고리 설정
                        productMap.put("price", listDto.getPrice());                // 상품 가격 설정

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
            }
        } else {
            response.put("login", false);
            log.info("Session Id is Not Found.");
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

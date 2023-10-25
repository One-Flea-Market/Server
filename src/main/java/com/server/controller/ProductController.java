package com.server.controller;

import com.server.mapper.ProductMapper;
import com.server.model.*;
import com.server.service.AmazonS3Service;
import com.server.service.CommentService;
import com.server.service.ProductService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = {"https://client-p34zpc52f-capstone-team-market.vercel.app/", "localhost:3000"})
@RequestMapping("/items")
public class ProductController {
    private final ProductService productService;
    private final CommentService commentService;
    private final SqlSession sqlSession;
    private final AmazonS3Service amazonS3Service;
    private final ProductMapper productMapper;

    /* 상품 메인 */
    @GetMapping("")
    public ResponseEntity<?> productView() {

        int page = 1;
        int pageSize = 12;
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
                productMap.put("list", representativeImage);
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

    /* 상품 등록 */
    @Transactional
    @PostMapping("/registration")
    public ResponseEntity<Map<String, Object>> registerProduct(ProductRequestDTO requestDTO,
                                                               HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        if (session.getAttribute("dto") == null) {
            log.info("실패");
            response.put("result", false);
            response.put("message", "로그인 상태가 아닙니다.");
        } else { // 세션이 있으면 여길 탈듯
            /* 상품 등록 서비스 호출 */
            UserDTO reqDto = (UserDTO) session.getAttribute("dto");
            log.info("session user : {}", session.getAttribute("dto"));

            ProductDTO dto = new ProductDTO();
            dto.setTitle(requestDTO.getTitle());
            dto.setStatus(requestDTO.getStatus());
            dto.setBody(requestDTO.getBody());
            dto.setDate(requestDTO.getDate());
            dto.setPrice(requestDTO.getPrice());
            dto.setUserId(reqDto.getId());

            List<MultipartFile> images = requestDTO.getImageFiles();

            log.info("imageLinks.size : {}", images.size());
            // 최대 12개까지만 이미지를 저장하도록 제한
            int maxImages = 12;
            if (images.size() > maxImages) {
                response.put("result", false);
                response.put("message", "이미지는 최대 12개 까지 첨부할 수 있습니다.");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

            List<String> imageLinks = amazonS3Service.uploadFiles(requestDTO.getImageFiles());  // 이미지 링크

            String linksAsString = String.join(",", imageLinks); // 이미지 링크를 문자열로 변환
            dto.setList(linksAsString); // 문자열로 변환한 이미지 링크를 DTO에 설정

            log.info("imageLinks : {}", imageLinks);
            int flag = productService.insertProduct(dto);

            if (flag == 0) {
                response.put("result", true);
            } else {
                response.put("result", false);
                response.put("message", "실패");
            }

        }
        return ResponseEntity.ok(response);
    }

    /* 상품 상세 */
    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable int id, ProductDTO dto, HttpSession session) {

        log.info("{}", id);
        Map<String, Object> response = new HashMap<>();

        if (session.getAttribute("dto") == null) {
            log.info("실패");
            response.put("result", false);
            response.put("message", "로그인 상태가 아닙니다.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED); // 401 Unauthorized 응답을 보냄
        }
        /* 상품 조회 서비스 호출 */
        UserDTO reqDto = (UserDTO) session.getAttribute("dto");
        log.info("session user : {}", session.getAttribute("dto"));
        dto.setUserId(reqDto.getId());

        // 상품안의 USER_ID 검색
        int userId = productService.getUserIdByProductSeq(id);

        log.info("로그인 중인 유저 Id : {}", reqDto.getId());
        log.info("상품 등록한 유저 Id : {}", userId);

        List<ProductDTO> productList = productService.getProductByIdWithLiked(id, reqDto);   // 상품 리스트 호출
        List<Map<String, Object>> responseList = new ArrayList<>();

        for (ProductDTO listDto : productList) {
            Map<String, Object> productMap = new LinkedHashMap<>();
            productMap.put("id", listDto.getId());
            productMap.put("title", listDto.getTitle());
            productMap.put("status", listDto.getStatus());
            productMap.put("body", listDto.getBody());
            productMap.put("date", listDto.getDate());
            productMap.put("price", listDto.getPrice());

            String linkAsString = listDto.getList();
            List<String> imageLinks = Arrays.asList(linkAsString.split(","));
            productMap.put("list", imageLinks);

            if (userId == reqDto.getId()) {
                response.put("message", "본인이 등록한 상품입니다.");
                productMap.put("onself", true);
                productMap.put("onlike", false);
            }

            if(userId != reqDto.getId()) {
                productMap.put("onself", false);
                productMap.put("onlike", listDto.isOnlike());
            }

            responseList.add(productMap);
        }
        return new ResponseEntity<>(responseList, HttpStatus.OK);
    }


    /* 상품 수정 */
    @PutMapping("/{id}/modify")
    public ResponseEntity<?> modifyProduct(@PathVariable int id, ProductRequestDTO requestDTO, HttpSession session) {
        log.info("{}", id);
        Map<String, Object> response = new HashMap<>();

        if (session.getAttribute("dto") == null) {
            log.info("실패");
            response.put("result", false);
            response.put("message", "로그인 상태가 아닙니다.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED); // 401 Unauthorized 응답을 보냄
        }
        /* 상품 조회 서비스 호출 */
        UserDTO reqDto = (UserDTO) session.getAttribute("dto");
        log.info("session user : {}", session.getAttribute("dto"));

        ProductDTO dto = new ProductDTO();

        dto.setId(id);
        dto.setTitle(requestDTO.getTitle());
        dto.setStatus(requestDTO.getStatus());
        dto.setBody(requestDTO.getBody());
        dto.setDate(requestDTO.getDate());
        dto.setPrice(requestDTO.getPrice());
        dto.setUserId(reqDto.getId());

        List<MultipartFile> images = requestDTO.getImageFiles();

        log.info("imageLinks.size : {}", images.size());
        // 최대 12개까지만 이미지를 저장하도록 제한
        int maxImages = 12;
        if (images.size() > maxImages) {
            response.put("result", false);
            response.put("message", "이미지는 최대 12개 까지 첨부할 수 있습니다.");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        productService.updateProductImages(id, images);

        /* List<String> imageLinks = amazonS3Service.uploadFiles(requestDTO.getImageFiles());  // 이미지 링크

        String linksAsString = String.join(",", imageLinks); // 이미지 링크를 문자열로 변환
        dto.setList(linksAsString); // 문자열로 변환한 이미지 링크를 DTO에 설정

        log.info("imageLinks : {}", imageLinks); */
        log.info("dto : {}", dto);

        // 상품 안의 userId 조회
        int userId = productService.getUserIdByProductSeq(id);

        log.info("로그인 중인 유저 Id : {}", reqDto.getId());
        log.info("상품 등록한 유저 Id : {}", userId);

        if (userId == reqDto.getId()) {
            int flag = productService.modifyProduct(dto);

            if (flag == 0) {
                response.put("result", true);
            } else {
                response.put("result", false);
                response.put("message", "상품 정보 수정에 실패했습니다.");
            }
        } else {
            response.put("message", "본인이 등록한 상품이 아닙니다.");
        }
        return new ResponseEntity<>(response, HttpStatus.OK);

    }

    /* 상품 찜하기 */
    @Transactional
    @PatchMapping("/{id}/like")
    public ResponseEntity<Object> updateLikeStatus(@PathVariable int id, @RequestBody LikeDTO dto, HttpSession session) {

        boolean onlike = dto.isOnlike();
        log.info("상품 id : {}", id);
        Map<String, Object> response = new HashMap<>();

        if(session.getAttribute("dto") == null) {
            log.info("실패");
            response.put("result", false);
            response.put("message", "로그인 상태가 아닙니다.");
        } else { // 세션이 있으면 여길 탈듯
            /* 상품 조회 서비스 호출 */
            UserDTO reqDto = (UserDTO) session.getAttribute("dto");
            log.info("session user : {}", session.getAttribute("dto"));
            int userId = reqDto.getId();
            log.info("user id : {}", reqDto.getId());

            // 상품안의 USER_ID 검색
            int idUser = productService.getUserIdByProductSeq(id);

            log.info("로그인 중인 유저 Id : {}", reqDto.getId());
            log.info("상품 등록한 유저 Id : {}", idUser);

            if (idUser == reqDto.getId()) {
                log.info("실패");
                response.put("result", false);
                response.put("message", "본인이 게시한 상품은 찜할 수 없습니다.");
            } else {
                int flag = productService.updateLikeStatus(userId, id, onlike);

                if(flag == 0) {
                    log.info("성공");
                    response.put("result", true);
                } else {
                    log.info("실패");
                    response.put("result", false);
                }
            }
        }
        return ResponseEntity.ok(response);
    }

    /* 상품 검색 */
    @GetMapping("/search/{search}")
    public ResponseEntity<?> getProductBySearch(@PathVariable String search) {

        int page = 1;
        int pageSize = 12;
        int count = productService.getSearchCount(search);
        log.info("테이블 내 컬럼 개수 ( count ) : {}", count);

        // 서비스에서 상품 목록을 가져옴
        List<ProductDTO> productList = productService.getProductBySearch(search, page);

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

    @DeleteMapping("/{id}/delete")
    public ResponseEntity<Object> deleteProduct(@PathVariable int id, HttpSession session) {

        log.info("상품 id : {}", id);
        Map<String, Object> response = new HashMap<>();

        if(session.getAttribute("dto") == null) {
            log.info("실패");
            response.put("result", false);
            response.put("message", "로그인 상태가 아닙니다.");
        } else { // 세션이 있으면 여길 탈듯
            /* 상품 조회 서비스 호출 */
            UserDTO reqDto = (UserDTO) session.getAttribute("dto");
            log.info("session user : {}", session.getAttribute("dto"));
            int userId = reqDto.getId();    // 세션에서 가져온 유저 id
            int idUser = productService.getUserIdByProductSeq(id);  // 상품 id를 이용해 상품 테이블에서 가져온 유저 id

            log.info("로그인 중인 유저 Id : {}", userId);
            log.info("상품 등록한 유저 Id : {}", idUser);

            if(userId == idUser) {
                int flag = productService.deleteProduct(id);

                if(flag == 0) {
                    log.info("성공");
                    response.put("result", true);
                } else {
                    log.info("실패");
                    response.put("result", false);
                    response.put("message", "모종의 이유로 삭제에 실패했습니다.");
                }
            } else {
                log.info("실패");
                response.put("result", false);
                response.put("message", "본인이 게시한 상품이 아닙니다.");
            }
        }
        return ResponseEntity.ok(response);
    }

    /* 댓글 조회 */
    @GetMapping("/{id}/comment")
    public ResponseEntity<List<ProductCommentDTO>> commentOnProduct(@PathVariable int id, HttpSession session) {
        log.info("상품 id : {}", id);
        Map<String, Object> response = new HashMap<>();

        if (session.getAttribute("dto") == null) {
            log.info("실패");
            response.put("result", false);
            response.put("message", "로그인 상태가 아닙니다.");
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); // 401 Unauthorized 응답을 보냄
        }

        UserDTO reqDto = (UserDTO) session.getAttribute("dto");
        List<ProductCommentDTO> reqDtoProduct = commentService.getProductComment(id);
        log.info("session user : {}", session.getAttribute("dto"));
        log.info("Get Product Comment : {}", reqDtoProduct);

        int userId = reqDto.getId();    // 세션에서 가져온 유저 id

        if (reqDtoProduct.isEmpty()) {
            // 댓글이 없을 경우 404 Not Found 응답을 보냄
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        for (ProductCommentDTO productComment : reqDtoProduct) {    // 검사
            int userIdByProductComment = productComment.getUserId(); // 댓글을 등록한 유저의 id를 가져옴.

            log.info("로그인 중인 유저 Id : {}", userId);
            log.info("댓글 등록한 유저 Id : {}", userIdByProductComment);

            if (userId == userIdByProductComment) {
                productComment.setOnself(true);
            } else {
                productComment.setOnself(false);
            }
        }
        // 댓글 리스트를 반환
        return new ResponseEntity<>(reqDtoProduct, HttpStatus.OK);
    }

    /* 댓글 작성 */
    @PostMapping("/{id}/comment/new")
    public ResponseEntity<?> commentProductWrite(@PathVariable int id, @RequestBody ProductCommentDTO dto, HttpSession session) {
        log.info("상품 id : {}", id);
        Map<String, Object> response = new HashMap<>();

        if(session.getAttribute("dto") == null) {
            log.info("실패");
            response.put("result", false);
            response.put("message", "로그인 상태가 아닙니다.");
        } else { // 세션이 있으면 여길 탈듯
            /* 상품 조회 서비스 호출 */
            UserDTO reqDto = (UserDTO) session.getAttribute("dto");
            log.info("session user : {}", session.getAttribute("dto"));
            log.info("get product : {}", productService.getProductByIdWithLiked(id, reqDto));
            int userId = reqDto.getId();    // 세션에서 가져온 유저 id
            int productSeq = id;  // 게시글 id를 이용해 게시글 테이블에서 가져온 유저 id

            dto.setUserId(userId);
            dto.setProductSeq(productSeq);

            int flag = commentService.writeProductComment(dto);

            if (flag == 0) {
                log.info("성공");
                response.put("result", true);
            } else {
                log.info("실패");
                response.put("result", false);
                response.put("message", "모종의 이유로 댓글 작성에 실패했습니다.");
            }
        }
        return ResponseEntity.ok(response);
    }

    /* 댓글 수정 */
    @PatchMapping("/{id}/comment/{commentId}/modify")
    public ResponseEntity<?> modifyProductComment(@PathVariable int id, @PathVariable int commentId, @RequestBody Map<String, Object> map,
                                                ProductCommentDTO dto, HttpSession session) {
        log.info("상품 id : {}", id);
        Map<String, Object> response = new HashMap<>();

        if(session.getAttribute("dto") == null) {
            log.info("실패");
            response.put("result", false);
            response.put("message", "로그인 상태가 아닙니다.");
        } else { // 세션이 있으면 여길 탈듯
            /* 상품 조회 서비스 호출 */
            UserDTO reqDto = (UserDTO) session.getAttribute("dto");
            log.info("session user : {}", session.getAttribute("dto"));
            log.info("get Product : {}", productService.getProductByIdWithLiked(id, reqDto));
            int userId = reqDto.getId();    // 세션에서 가져온 유저 id
            int productSeq = id;  // 게시글 id를 이용해 게시글 테이블에서 가져온 유저 id

            dto.setUserId(userId);
            dto.setProductSeq(productSeq);

            map.put("id", id);
            map.put("commentId", commentId);
            int flag = commentService.modifyProductComment(map);

            if (flag == 0) {
                log.info("성공");
                response.put("result", true);
            } else {
                log.info("실패");
                response.put("result", false);
                response.put("message", "모종의 이유로 댓글 수정에 실패했습니다.");
            }
        }
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}/comment/{commentId}/delete")
    public ResponseEntity<?> deleteProductComment(@PathVariable int id, @PathVariable int commentId,
                                                ProductCommentDTO dto, HttpSession session) {
        log.info("상품 id : {}", id);
        log.info("댓글 id : {}", commentId);
        Map<String, Object> response = new HashMap<>();

        if(session.getAttribute("dto") == null) {
            log.info("실패");
            response.put("result", false);
            response.put("message", "로그인 상태가 아닙니다.");
        } else { // 세션이 있으면 여길 탈듯
            /* 상품 조회 서비스 호출 */
            UserDTO reqDto = (UserDTO) session.getAttribute("dto");
            log.info("session user : {}", session.getAttribute("dto"));
            log.info("get product : {}", productService.getProductByIdWithLiked(id, reqDto));
            int userId = reqDto.getId();    // 세션에서 가져온 유저 id
            int productSeq = id;  // 게시글 id를 이용해 게시글 테이블에서 가져온 유저 id

            dto.setUserId(userId);
            dto.setProductSeq(productSeq);

            int flag = commentService.deleteProductComment(id, commentId);

            if (flag == 0) {
                log.info("성공");
                response.put("result", true);
            } else {
                log.info("실패");
                response.put("result", false);
                response.put("message", "모종의 이유로 댓글 삭제에 실패했습니다.");
            }
        }
        return ResponseEntity.ok(response);
    }
}

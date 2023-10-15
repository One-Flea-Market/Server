package com.server.controller;

import com.server.mapper.ProductMapper;
import com.server.model.*;
import com.server.response.MessageResBoard;
import com.server.response.MessageResProduct;
import com.server.service.CommentService;
import com.server.service.ProductService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ProductController {
    private final ProductService productService;
    private final CommentService commentService;
    private final SqlSession sqlSession;
    private final ProductMapper productMapper;

    /* 상품 메인 */
    @GetMapping("")
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

    /* 상품 등록 */
    @PostMapping("/registration")
    public ResponseEntity<?> registerProduct(@RequestBody ProductDTO dto, HttpSession session) {

        String sessionFlagYN = "N";
        boolean result;

        if (session.getAttribute("dto") == null) {
            sessionFlagYN = "N";
        } else { // 세션이 있으면 여길 탈듯
            /* 상품 등록 서비스 호출 */
            UserDTO reqDto = (UserDTO) session.getAttribute("dto");
            log.info("session user : {}", session.getAttribute("dto"));
            dto.setUserId(reqDto.getId());
            int flag = productService.insertProduct(dto);

            if (flag == 0) {
                result = true;
            } else {
                result = false;
            }
            /* 상품 등록 완료는 0이면 true 아니면 false */
            return new ResponseEntity<>(result, HttpStatus.OK);
        }
        /* 세션이 없으면 N */
        return new ResponseEntity<>(sessionFlagYN, HttpStatus.OK);
    }

    /* 상품 상세 */
    @GetMapping("/{id}")
    public ResponseEntity<MessageResProduct> getProductById(@PathVariable int id, ProductDTO dto, HttpSession session) {
        MessageResProduct messageResProduct = new MessageResProduct();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", Charset.forName("UTF-8")));

        log.info("{}", id);
        String sessionFlagYN = "N";

        if(session.getAttribute("dto") == null) {
            sessionFlagYN = "N";
        } else { // 세션이 있으면 여길 탈듯
            /* 상품 조회 서비스 호출 */
            UserDTO reqDto = (UserDTO) session.getAttribute("dto");
            log.info("session user : {}", session.getAttribute("dto"));
            dto.setUserId(reqDto.getId());

            // 상품안의 USER_ID 검색
            int userId = productService.getUserIdByProductSeq(id);

            log.info("로그인 중인 유저 Id : {}", reqDto.getId());
            log.info("상품 등록한 유저 Id : {}", userId);

            ProductDTO productDTO = new ProductDTO();

            messageResProduct.setProductList(productService.getProductById(id, reqDto));

            if (userId == reqDto.getId()) {
                productDTO.setOnself(true);
                productDTO.setOnlike(false);
                log.info("상품 등록한 유저 Id : {}", dto);
                messageResProduct.setMessage("본인이 등록한 상품입니다.");
            }

            if(userId != reqDto.getId()) {
                productDTO.setOnself(false);
                boolean alreadyLiked = productService.alreadyLiked(userId, dto.getProductSeq());
                if(alreadyLiked) {
                    productDTO.setOnlike(true);
                } else {
                    productDTO.setOnlike(false);
                }
            }
            /* 게시글 작성완료는 0이면 true 아니면 false */
            return new ResponseEntity<>(messageResProduct, headers, HttpStatus.OK);
        }
        /* 세션이 없으면 N */
        log.info("session : {}", sessionFlagYN);
        return new ResponseEntity<>(messageResProduct, headers, HttpStatus.OK);
    }

    /* 상품 수정 */
    @PutMapping("/{id}/modify")
    public ResponseEntity<MessageResProduct> modifyBoard(@PathVariable int id, @RequestBody Map<String, Object> map,
                                                       ProductDTO dto, HttpSession session) {
        MessageResProduct messageResProduct = new MessageResProduct();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", Charset.forName("UTF-8")));

        String sessionFlagYN = "N";

        if(session.getAttribute("dto") == null) {
            sessionFlagYN = "N";
        } else { // 세션이 있으면 여길 탈듯
            /* 상품 수정 서비스 호출 */
            UserDTO reqDto = (UserDTO) session.getAttribute("dto");
            log.info("session user : {}", session.getAttribute("dto"));
            dto.setUserId(reqDto.getId());

            // 상품안의 USER_ID 검색
            int userId = productService.getUserIdByProductSeq(id);

            if (userId == reqDto.getId()) {
                messageResProduct.setOnself(true);

                messageResProduct.setOnlike(false);

                map.put("id", id);
                int flag = productService.modifyProduct(map);
                //dto.setOnLike(0); // 0일때 onlike가 false
                if (flag == 0) {
                    messageResProduct.setResult(true);
                } else {
                    messageResProduct.setResult(false);
                    messageResProduct.setMessage("상품 정보 수정에 실패했습니다.");
                }
            } else {
                messageResProduct.setMessage("본인이 등록한 상품이 아닙니다.");
            }
            /* 상품 수정 완료는 0이면 true 아니면 false */
            return new ResponseEntity<>(messageResProduct, headers, HttpStatus.OK);
        }
        /* 세션이 없으면 N */
        log.info("session : {}", sessionFlagYN);
        return new ResponseEntity<>(messageResProduct, headers, HttpStatus.OK);
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
    public ResponseEntity<MessageResProduct> getProductBySearch(@PathVariable String search,
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

        List<ProductDTO> result = sqlSession.selectList("com.server.mapper.ProductMapper.getProductBySearch", map);

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
            log.info("get board : {}", productService.getProductById(id, reqDto));
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
            log.info("get Product : {}", productService.getProductById(id, reqDto));
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
    public ResponseEntity<?> deleteBoardComment(@PathVariable int id, @PathVariable int commentId,
                                                ProductCommentDTO dto, HttpSession session) {
        log.info("게시글 id : {}", id);
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
            log.info("get board : {}", productService.getProductById(id, reqDto));
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

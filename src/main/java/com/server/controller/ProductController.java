package com.server.controller;

import com.server.model.BoardDTO;
import com.server.model.ProductDTO;
import com.server.model.UserDTO;
import com.server.response.MessageResBoard;
import com.server.response.MessageResProduct;
import com.server.service.ProductService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ProductController {
    private final ProductService productService;

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
    /* onself, onlike 추가해야함 */
    @GetMapping("/{id}")
    public ResponseEntity<MessageResProduct> getProductById(@PathVariable int id, ProductDTO dto, HttpSession session) {
        MessageResProduct messageResProduct = new MessageResProduct();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", Charset.forName("UTF-8")));

        log.info("{}", id);
        String sessionFlagYN = "N";

        List<ProductDTO> list = productService.getProductById(id);

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

            if (userId == reqDto.getId()) {
                messageResProduct.setOnself(true);
                dto.setOnLike(0); // 0일때 onlike가 false
                messageResProduct.setOnlike(false);
            }

            if(userId != reqDto.getId()) {
                if(dto.getOnLike() == 1) {
                    messageResProduct.setOnlike(true);
                    messageResProduct.setOnself(false);
                }
            }

            messageResProduct.setProductList(productService.getProductById(id));

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
                dto.setOnLike(0); // 0일때 onlike가 false
                messageResProduct.setOnlike(false);

                map.put("id", id);
                int flag = productService.modifyProduct(map);

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

    @PatchMapping("{id}/like")
    public ResponseEntity<MessageResProduct> likeOnProduct(@PathVariable int id, ProductDTO dto, @RequestBody Map<String, Object> map, HttpSession session) {
        MessageResProduct messageResProduct = new MessageResProduct();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", Charset.forName("UTF-8")));

        log.info("상품 id : {}", id);
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

            if (userId == reqDto.getId()) {
                messageResProduct.setOnself(true);
                dto.setOnLike(0); // 0일때 onlike가 false
                messageResProduct.setOnlike(false);

                messageResProduct.setMessage("본인이 등록한 상품은 찜할 수 없습니다.");
            } else {
                map.put("id", id);
                int flag = productService.updateProductLike(map);

                if (flag == 0) {
                    messageResProduct.setResult(true);
                } else {
                    messageResProduct.setResult(false);
                    messageResProduct.setMessage("상품 찜하기 수정에 실패했습니다.");
                }
            }
            /* 상품 수정 완료는 0이면 true 아니면 false */
            return new ResponseEntity<>(messageResProduct, headers, HttpStatus.OK);
        }
        /* 세션이 없으면 N */
        log.info("session : {}", sessionFlagYN);
        return new ResponseEntity<>(messageResProduct, headers, HttpStatus.OK);
    }
}

package com.server.service;

import com.server.mapper.ProductMapper;
import com.server.model.LikeDTO;
import com.server.model.ProductDTO;
import com.server.model.UserDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductMapper productMapper;

    public int insertProduct(ProductDTO dto) {
        log.info("dto : {}", dto);

        int flag = 0;
        try {
            flag = 1;
            int result = productMapper.insertProduct(dto);

            Map<String, Object> imageMap = new HashMap<>();
            imageMap.put("id", dto.getId());
            imageMap.put("list", dto.getList());
            //productMapper.insertProductImages(imageMap);

            if (result >= 1) {
                flag = 0;
                // 이미지 링크 저장
            } else {
                flag = 1;
            }
            return flag;

        } catch (Exception e) {
            e.printStackTrace();
            return flag;
        }
    }

    public List<ProductDTO> getAllProduct() {
        log.info("{}", productMapper.getAllProduct());
        return productMapper.getAllProduct();
    }

    public List<ProductDTO> getProduct(int page) {
        int offset = (page - 1) * 12;
        log.info("offset : {}", offset);
        return productMapper.getProduct(offset);
    }

    public List<ProductDTO> getTransaction(int page) {
        int offset = (page - 1) * 12;
        log.info("offset : {}", offset);
        return productMapper.getTransaction(offset);
    }

    public List<ProductDTO> getRental(int page) {
        int offset = (page - 1) * 12;
        log.info("offset : {}", offset);
        return productMapper.getRental(offset);
    }

    public List<ProductDTO> getProductBySearch(String search, int page) {
        int offset = (page - 1) * 12;
        log.info("offset : {}", offset);
        search = '%'+search+'%';
        return productMapper.getProductBySearch(search, offset);
    }

    public List<ProductDTO> getTransactionBySearch(String search, int page) {
        int offset = (page - 1) * 12;
        log.info("offset : {}", offset);
        search = '%'+search+'%';
        return productMapper.getTransactionBySearch(search, offset);
    }

    public List<ProductDTO> getRentalBySearch(String search, int page) {
        int offset = (page - 1) * 12;
        log.info("offset : {}", offset);
        search = '%'+search+'%';
        return productMapper.getRentalBySearch(search, offset);
    }

    public int getProductCount() { return productMapper.getProductCount();}

    public int getTransactionCount() { return productMapper.getTransactionCount();}

    public int getRentalCount() { return productMapper.getRentalCount();}

    public int getSearchCount(String search) {
        search = '%'+search+'%';
        return productMapper.getSearchCount(search);
    }

    public int getUserIdByProductSeq(int id) {
        return productMapper.getUserIdByProductSeq(id);
    }

    public List<ProductDTO> getProductById(int id) {
        return productMapper.getProductById(id);
    }

    // 상품 상세
    public List<ProductDTO> getProductByIdWithLiked(int id, UserDTO reqDto) {
        List<ProductDTO> productList = productMapper.getProductById(id);
        int userId = reqDto.getId(); // 세션 내의 유저 id
        for (ProductDTO product : productList) {    // 검사
            int userIdByProduct = product.getUserId(); // 상품을 등록한 유저의 id를 가져옴.
            if (userIdByProduct == reqDto.getId()) {    // 세션 내의 유저 id와 상품 내의 유저 id가 같다면 여기로 들어갈 것.
                product.setOnself(true);    // 세션 내의 유저가 게시한 상품을 나타냄.
                product.setOnlike(false);   // 자신이 게시한 상품이기에 찜할 수 없음.
            } else {
                product.setOnself(false);
                boolean alreadyLiked = alreadyLiked(userId, product.getId());   // 레코드가 있는지 검사.
                log.info("product.getId() = {}", product.getId());
                log.info("userId = {}", userId);
                log.info("alreadyLiked = {}", alreadyLiked);
                if(alreadyLiked) {
                    log.info("service alreadyLiked : {}", productMapper.getLikedByUser(userId, product.getId()));
                    product.setOnlike(productMapper.getLikedByUser(userId, product.getId()));
                    log.info("service alreadyLiked after : {}", product.isOnlike());
                } else {
                    product.setOnlike(false);
                }
            }
        }
        log.info("productList in service : {}", productList);
        return productList;
    }

    /* 상품 수정 */
    public int modifyProduct(Map<String, Object> map) {

        int flag = 1;
        int result = productMapper.modifyProduct(map);

        if(result >= 1) {
            flag = 0;
        } else {
            flag = 1;
        }
        return flag;
    }

    /* 찜하기 테이블 안에 유저 고유 id와 상품 고유 id가 들어있는 레코드가 있는지 검사 */
    public boolean alreadyLiked(int userId, int productSeq) {
        return productMapper.isAlreadyLiked(userId, productSeq);
    }

    /* 찜하기 기능 */
    @Transactional
    public int updateLikeStatus(int userId, int productSeq, boolean onlike) {
        boolean alreadyLiked = alreadyLiked(userId, productSeq);

        int flag = 1;

        if(alreadyLiked) {  // 레코드가 있는지 검사 후에 있다면 이쪽으로 들어갈 것.
            Map<String, Object> params = new HashMap<>();
            params.put("userId", userId);
            params.put("id", productSeq);
            params.put("onlike", onlike);

            log.info("{} / {} / {}", userId, productSeq, onlike);
            log.info("{}", params);

            int result = productMapper.updateLikeStatus(params);

            log.info("result : {}", result);

            if(result >= 1) {
                flag = 0;
            } else {
                flag = 1;
            }
            return flag;
        } else {    // 레코드가 있는지 검사 후에 없다면 객체를 생성해 새로운 찜 정보 insert.
            LikeDTO newLikeDTO = new LikeDTO();
            newLikeDTO.setUserId(userId);
            newLikeDTO.setProductSeq(productSeq);
            newLikeDTO.setOnlike(onlike);

            int result = productMapper.insertLikeStatus(newLikeDTO);

            if(result >= 1) {
                flag = 0;
            } else {
                flag = 1;
            }
            return flag;
        }
    }

    /* 상품 삭제 */
    public int deleteProduct(int id) {
        int flag = 1;
        int result = productMapper.deleteProduct(id);

        if(result >= 1) {
            flag = 0;
        } else {
            flag = 1;
        }
        return flag;
    }
}

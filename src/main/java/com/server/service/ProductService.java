package com.server.service;

import com.server.mapper.ProductMapper;
import com.server.model.LikeDTO;
import com.server.model.ProductDTO;
import com.server.model.UserDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductMapper productMapper;

    private final AmazonS3Service amazonS3Service;

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

    public List<ProductDTO> getHomeProduct(){
        return productMapper.getHomeProduct();
    }

    public List<ProductDTO> getProductWithOffset(int offset) {
        log.info("offset : {}", offset);
        return productMapper.getProductWithOffset(offset);
    }

    public List<ProductDTO> getTransaction(int offset) {
        log.info("offset : {}", offset);
        return productMapper.getTransaction(offset);
    }

    public List<ProductDTO> getRental(int offset) {
        log.info("offset : {}", offset);
        return productMapper.getRental(offset);
    }

    public List<ProductDTO> getProductBySearch(String search, int offset) {
        log.info("offset : {}", offset);
        search = '%'+search+'%';
        return productMapper.getProductBySearch(search, offset);
    }

    public List<ProductDTO> getTransactionBySearch(String search, int offset) {
        log.info("offset : {}", offset);
        search = '%'+search+'%';
        return productMapper.getTransactionBySearch(search, offset);
    }

    public List<ProductDTO> getRentalBySearch(String search, int offset) {
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
    public int modifyProduct(ProductDTO dto) {

        int flag = 1;
        int result = productMapper.modifyProduct(dto);

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

    public void updateProductImages(int productId, List<MultipartFile> newImages) {
        // 기존 상품 이미지 URL 가져오기 (예시로 getProductImageUrlsFromDatabase 메서드 사용)
        List<String> existingImageUrls = getProductImageUrlsFromDatabase(productId);

        // 새로운 이미지 URL 가져오기
        List<String> newImageUrls = amazonS3Service.uploadFiles(newImages);

        // 기존 이미지 중에서 새로운 이미지와 일치하지 않는 것들을 삭제
        List<String> imagesToDelete = existingImageUrls.stream()
                .filter(url -> !newImageUrls.contains(url))
                .collect(Collectors.toList());

        // S3에서 이미지 삭제
        amazonS3Service.deleteFiles(imagesToDelete);

        // 새로운 이미지 URL을 상품에 업데이트
        saveNewImageUrlsToDatabase(productId, newImageUrls);
    }

    // 기존 상품 이미지 URL을 데이터베이스에서 가져오는 메서드 (실제 데이터베이스 조회 로직을 추가해주세요)
    public List<String> getProductImageUrlsFromDatabase(int productId) {
        // 데이터베이스에서 productId에 해당하는 이미지 URL 조회 로직을 구현
        // 예시로 더미 데이터를 반환합니다.
        String imageUrlString = productMapper.getImageUrl(productId);
        log.info("Arrays.asList : {}", Arrays.asList(imageUrlString.split(",")));
        return Arrays.asList(imageUrlString.split(","));
    }

    // 새로운 이미지 URL을 상품에 업데이트하는 메서드 (실제 데이터베이스 업데이트 로직을 추가해주세요)
    public int saveNewImageUrlsToDatabase(int productId, List<String> newImageUrls) {
        // 데이터베이스에 productId에 해당하는 상품의 이미지 URL을 업데이트하는 로직을 구현
        // 예시로 더미 데이터를 업데이트합니다.
        String imageUrlString = String.join(",", newImageUrls);
        log.info("Updating product images in the database: ProductId={}, NewImageUrls={}", productId, imageUrlString);
        // 여기에 실제 데이터베이스 업데이트 로직을 추가하세요.
        Map<String, Object> map = new HashMap<>();
        map.put("list", imageUrlString);
        map.put("id", productId);
        int result = productMapper.modifyImage(map);
        int flag = 1;

        if(result >= 1) {
            flag = 0;
        } else {
            flag = 1;
        }
        return flag;
    }
}

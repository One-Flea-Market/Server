package com.server.mapper;

import com.server.model.LikeDTO;
import com.server.model.ProductDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.security.core.parameters.P;

import java.util.List;
import java.util.Map;

@Mapper
public interface ProductMapper {

    int insertProduct(ProductDTO dto);
    int getProductCount();
    int getTransactionCount();
    int getRentalCount();
    int getSearchCount(@Param("search") String search);
    List<ProductDTO> getProduct(@Param("offset") int offset);
    List<ProductDTO> getAllProduct();
    List<ProductDTO> getProductById(int id);
    int getUserIdByProductSeq(int id);
    int modifyProduct(ProductDTO dto);
    int modifyImage(Map<String, Object> map);
    List<ProductDTO> getProductBySearch(@Param("search") String search, @Param("offset") int offset);
    List<ProductDTO> getTransactionBySearch(@Param("search") String search, @Param("offset") int offset);
    List<ProductDTO> getRentalBySearch(@Param("search") String search, @Param("offset") int offset);
    List<ProductDTO> getTransaction(@Param("offset") int offset);
    List<ProductDTO> getRental(@Param("offset") int offset);
    boolean isAlreadyLiked(@Param("userId") int userId, @Param("productSeq") int productSeq);
    Boolean getLikedByUser(@Param("userId") int userId, @Param("productSeq") int productSeq);
    int updateLikeStatus(Map<String, Object> params);
    int insertLikeStatus(LikeDTO dto);
    int deleteProduct(int id);
    int deleteCart(@Param("userId") int userId, @Param("productSeq") int productSeq);
    String getImageUrl(int id);
}

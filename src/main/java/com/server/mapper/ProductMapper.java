package com.server.mapper;

import com.server.model.LikeDTO;
import com.server.model.ProductDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface ProductMapper {

    int insertProduct(ProductDTO dto);
    int getProductCount();
    int getTransactionCount();
    int getRentalCount();
    List<ProductDTO> getProduct(@Param("offset") int offset, @Param("pageSize") int pageSize);
    List<ProductDTO> getProductById(int id);
    int getUserIdByProductSeq(int id);
    int modifyProduct(Map<String, Object> map);
    List<ProductDTO> getTransaction(@Param("offset") int offset, @Param("pageSize") int pageSize);
    List<ProductDTO> getRental(@Param("offset") int offset, @Param("pageSize") int pageSize);
    boolean isAlreadyLiked(@Param("userId") int userId, @Param("productSeq") int productSeq);
    boolean getLikedByUser(@Param("userId") int userId, @Param("productSeq") int productSeq);
    int updateLikeStatus(Map<String, Object> params);
    int insertLikeStatus(LikeDTO dto);
    int deleteProduct(int id);
}

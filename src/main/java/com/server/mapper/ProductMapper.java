package com.server.mapper;

import com.server.model.ProductDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface ProductMapper {

    int insertProduct(ProductDTO dto);
    List<ProductDTO> getProductById(int id);
    int getUserIdByProductSeq(int id);
    int modifyProduct(Map<String, Object> map);
    int updateProductLike(Map<String, Object> map);
}

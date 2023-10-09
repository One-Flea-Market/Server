package com.server.service;

import com.server.mapper.ProductMapper;
import com.server.model.ProductDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductMapper productMapper;

    public int insertProduct(ProductDTO dto) {
        log.info("dto : {}", dto);

        int flag = 1;
        int result = productMapper.insertProduct(dto);

        if(result >= 1) {
            flag = 0;
        } else {
            flag = 1;
        }
        return flag;
    }

    public int getUserIdByProductSeq(int id) {
        return productMapper.getUserIdByProductSeq(id);
    }

    public List<ProductDTO> getProductById(int id) {
        return productMapper.getProductById(id);
    }

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

    public int updateProductLike(Map<String, Object> map) {

        int flag = 1;
        int result = productMapper.updateProductLike(map);

        if(result >= 1) {
            flag = 0;
        } else {
            flag = 1;
        }
        return flag;
    }
}

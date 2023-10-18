package com.server.mapper;

import com.server.model.ProductDTO;
import com.server.model.UserDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserMapper {

    /* User 객체 하나 찾기 (조건 strEmail) */
    UserDTO selectOneUser(UserDTO dto);
    String selectEncPwd(UserDTO dto);
    /* 회원가입 */
    int joinUser(UserDTO dto);
    /* 이메일 중복 체크 */
    String emailCheck(String email);
    /* 닉네임 중복 체크 */
    String nameCheck(String name);
    List<ProductDTO> getMyProduct(int id);
    List<ProductDTO> getMyLikedProducts();
}

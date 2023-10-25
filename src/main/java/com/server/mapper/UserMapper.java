package com.server.mapper;

import com.server.model.ProductDTO;
import com.server.model.UserDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Mapper
public interface UserMapper {

    /* User 객체 하나 찾기 (조건 strEmail) */
    UserDTO selectOneUser(UserDTO dto);
    String selectEncPwd(UserDTO dto);
    /* 회원가입 */
    int joinUser(UserDTO dto);
    /* 이메일 중복 체크 */
    String emailCheck(@Param("email") String email);
    /* 닉네임 중복 체크 */
    String nameCheck(@Param("username") String username);
    List<ProductDTO> getMyProduct(int id);
    List<ProductDTO> getMyLikedProducts();
}

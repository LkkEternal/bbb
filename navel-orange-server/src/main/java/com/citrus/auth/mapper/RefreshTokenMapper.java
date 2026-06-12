package com.citrus.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.citrus.auth.entity.RefreshToken;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * Refresh Token Mapper
 */
@Mapper
public interface RefreshTokenMapper extends BaseMapper<RefreshToken> {

    /**
     * 根据tokenId查询
     */
    @Select("SELECT * FROM sys_refresh_token WHERE token_id = #{tokenId}")
    RefreshToken selectByTokenId(@Param("tokenId") String tokenId);
}

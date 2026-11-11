package com.emotion.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.emotion.user.entity.RefreshToken;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

@Mapper
public interface RefreshTokenMapper extends BaseMapper<RefreshToken> {

    @Select("SELECT * FROM sys_refresh_token WHERE token_id = #{tokenId} AND revoked = 0 AND expires_at > #{now} LIMIT 1")
    RefreshToken selectValidByTokenId(@Param("tokenId") String tokenId, @Param("now") LocalDateTime now);

    @Update("UPDATE sys_refresh_token SET revoked = 1 WHERE user_id = #{userId} AND revoked = 0")
    int revokeAllByUserId(@Param("userId") Long userId);
}

package com.example.housetrading.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.housetrading.entity.PasswordResetToken;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PasswordResetTokenMapper extends BaseMapper<PasswordResetToken> {
}

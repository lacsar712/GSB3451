package com.example.housetrading.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.housetrading.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}

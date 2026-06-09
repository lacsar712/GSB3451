package com.example.housetrading.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.housetrading.entity.House;
import com.example.housetrading.mapper.HouseMapper;
import org.springframework.stereotype.Service;

@Service
public class HouseService extends ServiceImpl<HouseMapper, House> {
}

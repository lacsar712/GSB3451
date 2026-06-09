package com.example.housetrading.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.housetrading.entity.Review;
import com.example.housetrading.mapper.ReviewMapper;
import org.springframework.stereotype.Service;

@Service
public class ReviewService extends ServiceImpl<ReviewMapper, Review> {
}

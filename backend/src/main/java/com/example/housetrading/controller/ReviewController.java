package com.example.housetrading.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.housetrading.common.Result;
import com.example.housetrading.entity.Review;
import com.example.housetrading.entity.User;
import com.example.housetrading.service.ReviewService;
import com.example.housetrading.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private UserService userService;

    @PostMapping("/submit")
    public Result<Void> submit(@RequestBody Review review) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getByUsername(username);
        review.setUserId(user.getId());
        reviewService.save(review);
        return Result.success();
    }

    @GetMapping("/public/house/{houseId}")
    public Result<List<Map<String, Object>>> getHouseReviews(@PathVariable Integer houseId) {
        List<Review> reviews = reviewService.list(new LambdaQueryWrapper<Review>()
                .eq(Review::getHouseId, houseId)
                .orderByDesc(Review::getCreateTime));
        
        // Enhance reviews with user information (username, avatar)
        List<Map<String, Object>> enhancedReviews = reviews.stream().map(review -> {
            User user = userService.getById(review.getUserId());
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", review.getId());
            map.put("content", review.getContent());
            map.put("rating", review.getRating());
            map.put("createTime", review.getCreateTime());
            map.put("username", user != null ? user.getUsername() : "未知用户");
            map.put("avatar", user != null ? user.getAvatar() : null);
            return map;
        }).collect(Collectors.toList());
        
        return Result.success(enhancedReviews);
    }
}

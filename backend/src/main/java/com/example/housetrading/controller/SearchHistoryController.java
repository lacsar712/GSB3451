package com.example.housetrading.controller;

import com.example.housetrading.common.Result;
import com.example.housetrading.entity.SearchHistory;
import com.example.housetrading.entity.User;
import com.example.housetrading.service.SearchHistoryService;
import com.example.housetrading.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/search-history")
public class SearchHistoryController {

    @Autowired
    private SearchHistoryService searchHistoryService;

    @Autowired
    private UserService userService;

    @PostMapping("/user/add")
    public Result<Void> add(@RequestBody Map<String, String> body) {
        String keyword = body.get("keyword");
        if (keyword == null || keyword.trim().isEmpty()) {
            return Result.error("搜索关键词不能为空");
        }
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getByUsername(username);
        searchHistoryService.addSearchRecord(user.getId(), keyword.trim());
        return Result.success();
    }

    @GetMapping("/user/my")
    public Result<List<SearchHistory>> my() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getByUsername(username);
        return Result.success(searchHistoryService.getRecentHistory(user.getId()));
    }

    @DeleteMapping("/user/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getByUsername(username);
        searchHistoryService.deleteByIdAndUserId(id, user.getId());
        return Result.success();
    }

    @DeleteMapping("/user/clear")
    public Result<Void> clear() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getByUsername(username);
        searchHistoryService.clearByUserId(user.getId());
        return Result.success();
    }
}

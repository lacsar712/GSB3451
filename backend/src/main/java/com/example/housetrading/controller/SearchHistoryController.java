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

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.getByUsername(username);
    }

    @PostMapping
    public Result<Void> add(@RequestBody Map<String, String> body) {
        String keyword = body.get("keyword");
        if (keyword == null || keyword.trim().isEmpty()) {
            return Result.error(400, "搜索关键词不能为空");
        }
        User user = getCurrentUser();
        searchHistoryService.addSearchHistory(user.getId(), keyword.trim());
        return Result.success();
    }

    @GetMapping
    public Result<List<SearchHistory>> list() {
        User user = getCurrentUser();
        List<SearchHistory> history = searchHistoryService.getSearchHistory(user.getId());
        return Result.success(history);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        User user = getCurrentUser();
        searchHistoryService.deleteSearchHistory(user.getId(), id);
        return Result.success();
    }

    @DeleteMapping
    public Result<Void> clear() {
        User user = getCurrentUser();
        searchHistoryService.clearSearchHistory(user.getId());
        return Result.success();
    }
}

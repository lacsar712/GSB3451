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

    private User currentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.getByUsername(username);
    }

    @GetMapping("/user/list")
    public Result<List<SearchHistory>> list() {
        User user = currentUser();
        return Result.success(searchHistoryService.listByUser(user.getId()));
    }

    @PostMapping("/user/add")
    public Result<Void> add(@RequestBody Map<String, String> body) {
        String keyword = body.get("keyword");
        if (keyword == null || keyword.trim().isEmpty()) {
            return Result.error("搜索关键词不能为空");
        }
        User user = currentUser();
        searchHistoryService.addHistory(user.getId(), keyword);
        return Result.success();
    }

    @DeleteMapping("/user/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        User user = currentUser();
        searchHistoryService.deleteByIdAndUser(id, user.getId());
        return Result.success();
    }

    @DeleteMapping("/user/clear")
    public Result<Void> clear() {
        User user = currentUser();
        searchHistoryService.clearByUser(user.getId());
        return Result.success();
    }
}

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

@RestController
@RequestMapping("/search-history")
public class SearchHistoryController {

    @Autowired
    private SearchHistoryService searchHistoryService;

    @Autowired
    private UserService userService;

    @PostMapping("/add")
    public Result<Void> addSearchHistory(@RequestParam String keyword) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getByUsername(username);
        searchHistoryService.addSearchHistory(user.getId(), keyword);
        return Result.success();
    }

    @GetMapping("/list")
    public Result<List<SearchHistory>> getSearchHistory() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getByUsername(username);
        List<SearchHistory> list = searchHistoryService.getUserSearchHistory(user.getId());
        return Result.success(list);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteSearchHistory(@PathVariable Integer id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getByUsername(username);
        searchHistoryService.deleteSearchHistory(user.getId(), id);
        return Result.success();
    }

    @DeleteMapping("/clear")
    public Result<Void> clearAllSearchHistory() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getByUsername(username);
        searchHistoryService.clearAllSearchHistory(user.getId());
        return Result.success();
    }
}

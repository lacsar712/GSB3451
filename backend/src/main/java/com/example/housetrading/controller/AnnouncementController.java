package com.example.housetrading.controller;

import com.example.housetrading.common.Result;
import com.example.housetrading.entity.Announcement;
import com.example.housetrading.entity.User;
import com.example.housetrading.service.AnnouncementService;
import com.example.housetrading.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/announcements")
public class AnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    @Autowired
    private UserService userService;

    @GetMapping("/public/list")
    public Result<List<Announcement>> list() {
        return Result.success(announcementService.list());
    }

    @PostMapping("/admin/add")
    public Result<Void> add(@RequestBody Announcement announcement) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getByUsername(username);
        announcement.setAuthorId(user.getId());
        announcementService.save(announcement);
        return Result.success();
    }

    @DeleteMapping("/admin/delete/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        announcementService.removeById(id);
        return Result.success();
    }
}

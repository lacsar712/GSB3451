package com.example.housetrading.controller;

import com.example.housetrading.common.Result;
import com.example.housetrading.entity.User;
import com.example.housetrading.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/admin/all")
    public Result<List<User>> listAll() {
        return Result.success(userService.list());
    }

    @PutMapping("/admin/status/{id}/{status}")
    public Result<Void> updateStatus(@PathVariable Integer id, @PathVariable("status") Integer newStatus) {
        User user = userService.getById(id);
        if (user != null) {
            user.setStatus(newStatus);
            userService.updateById(user);
        }
        return Result.success();
    }

    @DeleteMapping("/admin/delete/{id}")
    public Result<Void> deleteUser(@PathVariable Integer id) {
        userService.removeById(id);
        return Result.success();
    }
}

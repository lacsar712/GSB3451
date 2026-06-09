package com.example.housetrading.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.housetrading.common.Result;
import com.example.housetrading.entity.House;
import com.example.housetrading.entity.User;
import com.example.housetrading.service.HouseService;
import com.example.housetrading.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/houses")
public class HouseController {

    @Autowired
    private HouseService houseService;

    @Autowired
    private UserService userService;

    @GetMapping("/public/list")
    public Result<List<House>> listAll(@RequestParam(required = false, defaultValue = "all") String type) {
        LambdaQueryWrapper<House> wrapper = new LambdaQueryWrapper<House>().eq(House::getStatus, "ACTIVE");
        if ("recent".equals(type)) {
            wrapper.orderByDesc(House::getCreateTime);
        } else if ("hot".equals(type)) {
            // For demo, sort by price desc as a proxy for "hot" or just order by ID desc
            wrapper.orderByDesc(House::getId);
        }
        return Result.success(houseService.list(wrapper));
    }

    @GetMapping("/public/{id}")
    public Result<House> getById(@PathVariable Integer id) {
        return Result.success(houseService.getById(id));
    }

    @PostMapping("/landlord/add")
    public Result<Void> add(@RequestBody House house) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getByUsername(username);
        house.setLandlordId(user.getId());
        house.setStatus("PENDING");
        houseService.save(house);
        return Result.success();
    }

    @PutMapping("/landlord/update")
    public Result<Void> update(@RequestBody House house) {
        houseService.updateById(house);
        return Result.success();
    }

    @DeleteMapping("/landlord/delete/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        houseService.removeById(id);
        return Result.success();
    }
    
    @GetMapping("/landlord/my")
    public Result<List<House>> myHouses() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getByUsername(username);
        return Result.success(houseService.list(new LambdaQueryWrapper<House>().eq(House::getLandlordId, user.getId())));
    }

    @GetMapping("/admin/all")
    public Result<List<House>> adminAll() {
        return Result.success(houseService.list());
    }

    @PostMapping("/admin/approve/{id}")
    public Result<Void> approve(@PathVariable Integer id) {
        House house = houseService.getById(id);
        house.setStatus("ACTIVE");
        house.setRejectionReason(null);
        houseService.updateById(house);
        return Result.success();
    }

    @PostMapping("/admin/reject/{id}")
    public Result<Void> reject(@PathVariable Integer id, @RequestBody String reason) {
        House house = houseService.getById(id);
        house.setStatus("REJECTED");
        house.setRejectionReason(reason);
        houseService.updateById(house);
        return Result.success();
    }
}

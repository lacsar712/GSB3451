package com.example.housetrading.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.housetrading.common.Result;
import com.example.housetrading.entity.HouseCollection;
import com.example.housetrading.entity.User;
import com.example.housetrading.mapper.HouseCollectionMapper;
import com.example.housetrading.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/collections")
public class HouseCollectionController {

    @Autowired
    private HouseCollectionMapper houseCollectionMapper;

    @Autowired
    private UserService userService;

    @PostMapping("/user/toggle/{houseId}")
    public Result<Void> toggle(@PathVariable Integer houseId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getByUsername(username);
        HouseCollection existing = houseCollectionMapper.selectOne(new LambdaQueryWrapper<HouseCollection>()
                .eq(HouseCollection::getUserId, user.getId())
                .eq(HouseCollection::getHouseId, houseId));
        if (existing != null) {
            houseCollectionMapper.deleteById(existing.getId());
        } else {
            HouseCollection c = new HouseCollection();
            c.setUserId(user.getId());
            c.setHouseId(houseId);
            houseCollectionMapper.insert(c);
        }
        return Result.success();
    }

    @Autowired
    private com.example.housetrading.service.HouseService houseService;

    @GetMapping("/user/my")
    public Result<List<HouseCollection>> my() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getByUsername(username);
        List<HouseCollection> collections = houseCollectionMapper.selectList(new LambdaQueryWrapper<HouseCollection>().eq(HouseCollection::getUserId, user.getId()));
        for (HouseCollection c : collections) {
            c.setHouse(houseService.getById(c.getHouseId()));
        }
        return Result.success(collections);
    }
}

package com.example.housetrading.controller;

import com.example.housetrading.common.Result;
import com.example.housetrading.entity.Consultation;
import com.example.housetrading.entity.User;
import com.example.housetrading.mapper.ConsultationMapper;
import com.example.housetrading.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/consultations")
public class ConsultationController {

    @Autowired
    private ConsultationMapper consultationMapper;

    @Autowired
    private UserService userService;

    @PostMapping("/user/submit")
    public Result<Void> submit(@RequestBody Consultation consultation) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getByUsername(username);
        consultation.setUserId(user.getId());
        consultationMapper.insert(consultation);
        return Result.success();
    }
}

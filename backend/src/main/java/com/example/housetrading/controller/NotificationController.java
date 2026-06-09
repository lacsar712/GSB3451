package com.example.housetrading.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.housetrading.common.Result;
import com.example.housetrading.entity.Announcement;
import com.example.housetrading.entity.Appointment;
import com.example.housetrading.entity.House;
import com.example.housetrading.entity.User;
import com.example.housetrading.service.AnnouncementService;
import com.example.housetrading.service.AppointmentService;
import com.example.housetrading.service.HouseService;
import com.example.housetrading.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private HouseService houseService;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private AnnouncementService announcementService;

    @Autowired
    private UserService userService;

    @GetMapping("/count")
    public Result<Long> getCount() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getByUsername(username);
        if (user == null) {
            return Result.success(0L);
        }

        long count = 0;
        if ("ADMIN".equals(user.getRole())) {
            // Pending house audits
            count = houseService.count(new LambdaQueryWrapper<House>().eq(House::getStatus, "PENDING"));
        } else if ("LANDLORD".equals(user.getRole())) {
            // Pending appointment requests for their houses
            count = appointmentService.count(new LambdaQueryWrapper<Appointment>()
                    .eq(Appointment::getLandlordId, user.getId())
                    .eq(Appointment::getStatus, "PENDING"));
        } else {
            // New announcements in the last 7 days for normal users
            count = announcementService.count(new LambdaQueryWrapper<Announcement>()
                    .gt(Announcement::getCreateTime, LocalDateTime.now().minusDays(7)));
        }

        return Result.success(count);
    }
}

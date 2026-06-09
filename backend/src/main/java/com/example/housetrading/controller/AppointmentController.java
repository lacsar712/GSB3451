package com.example.housetrading.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.housetrading.common.Result;
import com.example.housetrading.entity.Appointment;
import com.example.housetrading.entity.User;
import com.example.housetrading.service.AppointmentService;
import com.example.housetrading.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private UserService userService;

    @Autowired
    private com.example.housetrading.service.HouseService houseService;

    @PostMapping("/user/book")
    public Result<Void> book(@RequestBody Appointment appointment) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getByUsername(username);
        appointment.setUserId(user.getId());
        
        // Fetch landlord information
        com.example.housetrading.entity.House house = houseService.getById(appointment.getHouseId());
        if (house != null) {
            appointment.setLandlordId(house.getLandlordId());
        }
        
        appointment.setStatus("PENDING");
        appointmentService.save(appointment);
        return Result.success();
    }

    @GetMapping("/user/my")
    public Result<List<java.util.Map<String, Object>>> myAppointments() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getByUsername(username);
        List<Appointment> list = appointmentService.list(new LambdaQueryWrapper<Appointment>().eq(Appointment::getUserId, user.getId()));
        return Result.success(enhanceAppointments(list));
    }

    @GetMapping("/landlord/my")
    public Result<List<java.util.Map<String, Object>>> myLandlordAppointments() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getByUsername(username);
        List<Appointment> list = appointmentService.list(new LambdaQueryWrapper<Appointment>()
                .eq(Appointment::getLandlordId, user.getId()));
        return Result.success(enhanceAppointments(list));
    }

    private List<java.util.Map<String, Object>> enhanceAppointments(List<Appointment> list) {
        return list.stream().map(app -> {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", app.getId());
            map.put("userId", app.getUserId());
            map.put("houseId", app.getHouseId());
            map.put("appointmentTime", app.getAppointmentTime());
            map.put("landlordId", app.getLandlordId());
            map.put("status", app.getStatus());
            map.put("remark", app.getRemark());
            map.put("createTime", app.getCreateTime());
            
            com.example.housetrading.entity.House house = houseService.getById(app.getHouseId());
            map.put("houseTitle", house != null ? house.getTitle() : "未知房源");
            return map;
        }).collect(java.util.stream.Collectors.toList());
    }

    @GetMapping("/landlord/house/{houseId}")
    public Result<List<Appointment>> houseAppointments(@PathVariable Integer houseId) {
        return Result.success(appointmentService.list(new LambdaQueryWrapper<Appointment>().eq(Appointment::getHouseId, houseId)));
    }

    @PostMapping("/landlord/approve/{id}")
    public Result<Void> approve(@PathVariable Integer id) {
        Appointment appointment = appointmentService.getById(id);
        appointment.setStatus("APPROVED");
        appointmentService.updateById(appointment);
        return Result.success();
    }

    @PostMapping("/landlord/reject/{id}")
    public Result<Void> reject(@PathVariable Integer id) {
        Appointment appointment = appointmentService.getById(id);
        appointment.setStatus("REJECTED");
        appointmentService.updateById(appointment);
        return Result.success();
    }
}

package com.example.housetrading.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.housetrading.entity.Appointment;
import com.example.housetrading.mapper.AppointmentMapper;
import org.springframework.stereotype.Service;

@Service
public class AppointmentService extends ServiceImpl<AppointmentMapper, Appointment> {
}

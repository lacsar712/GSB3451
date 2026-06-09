package com.example.housetrading.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.housetrading.entity.Payment;
import com.example.housetrading.mapper.PaymentMapper;
import org.springframework.stereotype.Service;

@Service
public class PaymentService extends ServiceImpl<PaymentMapper, Payment> {
}

package com.example.housetrading.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.housetrading.common.Result;
import com.example.housetrading.entity.Payment;
import com.example.housetrading.entity.User;
import com.example.housetrading.service.PaymentService;
import com.example.housetrading.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private UserService userService;

    @GetMapping("/my")
    public Result<List<Payment>> myPayments() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getByUsername(username);
        
        LambdaQueryWrapper<Payment> wrapper = new LambdaQueryWrapper<>();
        if ("ADMIN".equals(user.getRole())) {
            // Admin sees all
        } else if ("LANDLORD".equals(user.getRole())) {
            wrapper.eq(Payment::getPayeeId, user.getId());
        } else {
            wrapper.eq(Payment::getPayerId, user.getId());
        }
        
        return Result.success(paymentService.list(wrapper));
    }

    @PostMapping("/pay/{id}")
    public Result<Void> pay(@PathVariable Integer id) {
        Payment payment = paymentService.getById(id);
        if (payment == null) {
            return Result.error("支付记录不存在");
        }
        payment.setStatus("SUCCESS");
        paymentService.updateById(payment);
        return Result.success();
    }
}

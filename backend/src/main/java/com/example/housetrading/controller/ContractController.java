package com.example.housetrading.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.housetrading.common.Result;
import com.example.housetrading.entity.Contract;
import com.example.housetrading.entity.User;
import com.example.housetrading.service.ContractService;
import com.example.housetrading.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/contracts")
public class ContractController {

    @Autowired
    private ContractService contractService;

    @Autowired
    private UserService userService;

    @PostMapping("/landlord/create")
    public Result<Void> create(@RequestBody Contract contract) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getByUsername(username);
        contract.setLandlordId(user.getId());
        contract.setStatus("DRAFT");
        contractService.save(contract);
        return Result.success();
    }

    @GetMapping("/my")
    public Result<List<java.util.Map<String, Object>>> myContracts() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getByUsername(username);
        List<Contract> list = contractService.list(new LambdaQueryWrapper<Contract>()
                .eq(Contract::getUserId, user.getId())
                .or()
                .eq(Contract::getLandlordId, user.getId())
                .orderByDesc(Contract::getCreateTime));
        return Result.success(enhanceContracts(list));
    }

    private List<java.util.Map<String, Object>> enhanceContracts(List<Contract> list) {
        return list.stream().map(c -> {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", c.getId());
            map.put("userId", c.getUserId());
            map.put("landlordId", c.getLandlordId());
            map.put("houseId", c.getHouseId());
            map.put("contractContent", c.getContractContent());
            map.put("totalAmount", c.getTotalAmount());
            map.put("status", c.getStatus());
            map.put("signTime", c.getSignTime());
            map.put("createTime", c.getCreateTime());
            
            com.example.housetrading.entity.House house = houseService.getById(c.getHouseId());
            map.put("houseTitle", house != null ? house.getTitle() : "未知房源");
            return map;
        }).collect(java.util.stream.Collectors.toList());
    }

    @Autowired
    private com.example.housetrading.service.HouseService houseService;

    @Autowired
    private com.example.housetrading.service.PaymentService paymentService;

    @PostMapping("/user/sign/{id}")
    public Result<String> sign(@PathVariable Integer id) {
        Contract contract = contractService.getById(id);
        if (contract == null) return Result.error("合同不存在");
        
        contract.setStatus("SIGNED");
        contract.setSignTime(LocalDateTime.now());
        contractService.updateById(contract);

        // Generate payment record
        com.example.housetrading.entity.Payment payment = new com.example.housetrading.entity.Payment();
        payment.setContractId(contract.getId());
        payment.setPayerId(contract.getUserId());
        payment.setPayeeId(contract.getLandlordId());
        payment.setAmount(contract.getTotalAmount());
        payment.setStatus("PENDING");
        paymentService.save(payment);

        return Result.success("签署成功，请前往支付管理完成支付");
    }
}

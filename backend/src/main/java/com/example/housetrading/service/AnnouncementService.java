package com.example.housetrading.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.housetrading.entity.Announcement;
import com.example.housetrading.mapper.AnnouncementMapper;
import org.springframework.stereotype.Service;

@Service
public class AnnouncementService extends ServiceImpl<AnnouncementMapper, Announcement> {
}

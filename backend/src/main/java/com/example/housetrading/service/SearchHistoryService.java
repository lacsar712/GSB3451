package com.example.housetrading.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.housetrading.entity.SearchHistory;
import com.example.housetrading.mapper.SearchHistoryMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SearchHistoryService extends ServiceImpl<SearchHistoryMapper, SearchHistory> {

    private static final int MAX_HISTORY_COUNT = 10;

    @Transactional
    public void addSearchRecord(Integer userId, String keyword) {
        LambdaQueryWrapper<SearchHistory> existWrapper = new LambdaQueryWrapper<SearchHistory>()
                .eq(SearchHistory::getUserId, userId)
                .eq(SearchHistory::getKeyword, keyword);
        SearchHistory existing = this.getOne(existWrapper);
        if (existing != null) {
            this.removeById(existing.getId());
        }

        SearchHistory record = new SearchHistory();
        record.setUserId(userId);
        record.setKeyword(keyword);
        this.save(record);

        long count = this.count(new LambdaQueryWrapper<SearchHistory>()
                .eq(SearchHistory::getUserId, userId));
        if (count > MAX_HISTORY_COUNT) {
            long deleteCount = count - MAX_HISTORY_COUNT;
            List<SearchHistory> oldestList = this.list(new LambdaQueryWrapper<SearchHistory>()
                    .eq(SearchHistory::getUserId, userId)
                    .orderByAsc(SearchHistory::getCreateTime)
                    .last("LIMIT " + deleteCount));
            for (SearchHistory old : oldestList) {
                this.removeById(old.getId());
            }
        }
    }

    public List<SearchHistory> getRecentHistory(Integer userId) {
        return this.list(new LambdaQueryWrapper<SearchHistory>()
                .eq(SearchHistory::getUserId, userId)
                .orderByDesc(SearchHistory::getCreateTime)
                .last("LIMIT " + MAX_HISTORY_COUNT));
    }

    @Transactional
    public void deleteByIdAndUserId(Integer id, Integer userId) {
        this.remove(new LambdaQueryWrapper<SearchHistory>()
                .eq(SearchHistory::getId, id)
                .eq(SearchHistory::getUserId, userId));
    }

    @Transactional
    public void clearByUserId(Integer userId) {
        this.remove(new LambdaQueryWrapper<SearchHistory>()
                .eq(SearchHistory::getUserId, userId));
    }
}

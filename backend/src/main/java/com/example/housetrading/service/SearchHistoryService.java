package com.example.housetrading.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.housetrading.entity.SearchHistory;
import com.example.housetrading.mapper.SearchHistoryMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SearchHistoryService extends ServiceImpl<SearchHistoryMapper, SearchHistory> {

    private static final int MAX_HISTORY_SIZE = 10;

    public void addSearchHistory(Integer userId, String keyword) {
        SearchHistory existing = getOne(new LambdaQueryWrapper<SearchHistory>()
                .eq(SearchHistory::getUserId, userId)
                .eq(SearchHistory::getKeyword, keyword));
        if (existing != null) {
            removeById(existing.getId());
        }

        SearchHistory record = new SearchHistory();
        record.setUserId(userId);
        record.setKeyword(keyword);
        save(record);

        long count = count(new LambdaQueryWrapper<SearchHistory>()
                .eq(SearchHistory::getUserId, userId));
        if (count > MAX_HISTORY_SIZE) {
            SearchHistory oldest = getOne(new LambdaQueryWrapper<SearchHistory>()
                    .eq(SearchHistory::getUserId, userId)
                    .orderByAsc(SearchHistory::getCreateTime)
                    .last("LIMIT 1"));
            if (oldest != null) {
                removeById(oldest.getId());
            }
        }
    }

    public List<SearchHistory> getSearchHistory(Integer userId) {
        return list(new LambdaQueryWrapper<SearchHistory>()
                .eq(SearchHistory::getUserId, userId)
                .orderByDesc(SearchHistory::getCreateTime)
                .last("LIMIT " + MAX_HISTORY_SIZE));
    }

    public void deleteSearchHistory(Integer userId, Integer id) {
        remove(new LambdaQueryWrapper<SearchHistory>()
                .eq(SearchHistory::getUserId, userId)
                .eq(SearchHistory::getId, id));
    }

    public void clearSearchHistory(Integer userId) {
        remove(new LambdaQueryWrapper<SearchHistory>()
                .eq(SearchHistory::getUserId, userId));
    }
}

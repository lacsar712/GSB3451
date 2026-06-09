package com.example.housetrading.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.housetrading.entity.SearchHistory;
import com.example.housetrading.mapper.SearchHistoryMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SearchHistoryService extends ServiceImpl<SearchHistoryMapper, SearchHistory> {

    public static final int MAX_HISTORY = 10;

    public List<SearchHistory> listByUser(Integer userId) {
        return list(new LambdaQueryWrapper<SearchHistory>()
                .eq(SearchHistory::getUserId, userId)
                .orderByDesc(SearchHistory::getCreateTime));
    }

    public void addHistory(Integer userId, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return;
        }
        keyword = keyword.trim();
        // 同关键词去重：删除已存在的旧记录
        remove(new LambdaQueryWrapper<SearchHistory>()
                .eq(SearchHistory::getUserId, userId)
                .eq(SearchHistory::getKeyword, keyword));

        SearchHistory history = new SearchHistory();
        history.setUserId(userId);
        history.setKeyword(keyword);
        save(history);

        // 超过 MAX_HISTORY 条则删除最早的
        long total = count(new LambdaQueryWrapper<SearchHistory>()
                .eq(SearchHistory::getUserId, userId));
        if (total > MAX_HISTORY) {
            long over = total - MAX_HISTORY;
            List<SearchHistory> oldest = list(new LambdaQueryWrapper<SearchHistory>()
                    .eq(SearchHistory::getUserId, userId)
                    .orderByAsc(SearchHistory::getCreateTime)
                    .last("LIMIT " + over));
            for (SearchHistory h : oldest) {
                removeById(h.getId());
            }
        }
    }

    public boolean deleteByIdAndUser(Integer id, Integer userId) {
        return remove(new LambdaQueryWrapper<SearchHistory>()
                .eq(SearchHistory::getId, id)
                .eq(SearchHistory::getUserId, userId));
    }

    public boolean clearByUser(Integer userId) {
        return remove(new LambdaQueryWrapper<SearchHistory>()
                .eq(SearchHistory::getUserId, userId));
    }
}

package com.example.housetrading.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.housetrading.entity.SearchHistory;
import com.example.housetrading.mapper.SearchHistoryMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SearchHistoryService extends ServiceImpl<SearchHistoryMapper, SearchHistory> {

    private static final int MAX_HISTORY_COUNT = 10;

    @Transactional
    public void addSearchHistory(Integer userId, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return;
        }

        SearchHistory existing = getOne(new LambdaQueryWrapper<SearchHistory>()
                .eq(SearchHistory::getUserId, userId)
                .eq(SearchHistory::getKeyword, keyword));

        if (existing != null) {
            update(new LambdaUpdateWrapper<SearchHistory>()
                    .eq(SearchHistory::getId, existing.getId())
                    .set(SearchHistory::getUpdateTime, LocalDateTime.now()));
            return;
        }

        SearchHistory history = new SearchHistory();
        history.setUserId(userId);
        history.setKeyword(keyword);
        save(history);

        cleanupOldHistory(userId);
    }

    private void cleanupOldHistory(Integer userId) {
        List<SearchHistory> list = list(new LambdaQueryWrapper<SearchHistory>()
                .eq(SearchHistory::getUserId, userId)
                .orderByDesc(SearchHistory::getUpdateTime));

        if (list.size() > MAX_HISTORY_COUNT) {
            for (int i = MAX_HISTORY_COUNT; i < list.size(); i++) {
                removeById(list.get(i).getId());
            }
        }
    }

    public List<SearchHistory> getUserSearchHistory(Integer userId) {
        return list(new LambdaQueryWrapper<SearchHistory>()
                .eq(SearchHistory::getUserId, userId)
                .orderByDesc(SearchHistory::getUpdateTime)
                .last("LIMIT " + MAX_HISTORY_COUNT));
    }

    @Transactional
    public void deleteSearchHistory(Integer userId, Integer id) {
        remove(new LambdaQueryWrapper<SearchHistory>()
                .eq(SearchHistory::getId, id)
                .eq(SearchHistory::getUserId, userId));
    }

    @Transactional
    public void clearAllSearchHistory(Integer userId) {
        remove(new LambdaQueryWrapper<SearchHistory>()
                .eq(SearchHistory::getUserId, userId));
    }
}

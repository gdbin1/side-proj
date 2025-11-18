package com.gbk.sideproj.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gbk.sideproj.mapper.FavoriteMapper;
import com.gbk.sideproj.domain.WebItem; // ✅ 수정: model → domain

@Service
public class FavoriteService {

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Transactional
    public boolean addFavorite(Long userId, String plnmNo, String pbctNo) {
        if (favoriteMapper.existsFavorite(userId, plnmNo, pbctNo) > 0) {
            return false;
        }
        favoriteMapper.insertFavorite(userId, plnmNo, pbctNo);
        return true;
    }

    @Transactional
    public void removeFavorite(Long userId, String plnmNo, String pbctNo) {
        favoriteMapper.deleteFavorite(userId, plnmNo, pbctNo);
    }

    public List<WebItem> getUserFavorites(Long userId) {
        return favoriteMapper.selectUserFavorites(userId);
    }
}

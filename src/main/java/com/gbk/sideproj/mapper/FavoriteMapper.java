package com.gbk.sideproj.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.gbk.sideproj.domain.WebItem; // ✅ 수정됨

@Mapper
public interface FavoriteMapper {

    int existsFavorite(@Param("userId") Long userId,
                       @Param("plnmNo") String plnmNo,
                       @Param("pbctNo") String pbctNo);

    void insertFavorite(@Param("userId") Long userId,
                        @Param("plnmNo") String plnmNo,
                        @Param("pbctNo") String pbctNo);

    void deleteFavorite(@Param("userId") Long userId,
                        @Param("plnmNo") String plnmNo,
                        @Param("pbctNo") String pbctNo);

    List<WebItem> selectUserFavorites(@Param("userId") Long userId);
}

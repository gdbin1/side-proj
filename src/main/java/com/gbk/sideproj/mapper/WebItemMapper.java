package com.gbk.sideproj.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.gbk.sideproj.domain.WebItem;

@Mapper
public interface WebItemMapper {

    int upsert(WebItem it);

    // 단건 조회 대신 중복 PK 가능성을 고려한 List 반환
    List<WebItem> findByPkList(@Param("plnmNo") String plnmNo, @Param("pbctNo") String pbctNo);

    // 전체 조회
    List<WebItem> findAll();

    // PK 조회
    List<Map<String, String>> findAllPk();

    // 전체 삭제
    void deleteAll();

    // 일괄 삽입
    void insertItems(List<WebItem> newList);

    // 팝업용 JOIN 데이터
    List<Map<String, Object>> findItemWithDetails(@Param("cltrNm") String cltrNm);
}

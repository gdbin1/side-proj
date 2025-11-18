package com.gbk.sideproj.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import com.gbk.sideproj.domain.WebItemDetail;
import java.util.List;

@Mapper
public interface WebItemDetailMapper {

    // 단건 insert
    void insert(WebItemDetail detail);

    // 특정 키 조회
    WebItemDetail findByKeys(@Param("plnmNo") String plnmNo, @Param("pbctNo") String pbctNo);

    // 특정 물건(plnmNo/pbctNo)에 대한 모든 상세 이력 조회
    List<WebItemDetail> findAllByKeys(@Param("plnmNo") String plnmNo, @Param("pbctNo") String pbctNo);

    // 단일 상세 삭제
    void deleteByKeys(@Param("plnmNo") String plnmNo, @Param("pbctNo") String pbctNo);

    // 특정 물건에 대한 상세 전체 삭제
    void deleteAllByItem(@Param("plnmNo") String plnmNo, @Param("pbctNo") String pbctNo);

    // 전체 삭제 (옵션)
    void deleteAll();

    // ================== 신규: 팝업용 최근 3건 이력 조회 ==================
    @Select("""
        SELECT plnm_no, pbct_no, pbct_begn_dtm, pbct_cls_dtm, min_bid_prc
        FROM web_item_detail
        WHERE plnm_no = #{plnmNo} AND pbct_no = #{pbctNo}
        ORDER BY pbct_begn_dtm DESC
        LIMIT 3
    """)
    List<WebItemDetail> findRecentHistory(
            @Param("plnmNo") String plnmNo,
            @Param("pbctNo") String pbctNo
    );
}

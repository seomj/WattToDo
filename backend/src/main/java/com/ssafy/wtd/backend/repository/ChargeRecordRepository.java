package com.ssafy.wtd.backend.repository;

import com.ssafy.wtd.backend.model.ChargeRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper // MyBatis Mapper 인터페이스임을 명시
public interface ChargeRecordRepository {

    int insertRecord(ChargeRecord record); // 충전 시작 시 초기 기록 삽입
    int updateRecord(
            @Param("recordId") Long recordId,
            @Param("status") String status,
            @Param("chargedKwh") float chargedKwh,
            @Param("endTime") LocalDateTime endTime,
            @Param("durationMin") float durationMin,
            @Param("chargingCost") int chargingCost
    ); // 충전 완료 후 최종 데이터 업데이트
    ChargeRecord selectActiveRecordByUserId(Long userId); // 충전 중인 기록을 조회하는 메서드
    ChargeRecord selectRecordById(Long recordId);
    List<ChargeRecord> findAllByUserId(@Param("userId") Long userId);
    int deleteById(Long recordId); // 충전 기록 삭제
}
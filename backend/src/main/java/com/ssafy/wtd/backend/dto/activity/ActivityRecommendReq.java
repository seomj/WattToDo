package com.ssafy.wtd.backend.dto.activity;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ActivityRecommendReq {
    private Long userId;
    private double latitude; // 충전소 위도, 경도
    private double longitude;

    private int chargingTime; // 예: 30 (분 단위)
    private boolean ecoFriendly; // 친환경 여부 포함 체크박스
    private boolean publicTransport; // 대중교통 이용 가능 여부 (체크 시 4km 확장)
    private int travelTime; // 이동 시간 (예: 20분)
    private int personCount; // 인원 (예: 1명)

    private List<String> purposes; // 휴식, 식사, 공부, 운동, 쇼핑, 관광
    private List<String> locations; // 카페, 편의점, 공원, 산책로, 식당, 쇼핑몰, 서점, 도서관
    private String preferences; // 조용한 곳, 사람 적은 곳, 빠르게 다녀올 곳 등 자유 텍스트
}
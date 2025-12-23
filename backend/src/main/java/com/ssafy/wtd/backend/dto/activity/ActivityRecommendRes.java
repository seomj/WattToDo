package com.ssafy.wtd.backend.dto.activity;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ActivityRecommendRes {
    private List<PlaceInfo> recommendations; // 추천 장소 3곳

    @Getter
    @Builder
    public static class PlaceInfo {
        private String placeName; // 예: "그린리프 카페"
        private String category; // 예: "카페"
        private String description; // 예: "친환경 인테리어와 유기농 원두를 사용하는..."
        private int distanceMeter; // 예: 350 (단위: m)
        private int travelTimeMin; // 예: 5 (단위: 분)
        private boolean isEcoFriendly; // 친환경 여부 (나뭇잎 아이콘 표시용)
        private String imageUrl; // 아이콘 또는 장소 이미지
        private String phone; // 전화번호
        private String placeUrl; // 상세 페이지 (웹사이트 대용)
        private String address; // 주소
        private double latitude; // 위도
        private double longitude; // 경도
    }
}

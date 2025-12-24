package com.ssafy.wtd.backend.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class EnvEvChargerRes {

    // 최상위 필드
    private String resultMsg;
    private int totalCount;

    private Items items;

    public boolean isEmpty() {
        return items == null
                || items.item == null
                || items.item.isEmpty();
    }

    public List<Item> getItems() {
        return items.item;
    }

    // =========================
    // Items
    // =========================
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Items {

        private List<Item> item;
    }

    // =========================
    // Item (충전기 단위)
    // =========================
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item {

        /* ========= 충전소 ========= */
        @JsonProperty("statId")
        private String statId;

        @JsonProperty("statNm")
        private String statNm;

        @JsonProperty("addr")
        private String addr;

        @JsonProperty("lat")
        private String lat;

        @JsonProperty("lng")
        private String lng;

        /* ========= 충전기 ========= */
        @JsonProperty("chgerId")
        private String chgerId;

        @JsonProperty("chgerNm")
        private String chgerNm;

        @JsonProperty("chgerType")
        private String chgerType;

        @JsonProperty("stat")
        private String stat;

        @JsonProperty("statUpdDt")
        private String statUpdDt;

        /* ========= 운영 ========= */
        @JsonProperty("useTime")
        private String useTime;

        @JsonProperty("busiNm")
        private String busiNm;

        @JsonProperty("busiCall")
        private String busiCall;

        @JsonProperty("parkingFree")
        private String parkingFree;

        @JsonProperty("note")
        private String note;

        @JsonProperty("powerType")
        private String powerType;
    }
}

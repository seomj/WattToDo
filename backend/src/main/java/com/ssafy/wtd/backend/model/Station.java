package com.ssafy.wtd.backend.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Station {
    private String stationId;
    private String stationName;

    private String address;
    private Double lat;
    private Double lng;

    private String city;
    private String district;
}
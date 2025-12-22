package com.ssafy.wtd.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class CityDistrictUpdater {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 공공 데이터 가져올 때마다 실행되는 메서드
    public void updateCityDistrict() {
        String sql = "UPDATE charging_station " +
                "SET city = CASE " +
                "    WHEN address NOT LIKE '% %' THEN address " +
                "    ELSE SUBSTRING_INDEX(address, ' ', 1) " +
                "END, " +
                "district = CASE " +
                "    WHEN address LIKE '세종특별자치시%' THEN NULL " +
                "    WHEN (LENGTH(address) - LENGTH(REPLACE(address, ' ', ''))) >= 1 " +
                "    THEN SUBSTRING_INDEX(SUBSTRING_INDEX(address, ' ', 2), ' ', -1) " +
                "    ELSE NULL " +
                "END";

        jdbcTemplate.execute(sql);
        System.out.println("City and district columns updated.");
    }
}
-- 테스트 사용자 데이터를 삽입합니다.
INSERT INTO wtd_db.user
    (user_id, email, password, name, nickname, role, status, created_at, updated_at)
VALUES
    (101, 'test1@example.com', '1234', '테스트1', 'TestUser01', 'USER', 'ACTIVE', NOW(), NOW()),
    (102, 'test2@example.com', '1234', '테스트2', 'TestUser02', 'USER', 'ACTIVE', NOW(), NOW()),
    (103, 'test3@example.com', '1234', '테스트3', 'TestUser03', 'USER', 'ACTIVE', NOW(), NOW())
ON DUPLICATE KEY UPDATE
    -- user_id가 이미 존재할 경우, 이메일, 이름, 닉네임, 업데이트 시간만 업데이트합니다.
    email = VALUES(email),
    name = VALUES(name),
    nickname = VALUES(nickname),
    updated_at = NOW();
-- 주: '1234'는 예시이며, 실제 운영 환경에서는 반드시 비밀번호를 해시 처리해야 합니다.

-- 테스트용 충전소 데이터
INSERT INTO charging_station (station_id, station_name, address, lat, lng, city, district)
VALUES
('STN_001234', '광화문 충전소', '서울특별시 중구 세종대로 110', 37.5665, 126.9780, '서울', '중구'),
('STN_001235', '강남역 충전소', '서울특별시 강남구 강남대로 396', 37.4979, 127.0276, '서울', '강남구'),
('STN_001236', '홍대입구 충전소', '서울특별시 마포구 양화로 160', 37.5572, 126.9239, '서울', '마포구'),
('STN_001237', '여의도 충전소', '서울특별시 영등포구 여의도동', 37.5219, 126.9245, '서울', '영등포구'),
('STN_001238', '잠실역 충전소', '서울특별시 송파구 올림픽로 지하 265', 37.5133, 127.1000, '서울', '송파구')
ON DUPLICATE KEY UPDATE
    station_name = VALUES(station_name),
    address = VALUES(address),
    lat = VALUES(lat),
    lng = VALUES(lng),
    city = VALUES(city),
    district = VALUES(district);

-- 101번 사용자의 차량 정보 (아이오닉 6 롱레인지 2WD 기준)
INSERT INTO vehicle (
    user_id, 
    model, 
    efficiency, 
    battery_capacity, 
    max_range, 
    dc_charge_type, 
    ac_charge_type, 
    created_at
) VALUES (
    101, 
    '아이오닉 6 롱레인지', 
    6.0,            -- 전비: 6.0 km/kWh
    77.4,           -- 배터리 용량: 77.4 kWh
    524.0,          -- 완충 시 주행 거리: 524 km
    'DC콤보',       -- 급속 충전 방식
    'AC단상 5핀',   -- 완속 충전 방식
    NOW()
);

-- (추가 예시) 102번 사용자의 차량 정보 (테슬라 모델 3 RWD 기준)
INSERT INTO vehicle (
    user_id, 
    model, 
    efficiency, 
    battery_capacity, 
    max_range, 
    dc_charge_type, 
    ac_charge_type, 
    created_at
) VALUES (
    102, 
    'Model 3 RWD', 
    5.1, 
    60.0, 
    380.0, 
    '테슬라 전용(NACS)', 
    '테슬라 전용(NACS)', 
    NOW()
);
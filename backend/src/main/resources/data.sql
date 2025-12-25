-- 1. 차량 모델 정보(Master Data) 삽입
INSERT INTO vehicle (vehicle_id, model, efficiency, battery_capacity, max_range, fast_charge_type, slow_charge_type, created_at)
VALUES 
(1, '현대 더 뉴 아이오닉 5 롱레인지 2WD', 5.77, 84.0, 485.0, 'CCS Combo 1', 'Type 1 (5핀)', '2024-03-01 00:00:00'),
(2, '기아 더 뉴 EV6 롱레인지 2WD', 5.88, 84.0, 494.0, 'CCS Combo 1', 'Type 1 (5핀)', '2024-05-14 00:00:00'),
(3, '테슬라 모델 3 하이랜드 롱레인지 AWD', 6.23, 81.6, 508.0, 'Tesla NACS', 'Type 1 (Adapter)', '2024-04-04 00:00:00');

-- 2. 사용자 정보 삽입 (각 사용자에게 vehicle_id 할당)
INSERT INTO user (user_id, email, password, name, vehicle_id, created_at, updated_at)
VALUES
(101, 'test1@example.com', '1234', '테스트1', 1, NOW(), NOW()),
(102, 'test2@example.com', '1234', '테스트2', 2, NOW(), NOW()),
(103, 'test3@example.com', '1234', '테스트3', 3, NOW(), NOW()),
(104, 'test4@example.com', '1234', '테스트4', NULL, NOW(), NOW())
AS new
ON DUPLICATE KEY UPDATE
    email = new.email,
    name = new.name,
    vehicle_id = new.vehicle_id,
    updated_at = NOW();

-- 101번: 현재 충전 중인 상태 (record_id 자동 생성)
INSERT INTO charge_record (user_id, station_id, status, start_kwh, target_kwh, charged_kwh, charger_capacity, start_time, created_at)
VALUES (101, 'CGA00129', 'CHARGING', 20.0, 60.0, NULL, 100.0, '2025-12-24 09:07:10', '2025-12-24 09:07:10');

-- 102번: 급속 충전 완료 및 탄소 기록 연결
INSERT INTO charge_record (user_id, station_id, status, start_kwh, target_kwh, charged_kwh, charger_capacity, start_time, end_time, duration_min, charging_cost)
VALUES (102, 'AH000021', 'COMPLETED', 20.0, 70.0, 50.0, 100.0, '2025-12-21 14:00:00', '2025-12-21 14:45:00', 45.0, 16500);

-- 방금 삽입된 102번의 record_id를 가져와 탄소 기록 삽입
INSERT INTO carbon_record (record_id, user_id, carbon_saved, created_at) 
VALUES (LAST_INSERT_ID(), 102, 24.99, '2025-12-21 14:45:00');

-- 103번: 완속 충전 완료 및 탄소 기록 연결
INSERT INTO charge_record (user_id, station_id, status, start_kwh, target_kwh, charged_kwh, charger_capacity, start_time, end_time, duration_min, charging_cost)
VALUES (103, 'AH000039', 'COMPLETED', 10.0, 60.0, 50.0, 7.0, '2025-12-22 22:00:00', '2025-12-23 05:10:00', 430.0, 11000);

-- 방금 삽입된 103번의 record_id를 가져와 탄소 기록 삽입
INSERT INTO carbon_record (record_id, user_id, carbon_saved, created_at) 
VALUES (LAST_INSERT_ID(), 103, 27.82, '2025-12-23 05:10:00');
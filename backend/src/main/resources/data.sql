-- 테스트 사용자 데이터를 삽입합니다.
INSERT INTO wtd_db.user
    (user_id, email, password, name, nickname, role, status, created_at, updated_at)
VALUES
    (101, 'test1@example.com', '1234', '테스트1', 'TestUser01', 'USER', 'ACTIVE', NOW(), NOW()),
    (102, 'test2@example.com', '1234', '테스트2', 'TestUser02', 'USER', 'ACTIVE', NOW(), NOW()),
    (103, 'test3@example.com', '1234', '테스트3', 'TestUser03', 'USER', 'ACTIVE', NOW(), NOW()),
    (104, 'test4@example.com', '1234', '테스트4', 'TestUser04', 'USER', 'ACTIVE', NOW(), NOW())
ON DUPLICATE KEY UPDATE
    -- user_id가 이미 존재할 경우, 이메일, 이름, 닉네임, 업데이트 시간만 업데이트합니다.
    email = VALUES(email),
    name = VALUES(name),
    nickname = VALUES(nickname),
    updated_at = NOW();
-- 주: '1234'는 예시이며, 실제 운영 환경에서는 반드시 비밀번호를 해시 처리해야 합니다.

-- 차량 정보 테이블 데이터 삽입
-- 101번 사용자: 현대 더 뉴 아이오닉 5 롱레인지 2WD
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
    '현대 더 뉴 아이오닉 5 롱레인지 2WD',
    5.77,           -- 계산값 (485/84)
    84.0,
    485.0,
    'CCS Combo 1',
    'Type 1 (5핀)',
    '2024-03-01 00:00:00'
);

-- 102번 사용자: 기아 더 뉴 EV6 롱레인지 2WD
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
    '기아 더 뉴 EV6 롱레인지 2WD',
    5.88,           -- 계산값 (494/84)
    84.0,
    494.0,
    'CCS Combo 1',
    'Type 1 (5핀)',
    '2024-05-14 00:00:00'
);

-- 103번 사용자: 테슬라 모델 3 하이랜드 롱레인지 AWD
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
    103,
    '테슬라 모델 3 하이랜드 롱레인지 AWD',
    6.23,           -- 계산값 (508/81.6)
    81.6,
    508.0,
    'Tesla NACS',
    'Type 1 (Adapter)',
    '2024-04-04 00:00:00'
);

-- TODO: 충전 기록 테스트 데이터

-- TODO: 탄소 절감량 테스트 데이터


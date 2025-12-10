-- 1. 데이터베이스가 이미 존재한다면 삭제 (개발/테스트 환경에서 사용 권장)
DROP DATABASE IF EXISTS wtd_db;

-- 2. 데이터베이스 생성
CREATE DATABASE IF NOT EXISTS wtd_db
    CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

-- 3. 사용할 데이터베이스 선택
USE wtd_db;

-- 2.1. 사용자 정보 테이블
CREATE TABLE user (
    user_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL, -- Hashed password
    name VARCHAR(50) NOT NULL,
    nickname VARCHAR(50),
    role VARCHAR(10) NOT NULL DEFAULT 'USER', -- USER / ADMIN
    status VARCHAR(10) NOT NULL DEFAULT 'ACTIVE', -- active / disabled
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '사용자 정보';

-- 2.2. 차량 정보 테이블
CREATE TABLE vehicle (
    vehicle_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    model VARCHAR(100) NOT NULL,
    efficiency FLOAT, -- 전비 (e.g., km/kWh)
    battery_capacity FLOAT, -- 배터리 용량 (kWh)
    max_range FLOAT, -- 완충 시 주행 거리 (km)
    dc_charge_type VARCHAR(50), -- 차량이 지원하는 급속 충전 방식
    ac_charge_type VARCHAR(50), -- 차량이 지원하는 완속 충전 방식
    created_at DATETIME NOT NULL,
    
    FOREIGN KEY (user_id) REFERENCES user(user_id)
) COMMENT '사용자별 차량 정보';

-- 2.3. 충전소 정보 테이블
CREATE TABLE charging_station (
    station_id VARCHAR(20) PRIMARY KEY, -- KEPCO csId와 동일하게 VARCHAR 사용
    station_name VARCHAR(100) NOT NULL,
    address VARCHAR(255) NOT NULL,
    lat DOUBLE NOT NULL,
    lng DOUBLE NOT NULL,
    city VARCHAR(50),
    district VARCHAR(50)
) COMMENT '충전소 위치 및 기본 정보';

-- 2.4. 충전기 정보 테이블
CREATE TABLE charger (
    charger_id VARCHAR(20) PRIMARY KEY,     -- 충전기 고유 ID
    station_id VARCHAR(20) NOT NULL,        -- charging_station FK
    
    charger_name VARCHAR(50),               -- 충전기 번호(현장 표기 1번, 2번 등)
    model_name VARCHAR(50),                 -- 충전기 기계 모델명
    
    status INT NOT NULL,                    -- 0=정상, 1=고장, 2=점검, 3=사용중
    last_sync DATETIME NOT NULL,            -- 상태 갱신 시각

    FOREIGN KEY (station_id) REFERENCES charging_station(station_id)
) COMMENT '충전소에 설치된 개별 충전기 정보 및 상태';

-- 2.5. 충전기 포트 정보 테이블
CREATE TABLE charger_port (
    port_id VARCHAR(30) PRIMARY KEY,         -- 포트 고유 ID(pk)
    charger_id VARCHAR(20) NOT NULL,         -- charger FK
    port_name VARCHAR(50),                   -- 포트명 (A-01, B-02 등 선택)
    
    port_type VARCHAR(20) NOT NULL,          -- 타입 (Type1, Type2, CCS1, CHAdeMO 등)
    capacity INT NOT NULL,                   -- 포트 출력(kW)

    is_ac BOOLEAN NOT NULL,                  -- AC 여부 (TRUE=AC 완속)
    is_dc BOOLEAN NOT NULL,                  -- DC 여부 (TRUE=DC 급속)

    FOREIGN KEY (charger_id) REFERENCES charger(charger_id)
) COMMENT '충전기의 개별 포트(커넥터) 상세 정보';

-- 2.6. 충전 기록 테이블
CREATE TABLE charge_record (
    record_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    station_id VARCHAR(20) NOT NULL,
    
    charged_kwh FLOAT,
    start_kwh FLOAT,
    target_kwh FLOAT,
    duration_min INT,
    
    start_time DATETIME NOT NULL,
    end_time DATETIME,
    
    charging_cost INT,
    
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES user(user_id),
    FOREIGN KEY (station_id) REFERENCES charging_station(station_id)
) COMMENT '사용자의 충전 활동 기록';

-- 2.7. 탄소 절감 기록 테이블
CREATE TABLE carbon_record (
    carbon_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    record_id BIGINT NOT NULL, -- charge_record.record_id (FK)
    user_id BIGINT NOT NULL,
    carbon_saved FLOAT NOT NULL, -- 탄소 절감량 (kg)
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 완료 시점

    FOREIGN KEY (record_id) REFERENCES charge_record(record_id),
    FOREIGN KEY (user_id) REFERENCES user(user_id)
) COMMENT '충전 기록에 따른 탄소 절감량 기록';

-- 2.8. 즐겨찾는 충전소 테이블
CREATE TABLE favorite_station (
    user_id BIGINT NOT NULL,
    station_id VARCHAR(20) NOT NULL,
    
    PRIMARY KEY (user_id, station_id), -- 복합 기본 키 설정
    
    FOREIGN KEY (user_id) REFERENCES user(user_id),
    FOREIGN KEY (station_id) REFERENCES charging_station(station_id)
) COMMENT '사용자별 즐겨찾는 충전소';

-- 2.9. 탄소 계산 기준 설정 테이블
CREATE TABLE carbon_config (
    config_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    
    gasoline_co2_per_l FLOAT NOT NULL, -- 휘발유 1L 기준 CO₂ 배출량 (kg)
    avg_fuel_efficiency FLOAT NOT NULL, -- 평균 내연기관 연비 (km/L). 비교 기준
    ev_co2_per_kwh FLOAT NOT NULL, -- 전기 에너지 1kWh당 CO₂ 배출량 (kg)
    
    config_version VARCHAR(20) NOT NULL, -- 기준값 버전
    
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    updated_by BIGINT, -- 기준값을 수정한 관리자 ID (user.user_id)
    
    FOREIGN KEY (updated_by) REFERENCES user(user_id)
) COMMENT '탄소 절감량 계산에 사용되는 기준값';

-- 3.1. 기본 관리자 계정 삽입 (carbon_config의 updated_by를 위한 선행 작업)
INSERT INTO user (
    user_id, email, password, name, nickname, role, status, created_at, updated_at
) VALUES (
    1, 
    'admin@example.com', 
    '1234', -- 실제 운영 시에는 안전한 해시된 비밀번호 사용
    '관리자', 
    'SystemAdmin', 
    'ADMIN', 
    'ACTIVE', 
    NOW(), 
    NOW()
);

-- 3.2. 초기 탄소 계산 기준값 설정
INSERT INTO carbon_config (
    gasoline_co2_per_l, avg_fuel_efficiency, ev_co2_per_kwh, config_version, updated_by
) VALUES (
    2.18,
    10.0, -- 예시값: 평균 연비 10 km/L (확실하지 않음 - 통계청 등 공신력 있는 최신 자료로 대체 필요)
    0.4747,
    'v1.0.2025',
    1 -- 관리자 user_id
);
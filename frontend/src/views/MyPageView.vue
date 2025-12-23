<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';

const props = defineProps({
  user: {
    type: Object,
    default: null
  }
});

const emit = defineEmits(['navigate', 'logout']);

const vehicle = ref(null);
const chargeRecords = ref([]);
const favoriteStations = ref([]);
const loading = ref(true);

const fetchMyPageData = async () => {
    loading.value = true;
    const token = localStorage.getItem('accessToken');
    if (!token) return;

    try {
        const headers = { 'Authorization': `Bearer ${token}` };
        
        // 1. Fetch Vehicle
        try {
            const vRes = await axios.get('http://localhost:8080/vehicles/me', { headers });
            if (vRes.data.success) vehicle.value = vRes.data.data;
        } catch (e) { console.log("No vehicle found or error"); }

        // 2. Fetch Charge Records
        try {
            const rRes = await axios.get('http://localhost:8080/charge-records/me', { headers });
            chargeRecords.value = rRes.data;
        } catch (e) { console.error("Failed to fetch records", e); }

        // 3. Fetch Favorite Stations
        try {
            const fRes = await axios.get('http://localhost:8080/favorites', { headers });
            favoriteStations.value = fRes.data;
        } catch (e) { console.error("Failed to fetch favorites", e); }

    } catch (error) {
        console.error("Failed to load MyPage data", error);
    } finally {
        loading.value = false;
    }
};

const formatDate = (dateStr) => {
    if (!dateStr) return '';
    const date = new Date(dateStr);
    return `${date.getFullYear()}.${String(date.getMonth() + 1).padStart(2, '0')}.${String(date.getDate()).padStart(2, '0')}`;
};

onMounted(fetchMyPageData);
</script>

<template>
  <div class="mypage-container">
    <div class="mypage-header">
      <h1>마이페이지</h1>
      <p>계정 정보 및 설정을 관리하세요</p>
    </div>

    <div class="mypage-content">
      <!-- Left Column: Profile & Vehicle -->
      <div class="side-column">
        <!-- Profile Card -->
        <div class="card profile-card">
          <div class="profile-header">
            <div class="avatar-large">
              <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                <circle cx="12" cy="7" r="4"></circle>
              </svg>
              <button class="edit-avatar-btn">📷</button>
            </div>
            <h2 class="user-name">{{ user?.name || '사용자' }}</h2>
            <p class="user-email">{{ user?.email }}</p>
            <p class="join-date">가입일: {{ formatDate(user?.createdAt) }}</p>
            <button class="text-btn">📝 프로필 수정</button>
          </div>
        </div>

        <!-- Vehicle Card -->
        <div class="card vehicle-card">
          <div class="card-header">
            <h3>🚗 차량 정보</h3>
            <button class="text-btn" v-if="vehicle" @click="console.log('Update vehicle')">수정</button>
          </div>
          
          <div v-if="vehicle" class="vehicle-info">
            <div class="v-model">{{ vehicle.model }}</div>
            <div class="v-details">
              <div class="v-item">
                <span class="label">배터리 용량</span>
                <span class="value">{{ vehicle.batteryCapacity }} kWh</span>
              </div>
              <div class="v-item">
                <span class="label">충전 타입</span>
                <span class="value">{{ vehicle.dcChargeType || 'DC 콤보' }}</span>
              </div>
            </div>
          </div>
          
          <div v-else class="no-vehicle">
            <p>등록된 차량이 없습니다.</p>
            <button class="outline-btn" @click="console.log('Register vehicle')">차량 등록하기</button>
          </div>
        </div>
      </div>

      <!-- Right Column: Stats, History, Favorites, Account -->
      <div class="main-column">
        <!-- Charge History -->
        <div class="card history-card">
          <div class="card-header">
            <h3>🕒 충전 기록</h3>
            <button class="text-btn">전체 보기 ></button>
          </div>
          <div class="history-list">
            <div v-for="record in chargeRecords.slice(0, 5)" :key="record.recordId" class="history-item">
              <div class="item-left">
                <div class="status-icon" :class="record.status.toLowerCase()">⚡</div>
                <div class="item-info">
                  <div class="st-name">{{ record.stationName || '충전소' }}</div>
                  <div class="st-date">{{ formatDate(record.startTime) }}</div>
                </div>
              </div>
              <div class="item-right">
                <div class="amount">{{ record.chargedKwh || 0 }} kWh</div>
                <div class="cost">₩{{ record.chargingCost?.toLocaleString() || 0 }}</div>
              </div>
            </div>
            <div v-if="chargeRecords.length === 0" class="empty-state">
              최근 충전 기록이 없습니다.
            </div>
          </div>
        </div>

        <!-- Favorites -->
        <div class="card favorites-card">
          <div class="card-header">
            <h3>❤️ 즐겨찾기 충전소</h3>
          </div>
          <div class="favorites-grid">
            <div v-for="fav in favoriteStations" :key="fav.stationId" class="fav-item" @click="$emit('navigate', 'HOME')">
              <div class="fav-top">
                <span class="fav-name">{{ fav.stationName }}</span>
                <span class="heart-icon">❤️</span>
              </div>
              <div class="fav-addr">{{ fav.address }}</div>
            </div>
            <div v-if="favoriteStations.length === 0" class="empty-state-full">
              즐겨찾는 충전소가 없습니다.
            </div>
          </div>
        </div>

        <!-- Account Management -->
        <div class="card account-card">
          <div class="card-header">
             <h3>👤 계정 관리</h3>
          </div>
          <div class="account-actions">
            <button class="action-item">
              <div class="action-left">
                <span class="icon">🔒</span>
                <div class="action-text">
                  <div class="title">비밀번호 변경</div>
                  <div class="sub">계정 보안을 위해 주기적으로 변경하세요</div>
                </div>
              </div>
              <span class="arrow">></span>
            </button>
            <button class="action-item" @click="$emit('logout')">
              <div class="action-left">
                <span class="icon">🚪</span>
                <div class="action-text">
                  <div class="title">로그아웃</div>
                </div>
              </div>
              <span class="arrow">></span>
            </button>
            <button class="action-item danger">
              <div class="action-left">
                <span class="icon">🗑️</span>
                <div class="action-text">
                  <div class="title">회원 탈퇴</div>
                </div>
              </div>
              <span class="arrow">></span>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.mypage-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
  background-color: #f9fafb;
  min-height: 100%;
}

.mypage-header {
  margin-bottom: 2rem;
}

.mypage-header h1 {
  font-size: 2rem;
  font-weight: 700;
  color: #111827;
  margin: 0 0 0.5rem 0;
}

.mypage-header p {
  color: #6b7280;
  margin: 0;
}

.mypage-content {
  display: flex;
  gap: 2rem;
}

.side-column {
  width: 350px;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.main-column {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.card {
  background: white;
  border-radius: 16px;
  border: 1px solid #e5e7eb;
  padding: 1.5rem;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.25rem;
}

.card-header h3 {
  font-size: 1.1rem;
  font-weight: 700;
  color: #111827;
  margin: 0;
}

/* Profile Card */
.profile-card {
  text-align: center;
}

.profile-header {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.avatar-large {
  width: 100px;
  height: 100px;
  background-color: #3b82f6;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  margin-bottom: 1.25rem;
}

.edit-avatar-btn {
  position: absolute;
  bottom: 0;
  right: 0;
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 50%;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.user-name {
  font-size: 1.5rem;
  font-weight: 700;
  margin: 0 0 0.25rem 0;
}

.user-email {
  color: #6b7280;
  margin: 0 0 0.25rem 0;
}

.join-date {
  font-size: 0.9rem;
  color: #9ca3af;
  margin-bottom: 1rem;
}

/* Vehicle Card */
.vehicle-info {
  background-color: #f9fafb;
  border-radius: 12px;
  padding: 1rem;
}

.v-model {
  font-weight: 700;
  font-size: 1.1rem;
  margin-bottom: 1rem;
  color: #2563eb;
}

.v-details {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.v-item {
  display: flex;
  justify-content: space-between;
  font-size: 0.95rem;
}

.v-item .label {
  color: #6b7280;
}

.v-item .value {
  font-weight: 600;
}

.no-vehicle {
  text-align: center;
  padding: 1rem 0;
}

.no-vehicle p {
  color: #6b7280;
  margin-bottom: 1rem;
}

/* History Card */
.history-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.history-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  background-color: #f9fafb;
  border-radius: 12px;
  transition: transform 0.2s;
}

.history-item:hover {
  transform: translateX(4px);
}

.item-left {
  display: flex;
  gap: 1rem;
  align-items: center;
}

.status-icon {
  width: 40px;
  height: 40px;
  background-color: #dcfce7;
  color: #16a34a;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.25rem;
}

.status-icon.charging {
  background-color: #dbeafe;
  color: #2563eb;
}

.st-name {
  font-weight: 600;
  color: #111827;
}

.st-date {
  font-size: 0.85rem;
  color: #6b7280;
}

.item-right {
  text-align: right;
}

.amount {
  font-weight: 600;
}

.cost {
  font-size: 0.9rem;
  color: #6b7280;
}

/* Favorites Grid */
.favorites-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 1rem;
}

.fav-item {
  border: 1px solid #e5e7eb;
  padding: 1.25rem;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.fav-item:hover {
  border-color: #3b82f6;
  background-color: #eff6ff;
}

.fav-top {
  display: flex;
  justify-content: space-between;
  margin-bottom: 0.5rem;
}

.fav-name {
  font-weight: 700;
  color: #111827;
}

.heart-icon {
  color: #ef4444;
}

.fav-addr {
  font-size: 0.85rem;
  color: #6b7280;
  line-height: 1.4;
}

/* Account Actions */
.account-actions {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.action-item {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.25rem;
  background-color: #f9fafb;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  transition: background-color 0.2s;
  text-align: left;
}

.action-item:hover {
  background-color: #f3f4f6;
}

.action-left {
  display: flex;
  gap: 1.25rem;
  align-items: center;
}

.action-left .icon {
  font-size: 1.5rem;
}

.action-text .title {
  font-weight: 600;
  color: #111827;
}

.action-text .sub {
  font-size: 0.85rem;
  color: #6b7280;
}

.action-item.danger .title {
  color: #ef4444;
}

/* Common UI Elements */
.text-btn {
  background: none;
  border: none;
  color: #3b82f6;
  font-weight: 600;
  cursor: pointer;
  font-size: 0.9rem;
}

.outline-btn {
  background: white;
  border: 1px solid #3b82f6;
  color: #3b82f6;
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.outline-btn:hover {
  background-color: #eff6ff;
}

.empty-state, .empty-state-full {
  padding: 2rem;
  text-align: center;
  color: #9ca3af;
  background-color: #f9fafb;
  border-radius: 12px;
}

.empty-state-full {
    grid-column: 1 / -1;
}

@media (max-width: 1024px) {
  .mypage-content {
    flex-direction: column;
  }
  .side-column {
    width: 100%;
  }
}
</style>

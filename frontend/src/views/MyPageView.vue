<script setup>
import { ref, onMounted, inject } from 'vue';
import axios from 'axios';

const props = defineProps({
  user: {
    type: Object,
    default: null
  }
});

const emit = defineEmits(['navigate', 'logout', 'withdraw', 'update-user']);
const showAlert = inject('showAlert');

const vehicle = ref(null);
const chargeRecords = ref([]);
const favoriteStations = ref([]);
const loading = ref(true);

// Vehicle Registration State
const showVehicleModal = ref(false);
const vehicleForm = ref({
    model: '',
    efficiency: null,
    batteryCapacity: null,
    maxRange: null,
    dcChargeType: '',
    acChargeType: ''
});

const openVehicleModal = (existingVehicle = null) => {
    if (existingVehicle) {
        vehicleForm.value = { ...existingVehicle };
    } else {
        vehicleForm.value = {
            model: '',
            efficiency: null,
            batteryCapacity: null,
            maxRange: null,
            dcChargeType: 'DC콤보',
            acChargeType: 'AC단상 5핀'
        };
    }
    showVehicleModal.value = true;
};

// Lookup spec by model name
const lookupLoading = ref(false);
const lookupStatus = ref({ type: '', message: '' });

const handleLookupSpec = async () => {
    if (!vehicleForm.value.model || vehicleForm.value.model.trim().length < 2) {
        showAlert({
            title: '검색어 짧음',
            message: '검색할 모델명을 2글자 이상 입력해주세요.',
            emoji: '🔍'
        });
        return;
    }

    lookupLoading.value = true;
    lookupStatus.value = { type: '', message: '' };

    try {
        const token = localStorage.getItem('accessToken');
        const response = await axios.get(`http://localhost:8080/vehicles/spec`, {
            params: { model: vehicleForm.value.model },
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (response.data.success && response.data.data) {
            const spec = response.data.data;
            
            // Charging type normalization mapping
            const mapDC = (type) => {
                if (!type) return 'DC콤보';
                if (type.includes('Combo') || type.includes('콤보')) return 'DC콤보';
                if (type.includes('CHAdeMO') || type.includes('차데모')) return 'CHAdeMO';
                if (type.includes('Tesla') || type.includes('NACS') || type.includes('테슬라')) return '테슬라';
                if (type.includes('3상')) return 'AC3상';
                return 'DC콤보';
            };
            
            const mapAC = (type) => {
                if (!type) return 'AC단상 5핀';
                if (type.includes('5핀') || type.includes('Type 1')) return 'AC단상 5핀';
                if (type.includes('7핀') || type.includes('Type 2')) return 'AC단상 7핀';
                if (type.includes('Tesla') || type.includes('NACS') || type.includes('테슬라')) return '테슬라';
                return 'AC단상 5핀';
            };

            vehicleForm.value = {
                ...vehicleForm.value,
                efficiency: spec.efficiency,
                batteryCapacity: spec.batteryCapacity,
                maxRange: spec.maxRange,
                dcChargeType: mapDC(spec.dcChargeType),
                acChargeType: mapAC(spec.acChargeType)
            };
            lookupStatus.value = { type: 'success', message: '💡 차종 정보를 찾았습니다! 상세 스펙이 자동 입력되었습니다.' };
        } else {
            lookupStatus.value = { type: 'error', message: '❌ 검색된 정보가 없습니다. 상세 스펙을 직접 입력해주세요.' };
        }
    } catch (error) {
        console.error("Failed to fetch spec", error);
        lookupStatus.value = { type: 'error', message: '⚠️ 조회 중 오류가 발생했습니다.' };
    } finally {
        lookupLoading.value = false;
    }
};

const handleRegisterVehicle = async () => {
    const token = localStorage.getItem('accessToken');
    if (!token) return;

    try {
        const response = await axios.post('http://localhost:8080/vehicles', vehicleForm.value, {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (response.data.success) {
            const isUpdate = !!vehicle.value;
            showAlert({
                title: isUpdate ? '차량 수정 완료' : '차량 등록 완료',
                message: isUpdate ? '차량 정보가 성공적으로 수정되었습니다.' : '차량이 성공적으로 등록되었습니다.',
                emoji: '🚗'
            });
            vehicle.value = response.data.data;
            showVehicleModal.value = false;
        } else {
            showAlert({
                title: '등록 실패',
                message: response.data.message || '등록에 실패했습니다.',
                emoji: '❌'
            });
        }
    } catch (error) {
        console.error("Vehicle registration failed:", error);
        showAlert({
            title: '오류 발생',
            message: error.response?.data?.message || '차량 등록 중 오류가 발생했습니다.',
            emoji: '⚠️'
        });
    }
};
// Account Editing State
const showEditModal = ref(false);
const isVerified = ref(false);
const verificationPassword = ref('');
const editForm = ref({
    email: '',
    password: ''
});

const openEditModal = () => {
    isVerified.value = false;
    verificationPassword.value = '';
    editForm.value = {
        email: props.user?.email || '',
        password: ''
    };
    showEditModal.value = true;
};

const handleVerifyPassword = async () => {
    const token = localStorage.getItem('accessToken');
    if (!token) return;

    try {
        const response = await axios.post('http://localhost:8080/myinfo/verify-password', 
            { password: verificationPassword.value },
            { headers: { 'Authorization': `Bearer ${token}` } }
        );

        if (response.data.success) {
            isVerified.value = true;
        } else {
            showAlert({
                title: '인증 실패',
                message: '비밀번호가 일치하지 않습니다.',
                emoji: '🔒'
            });
        }
    } catch (error) {
        console.error("Verification failed:", error);
        showAlert({
            title: '인증 오류',
            message: error.response?.data?.message || '인증 중 오류가 발생했습니다.',
            emoji: '⚠️'
        });
    }
};

const handleUpdateInfo = async () => {
    const token = localStorage.getItem('accessToken');
    if (!token) return;

    try {
        // Send email and password (update password ONLY if provided)
        const updateData = {
            email: editForm.value.email
        };
        if (editForm.value.password) {
            updateData.password = editForm.value.password;
        }

        const response = await axios.patch('http://localhost:8080/myinfo', updateData, {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (response.data.success) {
            showAlert({
                title: '수정 완료',
                message: '회원 정보가 수정되었습니다.',
                emoji: '✅'
            });
            emit('update-user', response.data.data);
            showEditModal.value = false;
        } else {
            showAlert({
                title: '수정 실패',
                message: response.data.message || '수정에 실패했습니다.',
                emoji: '❌'
            });
        }
    } catch (error) {
        console.error("Update failed:", error);
        showAlert({
            title: '수정 오류',
            message: error.response?.data?.message || '정보 수정 중 오류가 발생했습니다.',
            emoji: '⚠️'
        });
    }
};

// History Modal State
// History Modal State
const showHistoryModal = ref(false);
const showFavoritesModal = ref(false);
const showCarbonInfoModal = ref(false);

const handleLogout = () => {
    emit('logout');
};

const handleWithdraw = async () => {
    if (!confirm("정말 탈퇴하시겠습니까? 탈퇴 후에는 모든 데이터가 삭제되며 복구할 수 없습니다.")) {
        return;
    }

    const token = localStorage.getItem('accessToken');
    if (!token) return;

    try {
        const response = await axios.delete('http://localhost:8080/myinfo', {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (response.data.success) {
            emit('withdraw');
        } else {
            showAlert({
                title: '탈퇴 실패',
                message: response.data.message || '탈퇴 처리에 실패했습니다.',
                emoji: '❌'
            });
        }
    } catch (error) {
        console.error("Account withdrawal failed:", error);
        showAlert({
            title: '탈퇴 오류',
            message: '회원 탈퇴 중 오류가 발생했습니다.',
            emoji: '⚠️'
        });
    }
};

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

const toggleFavorite = async (stationId) => {
    const token = localStorage.getItem('accessToken');
    if (!token) return;

    try {
        await axios.post(`http://localhost:8080/favorites/${stationId}`, {}, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        // Refresh the list
        const fRes = await axios.get('http://localhost:8080/favorites', {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        favoriteStations.value = fRes.data;
    } catch (error) {
        console.error("Failed to toggle favorite", error);
    }
};

const goToStation = (stationId) => {
    emit('navigate', 'HOME', { stationId });
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
    <div class="mypage-inner">
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
          </div>
        </div>

        <!-- Carbon Saving Card -->
        <div v-if="user?.totalCarbonSaved !== undefined" class="carbon-saving-card">
          <div class="card-top">
            <div class="title-area">
              <span class="leaf-icon">🍃</span>
              <span class="label">누적 CO₂ 감축량</span>
            </div>
            <button class="info-btn" @click="showCarbonInfoModal = true">ⓘ</button>
          </div>
          <div class="card-value">
            <span class="number">{{ (user.totalCarbonSaved || 0).toLocaleString(undefined, {minimumFractionDigits: 1, maximumFractionDigits: 1}) }}</span>
            <span class="unit">kg CO₂</span>
          </div>
        </div>

        <!-- Vehicle Card -->
        <div class="card vehicle-card">
          <div class="card-header">
            <h3>🚗 차량 정보</h3>
            <button class="text-btn" v-if="vehicle" @click="openVehicleModal(vehicle)">수정</button>
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
            <button class="outline-btn" @click="openVehicleModal()">차량 등록하기</button>
          </div>
        </div>
      </div>

      <!-- Right Column: Stats, History, Favorites, Account -->
      <div class="main-column">
        <!-- Charge History -->
        <div class="card history-card">
          <div class="card-header">
            <h3>🕒 충전 기록</h3>
            <button class="text-btn" @click="showHistoryModal = true">전체 보기 ></button>
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
            <button class="text-btn" @click="showFavoritesModal = true">전체 보기 ></button>
          </div>
          <div class="favorites-grid">
            <div v-for="fav in favoriteStations.slice(0, 4)" :key="fav.stationId" class="fav-item" @click="goToStation(fav.stationId)">
              <div class="fav-top">
                <span class="fav-name">{{ fav.stationName }}</span>
                <span class="heart-icon" @click.stop="toggleFavorite(fav.stationId)" style="cursor: pointer;">❤️</span>
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
            <button class="action-item" @click="openEditModal">
              <div class="action-left">
                <span class="icon">👤</span>
                <div class="action-text">
                  <div class="title">회원정보 수정</div>
                  <div class="sub">이메일, 비밀번호 등 회원 정보를 변경하세요</div>
                </div>
              </div>
              <span class="arrow">></span>
            </button>
            <button class="action-item" @click="handleLogout">
              <div class="action-left">
                <span class="icon">🚪</span>
                <div class="action-text">
                  <div class="title">로그아웃</div>
                </div>
              </div>
              <span class="arrow">></span>
            </button>
            <button class="action-item danger" @click="handleWithdraw">
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
</div>

  <!-- Edit Profile Modal -->
  <Transition name="modal">
    <div v-if="showEditModal" class="modal-overlay" @click="showEditModal = false">
      <div class="modal-container" @click.stop>
        <div class="modal-header">
          <h2>회원정보 수정</h2>
          <button class="close-btn" @click="showEditModal = false">&times;</button>
        </div>
        
        <!-- Step 1: Verification -->
        <div v-if="!isVerified" class="verification-step">
          <p class="step-desc">회원 정보 보안을 위해 현재 비밀번호를 입력해주세요.</p>
          <form @submit.prevent="handleVerifyPassword" class="modal-form">
            <div class="form-group">
              <label>현재 비밀번호</label>
              <input type="password" v-model="verificationPassword" required placeholder="비밀번호 입력">
            </div>
            <div class="modal-footer">
              <button type="button" class="cancel-btn" @click="showEditModal = false">취소</button>
              <button type="submit" class="save-btn">확인</button>
            </div>
          </form>
        </div>

        <!-- Step 2: Modification -->
        <div v-else class="modification-step">
          <p class="step-desc">수정할 이메일과 새 비밀번호를 입력해주세요.</p>
          <form @submit.prevent="handleUpdateInfo" class="modal-form">
            <div class="form-group">
              <label>이메일</label>
              <input type="email" v-model="editForm.email" required placeholder="example@email.com">
            </div>
            
            <div class="form-group">
              <label>새 비밀번호</label>
              <input type="password" v-model="editForm.password" placeholder="변경할 비밀번호 (입력 시 변경됨)">
              <p class="form-help">비밀번호를 입력하지 않으면 기존 비밀번호가 유지됩니다.</p>
            </div>

            <div class="modal-footer">
              <button type="button" class="cancel-btn" @click="showEditModal = false">취소</button>
              <button type="submit" class="save-btn">정보 수정</button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </Transition>

  <!-- Vehicle Registration Modal -->
  <Transition name="modal">
    <div v-if="showVehicleModal" class="modal-overlay" @click="showVehicleModal = false">
      <div class="modal-container" @click.stop>
        <div class="modal-header">
          <h2>{{ vehicle ? '차량 정보 수정' : '차량 등록' }}</h2>
          <button class="close-btn" @click="showVehicleModal = false">&times;</button>
        </div>
        
        <p class="step-desc">
          모델명을 입력하시면 기존 데이터를 바탕으로 스펙이 자동 입력됩니다.<br>
          우리 DB에 없는 모델의 경우 상세 스펙을 직접 입력해주세요.
        </p>

        <form @submit.prevent="handleRegisterVehicle" class="modal-form">
          <div class="form-group">
            <label>모델명 (필수)</label>
            <div class="input-with-button">
                <input type="text" v-model="vehicleForm.model" required placeholder="예: 아이오닉 6 롱레인지">
                <button type="button" class="lookup-btn" @click="handleLookupSpec" :disabled="lookupLoading">
                    {{ lookupLoading ? '...' : '스펙 조회' }}
                </button>
            </div>
            <p v-if="lookupStatus.message" class="lookup-msg" :class="lookupStatus.type">
                {{ lookupStatus.message }}
            </p>
          </div>
          
          <div class="form-row">
            <div class="form-group">
              <label>전비 (km/kWh)</label>
              <input type="number" step="any" v-model="vehicleForm.efficiency" placeholder="5.4">
            </div>
            <div class="form-group">
              <label>배터리 용량 (kWh)</label>
              <input type="number" step="any" v-model="vehicleForm.batteryCapacity" placeholder="77.4">
            </div>
          </div>

          <div class="form-group">
            <label>주행 가능 거리 (km)</label>
            <input type="number" step="any" v-model="vehicleForm.maxRange" placeholder="450">
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>급속 충전 타입</label>
              <select v-model="vehicleForm.dcChargeType" class="form-select">
                <option value="DC콤보">DC콤보</option>
                <option value="CHAdeMO">차데모</option>
                <option value="AC3상">AC3상</option>
                <option value="테슬라">테슬라/NACS</option>
              </select>
            </div>
            <div class="form-group">
              <label>완속 충전 타입</label>
              <select v-model="vehicleForm.acChargeType" class="form-select">
                <option value="AC단상 5핀">AC 5핀 (Type 1)</option>
                <option value="AC단상 7핀">AC 7핀 (Type 2)</option>
                <option value="테슬라">테슬라/NACS</option>
              </select>
            </div>
          </div>

          <div class="modal-footer">
            <button type="button" class="cancel-btn" @click="showVehicleModal = false">취소</button>
            <button type="submit" class="save-btn">{{ vehicle ? '수정 완료' : '등록 완료' }}</button>
          </div>
        </form>
      </div>
    </div>
  </Transition>

  <!-- Full History Modal -->
  <Transition name="modal">
    <div v-if="showHistoryModal" class="modal-overlay" @click="showHistoryModal = false">
      <div class="modal-container history-modal" @click.stop>
        <div class="modal-header">
          <h2>전체 충전 기록</h2>
          <button class="close-btn" @click="showHistoryModal = false">&times;</button>
        </div>
        
        <div class="full-history-list">
          <div v-for="record in chargeRecords" :key="record.recordId" class="history-item">
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
            충전 기록이 없습니다.
          </div>
        </div>
        
        <div class="modal-footer">
          <button type="button" class="save-btn" @click="showHistoryModal = false">닫기</button>
        </div>
      </div>
    </div>
  </Transition>

  <!-- Full Favorites Modal -->
  <Transition name="modal">
    <div v-if="showFavoritesModal" class="modal-overlay" @click="showFavoritesModal = false">
      <div class="modal-container history-modal" @click.stop>
        <div class="modal-header">
          <h2>전체 즐겨찾기 목록</h2>
          <button class="close-btn" @click="showFavoritesModal = false">&times;</button>
        </div>
        
        <div class="full-history-list">
          <div v-for="fav in favoriteStations" :key="fav.stationId" class="history-item fav-history-item" @click="goToStation(fav.stationId)">
            <div class="item-left">
              <div class="status-icon favorite">❤️</div>
              <div class="item-info">
                <div class="st-name">{{ fav.stationName }}</div>
                <div class="st-addr">{{ fav.address }}</div>
              </div>
            </div>
            <div class="item-right">
                <span class="heart-icon" @click.stop="toggleFavorite(fav.stationId)" style="cursor: pointer; font-size: 1.2rem;">❤️</span>
            </div>
          </div>
          <div v-if="favoriteStations.length === 0" class="empty-state">
            즐겨찾는 충전소가 없습니다.
          </div>
        </div>
        
        <div class="modal-footer">
          <button type="button" class="save-btn" @click="showFavoritesModal = false">닫기</button>
        </div>
      </div>
    </div>
  </Transition>

  <!-- Carbon Info Modal -->
  <Transition name="modal">
    <div v-if="showCarbonInfoModal" class="modal-overlay" @click="showCarbonInfoModal = false">
      <div class="modal-container carbon-info-modal" @click.stop>
        <div class="modal-header">
          <h2>CO₂ 감축량 계산 공식</h2>
          <button class="close-btn" @click="showCarbonInfoModal = false">&times;</button>
        </div>
        
        <div class="modal-content-scroller">
          <div class="info-section">
            <h4>기본 계산식</h4>
            <div class="formula-box">
              <p>CO₂ 감축량 = (일반 차량 탄소 배출량) - (전기차 탄소 배출량)</p>
              <div class="formula-detail">
                (충전량 × 2.3 kg/L ÷ 15 km/L) - (충전량 × 0.5 kg/kWh)
              </div>
            </div>
            <ul class="info-list">
              <li><strong>전기차 충전</strong>: 1 kWh당 약 0.5 kg CO₂ 배출</li>
              <li><strong>일반 차량</strong>: 1 L당 약 2.3 kg CO₂ 배출 (연비 15km/L 가정)</li>
              <li><strong>나무 1그루</strong>: 연간 약 6.6 kg CO₂ 흡수</li>
            </ul>
          </div>

          <div class="info-section example-section">
            <h4>계산 예시</h4>
            <div class="example-box">
              <p><strong>100 kWh 충전 시:</strong></p>
              <ul>
                <li>전기차 배출량: 100 × 0.5 = 50 kg CO₂</li>
                <li>일반 차량 배출량 (약 1,500km 주행): 100 × 0.153 × 2.3 ≈ 230 kg CO₂</li>
                <li><strong>감축량</strong>: 230 - 50 = 180 kg CO₂</li>
              </ul>
            </div>
          </div>
        </div>
        
        <div class="modal-footer">
          <button type="button" class="save-btn" @click="showCarbonInfoModal = false">확인</button>
        </div>
      </div>
    </div>
  </Transition>
</template>

<style scoped>
/* Modal Styles */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-container {
  background: white;
  width: 90%;
  max-width: 500px;
  border-radius: 20px;
  padding: 2rem;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.modal-header h2 {
  font-size: 1.5rem;
  font-weight: 700;
  margin: 0;
}

.step-desc {
  color: #6b7280;
  font-size: 0.95rem;
  margin-bottom: 1.5rem;
  line-height: 1.5;
}

.close-btn {
  background: none;
  border: none;
  font-size: 2rem;
  color: #9ca3af;
  cursor: pointer;
}

.modal-form {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-group label {
  font-size: 0.9rem;
  font-weight: 600;
  color: #374151;
}

.form-group input, .form-select {
  padding: 0.75rem 1rem;
  border: 1px solid #d1d5db;
  border-radius: 10px;
  font-size: 1rem;
  transition: border-color 0.2s;
  background-color: white;
}

.form-group input:focus, .form-select:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.2);
}

.input-with-button {
  display: flex;
  gap: 0.5rem;
}

.input-with-button input {
  flex: 1;
}

.lookup-btn {
  background-color: #3b82f6;
  color: white;
  border: none;
  border-radius: 8px;
  padding: 0 1rem;
  font-size: 0.9rem;
  font-weight: 600;
  cursor: pointer;
  white-space: nowrap;
  transition: background-color 0.2s;
}

.lookup-btn:hover {
  background-color: #2563eb;
}

.lookup-btn:disabled {
  background-color: #9ca3af;
  cursor: not-allowed;
}

.lookup-msg {
  font-size: 0.85rem;
  margin-top: 0.5rem;
  padding: 0.5rem;
  border-radius: 6px;
}

.lookup-msg.success {
  background-color: #ecfdf5;
  color: #059669;
}

.lookup-msg.error {
  background-color: #fef2f2;
  color: #dc2626;
}

/* History Modal Specific Styles */
.history-modal {
  max-width: 600px;
}

.full-history-list {
  max-height: 400px;
  overflow-y: auto;
  padding-right: 0.5rem;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.full-history-list .history-item {
  padding: 1rem;
  background-color: #f9fafb;
  border-radius: 12px;
  border: 1px solid #f3f4f6;
}

.form-help {
  font-size: 0.8rem;
  color: #6b7280;
  margin: 0;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.modal-footer {
  display: flex;
  gap: 1rem;
  margin-top: 1rem;
}

.cancel-btn {
  flex: 1;
  padding: 0.75rem;
  border: 1px solid #e5e7eb;
  background: white;
  border-radius: 10px;
  font-weight: 600;
  cursor: pointer;
}

.save-btn {
  flex: 2;
  padding: 0.75rem;
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 10px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
}

.save-btn:hover {
  background: #2563eb;
}

/* Transitions */
.modal-enter-active, .modal-leave-active {
  transition: opacity 0.3s ease;
}
.modal-enter-from, .modal-leave-to {
  opacity: 0;
}

.mypage-container {
  width: 100%;
  height: 100%;
  overflow-y: auto;
  padding: 2rem;
  box-sizing: border-box;
  background-color: #f9fafb;
}

.mypage-inner {
  max-width: 1200px;
  margin: 0 auto;
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
  margin-bottom: 0;
}

/* Carbon Saving Card (Enhanced Two-Line Style) */
.carbon-saving-card {
  background: linear-gradient(135deg, #f0fdf4 0%, #f0f9ff 100%);
  border-radius: 16px;
  padding: 1.5rem;
  border: 1px solid #dcfce7;
  display: flex;
  flex-direction: column;
  gap: 1rem;
  transition: all 0.2s;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
}

.carbon-saving-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.08);
}

.carbon-saving-card .card-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.carbon-saving-card .title-area {
  display: flex;
  align-items: center;
  gap: 0.6rem;
}

.carbon-saving-card .leaf-icon {
  font-size: 1.25rem;
}

.carbon-saving-card .label {
  font-size: 1rem;
  font-weight: 600;
  color: #374151;
}

.carbon-saving-card .info-btn {
  background: rgba(16, 185, 129, 0.1);
  border: none;
  color: #10b981;
  width: 26px;
  height: 26px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.2s;
}

.carbon-saving-card .info-btn:hover {
  background: rgba(16, 185, 129, 0.2);
  transform: scale(1.1);
}

.carbon-saving-card .card-value {
  display: flex;
  align-items: baseline;
  gap: 0.5rem;
}

.carbon-saving-card .number {
  font-size: 2.25rem;
  font-weight: 800;
  color: #059669;
  letter-spacing: -0.02em;
}

.carbon-saving-card .unit {
  font-size: 1.2rem;
  font-weight: 600;
  color: #6b7280;
}

/* Carbon Info Modal Styles */
.carbon-info-modal {
  max-width: 480px;
}

.modal-content-scroller {
  max-height: 500px;
  overflow-y: auto;
  padding-right: 0.5rem;
}

.info-section {
  margin-bottom: 2rem;
}

.info-section h4 {
  font-size: 1.1rem;
  font-weight: 700;
  color: #111827;
  margin: 0 0 1rem 0;
}

.formula-box, .example-box {
  background-color: #f0fdf4;
  border-radius: 12px;
  padding: 1.25rem;
  border: 1px solid #dcfce7;
}

.formula-box p {
  color: #166534;
  font-weight: 600;
  margin: 0 0 0.5rem 0;
  text-align: center;
}

.formula-detail {
  font-size: 0.85rem;
  color: #15803d;
  text-align: center;
  opacity: 0.8;
}

.info-list {
  margin: 1.25rem 0 0 0;
  padding: 0;
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.info-list li {
  font-size: 0.9rem;
  color: #374151;
  display: flex;
  gap: 0.5rem;
}

.info-list li::before {
  content: '•';
  color: #10b981;
}

.example-box {
  background-color: #f9fafb;
  border: 1px solid #f3f4f6;
}

.example-box p {
  color: #374151;
  margin-bottom: 0.75rem;
  text-align: left;
}

.example-box ul {
  margin: 0;
  padding-left: 1.25rem;
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

.example-box li {
  font-size: 0.85rem;
  color: #4b5563;
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

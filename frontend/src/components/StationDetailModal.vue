<script setup>
import { ref, watch, computed, inject } from 'vue';
import axios from 'axios';
import StartChargingModal from './StartChargingModal.vue';
import ChargingModal from './ChargingModal.vue';

const props = defineProps({
  show: Boolean,
  station: {
    type: Object,
    default: null
  },
  user: {
    type: Object,
    default: null
  }
});

const emit = defineEmits(['close', 'status-updated']);

const showAlert = inject('showAlert');

const isFavorite = ref(false);
const isLoggedIn = computed(() => !!props.user);

// Check favorite status when station or login state changes
watch([() => props.station, isLoggedIn], async ([newStation, loggedIn]) => {
    if (loggedIn && newStation && newStation.stationId) {
        try {
            const token = localStorage.getItem('accessToken');
            const response = await axios.get(`http://localhost:8080/favorites/${newStation.stationId}/check`, {
                headers: token ? { 'Authorization': `Bearer ${token}` } : {}
            });
            isFavorite.value = response.data.isFavorite;
        } catch (error) {
            console.error("Failed to check favorite status:", error);
            isFavorite.value = false;
        }
    } else {
        isFavorite.value = false;
    }
}, { immediate: true });

const toggleFavorite = async () => {
    if (!props.station) return;
    
    if (!isLoggedIn.value) {
        showAlert({ title: '로그인 필요', message: "로그인이 필요한 기능입니다.", emoji: '🔒' });
        return;
    }

    try {
        const token = localStorage.getItem('accessToken');
        const response = await axios.post(`http://localhost:8080/favorites/${props.station.stationId}`, {}, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        
        isFavorite.value = response.data.isFavorite;
        showAlert({ title: '즐겨찾기', message: response.data.message, emoji: '⭐' });
    } catch (error) {
        console.error("Failed to toggle favorite:", error);
        showAlert({ title: '오류', message: "즐겨찾기 처리 중 오류가 발생했습니다.", emoji: '⚠️' });
    }
};

// Helper to count available/total chargers by type
const getChargerCount = (chargers, type) => {
    if(!chargers) return '0/0';
    const total = chargers.filter(c => c.chargeType && c.chargeType.includes(type)).length;
    // Check for various 'available' status codes or strings
    const available = chargers.filter(c => {
        if (!c.chargeType || !c.chargeType.includes(type)) return false;
        const s = String(c.status).toUpperCase();
        return s === '0' || s === '2' || s === 'AVAILABLE' || s === 'WAIT'; // 0 or 2: Available
    }).length;
    return `${available}/${total}`;
};

// Mock price since it's missing in DTO
const getStatusColor = (status) => {
    // Backend might return "AVAILABLE", "CHARGING", "WAIT" or numbers "1", "2"...
    // Ensure we check all string variants
    const s = String(status).toUpperCase();
    if (s === '3' || s === 'CHARGING') return '#f97316'; // Orange (Charging)
    if (s === '0' || s === '2' || s === 'AVAILABLE' || s === 'WAIT') return '#2563eb'; // Blue (Available)
    return '#9ca3af'; // Gray (Unavailable/Unknown)
};

const getStatusText = (status) => {
     const s = String(status).toUpperCase();
     if (s === '3' || s === 'CHARGING') return '충전중';
     if (s === '0' || s === '2' || s === 'AVAILABLE' || s === 'WAIT') return '사용 가능';
     return '사용 불가';
};

// Charging Logic
const showStartModal = ref(false);
const showStopModal = ref(false);
const isUserCharging = computed(() => props.user && props.user.status === 'CHARGING');
const activeStationId = ref(null);

watch(() => props.show, async (newVal) => {
    if (newVal && props.user && props.user.status === 'CHARGING') {
        try {
            const token = localStorage.getItem('accessToken');
            const response = await axios.get('http://localhost:8080/charge-records/me', {
                headers: { 'Authorization': `Bearer ${token}` }
            });
            const activeRecord = response.data.find(r => r.status === 'CHARGING');
            if (activeRecord) {
                activeStationId.value = activeRecord.stationId;
            } else {
                activeStationId.value = null;
            }
        } catch (error) {
            console.error("Failed to fetch active record:", error);
            activeStationId.value = null;
        }
    } else if (!newVal) {
        activeStationId.value = null;
    }
}, { immediate: true });

const handleStartChargeClick = async () => {
    if (!props.user) {
        showAlert({ title: '로그인 필요', message: "로그인 시에만 누를 수 있습니다.", emoji: '🔒' });
        return;
    }

    if (isUserCharging.value) {
        showAlert({ title: '충전 불가', message: "이미 다른 충전소에서 충전 중입니다. 무조건 한 사람당 하나의 충전만 진행할 수 있습니다.", emoji: '🚫' });
        return;
    }

    // Geolocation Check
    if (!navigator.geolocation) {
        showAlert({ title: '위치 오류', message: "위치 정보를 사용할 수 없습니다.", emoji: '📍' });
        return;
    }

    navigator.geolocation.getCurrentPosition(
        async (position) => {
            const { latitude, longitude } = position.coords;
            
            // Step 3-1 & 3-2: Location comparison
            const distance = calculateDistance(
                latitude, 
                longitude, 
                props.station.lat, 
                props.station.lng
            );

            // 5km threshold for testing/usage convenience
            console.log("=== Debug Location ===");
            console.log("User Pos:", latitude, longitude);
            console.log("Station Pos:", props.station.lat, props.station.lng);
            console.log("Calculated Distance:", distance.toFixed(2), "m");

            if (distance > 5000) {
                showAlert({ 
                    title: '위치 불일치', 
                    message: `현재 위치가 선택한 충전소가 아닙니다. (계산된 거리: ${distance.toFixed(1)}m). 다시 확인해 주세요.`,
                    emoji: '📍'
                });
                return; // Return to step 1 (keep modal open but stop flow)
            }
            
            // 4. Open Input Form
            showStartModal.value = true;
        },
        (error) => {
            showAlert({ title: '위치 오류', message: "현재 위치를 가져올 수 없습니다. 위치 권한을 확인해주세요.", emoji: '📍' });
        }
    );
};

// Helper to calculate distance in meters
const calculateDistance = (lat1, lon1, lat2, lon2) => {
    const R = 6371e3; // metres
    const φ1 = lat1 * Math.PI/180;
    const φ2 = lat2 * Math.PI/180;
    const Δφ = (lat2-lat1) * Math.PI/180;
    const Δλ = (lon2-lon1) * Math.PI/180;

    const a = Math.sin(Δφ/2) * Math.sin(Δφ/2) +
          Math.cos(φ1) * Math.cos(φ2) *
          Math.sin(Δλ/2) * Math.sin(Δλ/2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

    return R * c; // in metres
};

const submitStartCharge = async (formData) => {
    try {
        const token = localStorage.getItem('accessToken');
        const pos = await new Promise((resolve, reject) => {
            navigator.geolocation.getCurrentPosition(resolve, reject);
        });

        const response = await axios.post('http://localhost:8080/charge-records/start', {
            stationId: props.station.stationId,
            userLatitude: pos.coords.latitude,
            userLongitude: pos.coords.longitude,
            targetKwh: formData.targetKwh,
            startKwh: formData.startKwh,
            chargerCapacity: formData.chargerCapacity
        }, {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        showAlert({ title: '충전 시작!', message: "충전이 시작되었습니다. 안전한 충전 되세요!", emoji: '⚡' });
        showStartModal.value = false;
        // Update user status globally
        emit('status-updated', 'CHARGING');
    } catch (error) {
        console.error("Failed to start charge:", error);
        if (error.response && error.response.status === 401) {
            showAlert({ title: '인증 만료', message: "로그인 세션이 만료되었습니다. 다시 로그인해주세요.", emoji: '🔑' });
        } else if (error.response && error.response.data) {
            showAlert({ title: '충전 시작 실패', message: error.response.data.message || error.response.data || "충전 시작에 실패했습니다.", emoji: '⚠️' });
        } else {
            showAlert({ title: '서버 오류', message: "서버 오류가 발생했습니다.", emoji: '⚠️' });
        }
    }
};

const handleStopChargingRequest = () => {
    showStopModal.value = true;
};
</script>

<template>
  <div v-if="show && station" class="modal-overlay" @click.self="$emit('close')">
    <div class="detail-card">
      <div class="card-header">
        <button class="icon-btn back-btn" @click="$emit('close')">
            <span class="arrow-icon">←</span>
        </button>
        <h2 class="station-name">{{ station.stationName }}</h2>
        <button class="icon-btn star-btn" @click="toggleFavorite">
             <span class="star-icon" :class="{ favorited: isFavorite }">
                {{ isFavorite ? '★' : '☆' }}
             </span>
        </button>
      </div>

      <div class="scroll-content">
        <!-- Info Section -->
        <div class="info-section">
            <h1 class="main-title">{{ station.stationName }}</h1>
            <p class="address">{{ station.address }} <span class="copy-icon">📋</span></p>
            
            <div class="summary-box">
                <div class="summary-item">
                    <span class="bolt-icon blue">⚡</span>
                    <span class="label">완속</span>
                    <span class="count blue">{{ getChargerCount(station.chargers, '완속') }}</span>
                </div>
                <div class="divider">|</div>
                <div class="summary-item">
                    <span class="bolt-icon blue">⚡</span>
                    <span class="label">급속</span>
                    <span class="count blue">{{ getChargerCount(station.chargers, '급속') }}</span>
                </div>
            </div>

            <div class="station-actions">
                <button 
                  v-if="!isUserCharging"
                  class="charge-btn-full start"
                  @click="handleStartChargeClick()"
                >
                  충전 시작
                </button>
                <button 
                  v-else-if="isUserCharging"
                  class="charge-btn-full"
                  :class="station.stationId === activeStationId ? 'stop' : 'disabled'"
                  :disabled="station.stationId !== activeStationId"
                  @click="handleStopChargingRequest"
                >
                  {{ station.stationId === activeStationId ? '충전 중 (종료하기)' : '이미 다른 곳에서 충전 중입니다' }}
                </button>
            </div>
        </div>

        <div class="section-divider"></div>

        <!-- Charger List -->
        <div class="charger-list-section">
            <div class="list-header">
                <h3>충전기 정보</h3>
            </div>

            <div v-for="charger in station.chargers" :key="charger.chargerId" class="charger-card">
                <div class="charger-top">
                    <div class="charger-name">{{ charger.chargerName }}</div>
                    <div class="charger-status" :style="{ color: getStatusColor(charger.status) }">
                        {{ charger.statusLabel || getStatusText(charger.status) }} 
                    </div>
                </div>
                
                <div class="charger-id">
                    충전기 ID {{ charger.chargerId }}
                </div>

                <div class="connector-types">
                    <!-- Hardcoded icons for now -->
                     <div class="connector active">
                        <div class="conn-icon">🔌</div>
                        <span>{{ charger.chargeType || '충전타입' }}</span>
                    </div>
                 </div>
            </div>
        </div>
      </div>

      <!-- Internal Modals -->
      <StartChargingModal 
        :show="showStartModal" 
        :station-name="station.stationName"
        @close="showStartModal = false"
        @start="submitStartCharge"
      />

      <ChargingModal 
        :show="showStopModal"
        @close="showStopModal = false"
        @analyze="showStopModal = false; emit('status-updated', 'ACTIVE')"
      />
    </div>
  </div>
</template>

<style scoped>
.modal-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0,0,0,0.3);
  z-index: 100;
  display: flex;
  justify-content: center;
  align-items: center; /* Center for now, user image looks like full screen mobile or centered card */
}

/* Make it look like the mobile screenshot - likely a floating card or side panel on desktop */
.detail-card {
  background: white;
  width: 100%;
  max-width: 400px; /* Mobile width */
  height: 90%;
  max-height: 800px;
  border-radius: 16px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: 0 10px 25px rgba(0,0,0,0.2);
}

.scroll-content {
    flex: 1;
    overflow-y: auto;
}

.card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 1rem;
    border-bottom: 1px solid #f3f4f6;
}

.station-name {
    font-size: 1rem;
    font-weight: 600;
    color: #1f2937;
    margin: 0;
}

.icon-btn {
    background: none;
    border: none;
    cursor: pointer;
    font-size: 1.25rem;
    padding: 0.25rem;
    color: #6b7280;
}

.star-icon.favorited {
    color: #f59e0b; /* Amber/Yellow for favorited */
}

/* Info Section */
.info-section {
    padding: 1.5rem;
}

.company-name {
    color: #6b7280;
    font-size: 0.9rem;
    margin-bottom: 0.25rem;
}

.main-title {
    font-size: 1.25rem;
    font-weight: 700;
    margin: 0 0 0.5rem 0;
    color: #111827;
}

.address {
    color: #4b5563;
    font-size: 0.95rem;
    margin-bottom: 1rem;
}

.badges {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    margin-bottom: 1.5rem;
}

.badge {
    border: 1px solid #d1d5db;
    padding: 2px 8px;
    border-radius: 9999px;
    font-size: 0.85rem;
    color: #4b5563;
}

.summary-box {
    background-color: #f9fafb;
    border-radius: 12px;
    padding: 1rem;
    display: flex;
    justify-content: space-around;
    align-items: center;
}

.summary-item {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    font-weight: 600;
    font-size: 0.95rem;
}

.blue { color: #2563eb; }

.section-divider {
    height: 8px;
    background-color: #f3f4f6;
}

/* Charger List */
.charger-list-section {
    padding: 1.5rem;
}

.list-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 1rem;
}

.list-header h3 {
    margin: 0;
    font-size: 1.1rem;
    font-weight: 700;
}

.report-btn {
    background: none;
    border: 1px solid #fee2e2;
    color: #ef4444;
    padding: 2px 8px;
    border-radius: 4px;
    font-size: 0.8rem;
    cursor: pointer;
}

.charger-card {
    border: 1px solid #e5e7eb;
    border-radius: 12px;
    padding: 1.25rem;
    margin-bottom: 1rem;
}

.charger-top {
    display: flex;
    justify-content: space-between;
    margin-bottom: 0.5rem;
}

.charger-name {
    font-weight: 700;
    font-size: 1.05rem;
}

.charger-status {
    font-weight: 700;
    font-size: 0.95rem;
}

.charger-id {
    background-color: #f3f4f6;
    display: inline-block;
    padding: 2px 8px;
    border-radius: 4px;
    color: #6b7280;
    font-size: 0.85rem;
    margin-bottom: 1rem;
}

.connector-types {
    display: flex;
    gap: 1.5rem;
    margin-bottom: 1rem;
}

.connector {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 0.25rem;
    color: #9ca3af;
    font-size: 0.8rem;
}

.connector.active {
    color: #2563eb;
    font-weight: 600;
}

.conn-icon {
    font-size: 1.5rem;
    border: 1px solid currentColor;
    border-radius: 50%;
    width: 40px;
    height: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
}

.last-charge {
    text-align: right;
    font-size: 0.8rem;
    color: #9ca3af;
}

.charger-actions {
    margin-top: 1rem;
    display: flex;
    justify-content: flex-end;
}

.charge-btn {
    padding: 0.6rem 1.5rem;
    border-radius: 8px;
    font-weight: 700;
    cursor: pointer;
    border: none;
    transition: all 0.2s;
}

.charge-btn.start {
    background-color: #3b82f6;
    color: white;
}

.charge-btn.start:disabled {
    background-color: #d1d5db;
    cursor: not-allowed;
}

.charge-btn.stop {
    background-color: #16a34a;
    color: white;
}

.station-actions {
    margin-top: 1.5rem;
}

.charge-btn-full {
    width: 100%;
    padding: 0.875rem;
    border-radius: 12px;
    font-size: 1.1rem;
    font-weight: 700;
    cursor: pointer;
    border: none;
    transition: all 0.2s;
}

.charge-btn-full.start {
    background-color: #3b82f6;
    color: white;
}

.charge-btn-full.start:hover {
    background-color: #2563eb;
}

.charge-btn-full.stop {
    background-color: #16a34a;
    color: white;
}

.charge-btn-full.disabled {
    background-color: #d1d5db;
    color: white;
    cursor: not-allowed;
}
</style>

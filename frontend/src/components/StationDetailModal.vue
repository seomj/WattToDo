<script setup>
import { ref, watch } from 'vue';
import axios from 'axios';

const props = defineProps({
  show: Boolean,
  station: {
    type: Object,
    default: null
  }
});

const emit = defineEmits(['close']);

const isFavorite = ref(false);

// Check favorite status when station changes
watch(() => props.station, async (newStation) => {
    if (newStation && newStation.stationId) {
        try {
            const token = localStorage.getItem('accessToken');
            const response = await axios.get(`http://localhost:8080/api/v1/favorites/${newStation.stationId}/check`, {
                headers: token ? { 'Authorization': `Bearer ${token}` } : {}
            });
            isFavorite.value = response.data.isFavorite;
        } catch (error) {
            console.error("Failed to check favorite status:", error);
            isFavorite.value = false;
        }
    }
}, { immediate: true });

const toggleFavorite = async () => {
    if (!props.station) return;
    
    try {
        const token = localStorage.getItem('accessToken');
        if (!token) {
            alert("로그인이 필요한 기능입니다.");
            return;
        }

        const response = await axios.post(`http://localhost:8080/api/v1/favorites/${props.station.stationId}`, {}, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        
        isFavorite.value = response.data.isFavorite;
        alert(response.data.message);
    } catch (error) {
        console.error("Failed to toggle favorite:", error);
        alert("즐겨찾기 처리 중 오류가 발생했습니다.");
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
        return s === '3' || s === 'WAIT' || s === 'AVAILABLE';
    }).length;
    return `${available}/${total}`;
};

// Mock price since it's missing in DTO
const getStatusColor = (status) => {
    // Backend might return "AVAILABLE", "CHARGING", "WAIT" or numbers "1", "2"...
    // Ensure we check all string variants
    const s = String(status).toUpperCase();
    if (s === '2' || s === 'CHARGING') return '#16a34a'; // Green (Charging)
    if (s === '3' || s === 'WAIT' || s === 'AVAILABLE') return '#2563eb'; // Blue (Available)
    return '#9ca3af'; // Gray (Unavailable/Unknown)
};

const getStatusText = (status) => {
     const s = String(status).toUpperCase();
     if (s === '2' || s === 'CHARGING') return '충전중';
     if (s === '3' || s === 'WAIT' || s === 'AVAILABLE') return '사용 가능';
     return '사용 불가';
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
                 
                 <div class="last-charge fa-pull-right">
                     <!-- Date info might be missing in DTO, removing mock date -->
                 </div>
            </div>
        </div>
      </div>
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
</style>

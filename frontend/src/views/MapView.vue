<script setup>
import { ref } from 'vue';
import axios from 'axios';
import KakaoMap from '../components/KakaoMap.vue';
import StationDetailModal from '../components/StationDetailModal.vue';

const searchQuery = ref('');
const showDetailModal = ref(false);
const selectedStation = ref(null);

const handleSearch = () => {
    console.log("Searching for:", searchQuery.value);
    // Future: Implement search functionality
};

const handleMarkerClick = async (stationId) => {
    try {
        console.log("Fetching detail for:", stationId);
        const response = await axios.get(`http://localhost:8080/stations/${stationId}`);
        if(response.data.success) {
            selectedStation.value = response.data.data;
            showDetailModal.value = true;
        } else {
            console.error("Failed to fetch:", response.data.message);
        }
    } catch (error) {
        console.error("Failed to fetch detail:", error);
    }
};

const closeDetail = () => {
    showDetailModal.value = false;
    selectedStation.value = null;
};
</script>

<template>
  <div class="map-view-container">
    <!-- Map Area (Full container) -->
    <div class="map-container">
        
      <!-- Search Bar Overlay -->
      <div class="search-overlay">
          <div class="search-bar">
            <span class="search-icon">🔍</span>
            <input 
                v-model="searchQuery" 
                @keyup.enter="handleSearch"
                placeholder="충전소 검색 / 장소 검색" 
                class="search-input"
            />
            <button @click="handleSearch" class="send-btn">
                ➤
            </button>
          </div>
      </div>

      <!-- The Map -->
      <KakaoMap @marker-click="handleMarkerClick" />

      <!-- Detail Modal Overlay -->
      <StationDetailModal 
          :show="showDetailModal" 
          :station="selectedStation"
          @close="closeDetail"
      />
    </div>
  </div>
</template>

<style scoped>
.map-view-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  width: 100%;
  /* Remove padding to ensure true full width/height */
  padding: 0;
  background-color: white;
}

.map-container {
    width: 100%;
    height: 100%;
    position: relative;
    /* Optional: if you want dashed border at top only */
    border-top: 1px solid #e5e7eb; 
}

/* Search Bar Overlay - Positioned ON TOP of the map */
.search-overlay {
    position: absolute;
    top: 20px;
    left: 50%;
    transform: translateX(-50%);
    width: 90%;
    max-width: 600px;
    z-index: 50; /* Ensure it is above the map */
}

.search-bar {
  display: flex;
  align-items: center;
  background: white;
  width: 100%;
  padding: 0.75rem 1.5rem;
  border-radius: 9999px; /* Pill shape */
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15); /* Stronger shadow for floating effect */
  border: 1px solid #e5e7eb;
  transition: all 0.2s;
}

.search-bar:focus-within {
    box-shadow: 0 4px 16px rgba(59, 130, 246, 0.2);
    border-color: #3b82f6;
}

.search-icon {
  font-size: 1.2rem;
  margin-right: 0.75rem;
  color: #6b7280;
}

.search-input {
  flex: 1;
  border: none;
  font-size: 1rem;
  outline: none;
  color: #374151;
  background: transparent;
}

.search-input::placeholder {
  color: #9ca3af;
}

.send-btn {
  background: none;
  border: none;
  cursor: pointer;
  color: #3b82f6;
  font-size: 1.2rem;
  padding: 0;
  display: flex;
  align-items: center;
}

/* Ensure Map fills container */
:deep(.kakao-map) {
  width: 100% !important;
  height: 100% !important;
}
</style>

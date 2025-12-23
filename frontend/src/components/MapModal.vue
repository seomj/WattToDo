<script setup>
import { onMounted, watch, ref } from 'vue'

const props = defineProps({
  show: Boolean,
  location: {
    type: Object,
    default: () => ({
      name: '그린리프 카페',
      address: '서울시 강남구 강남대로 398',
      lat: 37.498095,
      lng: 127.027610
    })
  }
})

const emit = defineEmits(['close'])
const mapContainer = ref(null)

// Kakao Map load
let map = null;
let marker = null;

const loadMap = () => {
  if (!mapContainer.value || !window.kakao || !window.kakao.maps) return
  
  const options = {
    center: new kakao.maps.LatLng(props.location.lat, props.location.lng),
    level: 3
  };

  // Create map
  map = new kakao.maps.Map(mapContainer.value, options);

  // Create marker
  const markerPosition = new kakao.maps.LatLng(props.location.lat, props.location.lng);
  marker = new kakao.maps.Marker({
    position: markerPosition
  });

  marker.setMap(map);
}

watch(() => props.show, (newVal) => {
  if (newVal) {
    // Small delay to ensure container is ready and rendered
    setTimeout(loadMap, 100)
  }
})
</script>

<template>
  <div v-if="show" class="modal-overlay">
    <div class="modal-content">
      <div class="modal-header">
        <div class="title-area">
          <h2>{{ location.name }}</h2>
          <p class="address">{{ location.address }}</p>
        </div>
        <button class="close-btn" @click="$emit('close')">×</button>
      </div>

      <div class="map-area" ref="mapContainer">
        <!-- Map renders here -->
      </div>

      <div class="detail-row">
        <div class="detail-item">
          <span class="label">거리</span>
          <span class="value">{{ location.distance }}</span>
        </div>
        <div class="detail-item">
          <span class="label">도보</span>
          <span class="value">{{ location.walkTime }}</span>
        </div>
      </div>

      <button class="confirm-btn" @click="$emit('close')">확인</button>
    </div>
  </div>
</template>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 200;
}

.modal-content {
  background-color: white;
  border-radius: 16px;
  width: 90%;
  max-width: 600px;
  overflow: hidden;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.modal-header {
  padding: 1.5rem;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  border-bottom: 1px solid #e5e7eb;
}

h2 {
  font-size: 1.25rem;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 0.25rem 0;
}

.address {
  color: #6b7280;
  font-size: 0.9rem;
  margin: 0;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  color: #9ca3af;
  cursor: pointer;
  padding: 0;
  line-height: 1;
}

.map-area {
  height: 300px;
  background-color: #f3f4f6;
  position: relative;
}

.info-bar {
  position: absolute;
  bottom: 110px; /* Adjust based on layout */
  left: 1.5rem;
}

.info-badge {
  background-color: white;
  padding: 0.5rem 1rem;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.9rem;
  font-weight: 600;
  color: #1f2937;
}

.info-badge .type {
  color: #3b82f6;
}

.detail-row {
  display: flex;
  padding: 1.5rem;
  gap: 2rem;
  background-color: #f9fafb;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.label {
  font-size: 0.8rem;
  color: #6b7280;
}

.value {
  font-weight: 600;
  color: #1f2937;
}

.confirm-btn {
  width: calc(100% - 3rem);
  margin: 0 1.5rem 1.5rem;
  background-color: #0055d4;
  color: white;
  padding: 0.875rem;
  border-radius: 8px;
  border: none;
  font-weight: 600;
  font-size: 1rem;
  cursor: pointer;
}

.confirm-btn:hover {
  background-color: #0044aa;
}
</style>

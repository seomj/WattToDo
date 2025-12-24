<script setup>
import { onMounted, ref, watch } from 'vue';
import axios from 'axios';

const props = defineProps({
  lat: {
    type: Number,
    default: 37.498095
  },
  lng: {
    type: Number,
    default: 127.027610
  }
});

const mapContainer = ref(null);
const mapInstance = ref(null);
const stations = ref([]);
const markers = ref([]);

const KAKAO_KEY = import.meta.env.VITE_KAKAO_MAP_KEY;

const loadKakaoMap = () => {
  if (window.kakao && window.kakao.maps) {
    initMap();
  } else {
    const script = document.createElement('script');
    console.log("Loading Kakao Map with key:", KAKAO_KEY ? "Present" : "Missing");
    script.src = `https://dapi.kakao.com/v2/maps/sdk.js?appkey=${KAKAO_KEY}&autoload=false`;
    script.onload = () => {
      window.kakao.maps.load(() => {
        initMap();
      });
    };
    script.onerror = () => {
      console.error("Kakao Map Script Failed to Load");
      alert("지도를 불러오는데 실패했습니다. API Key가 'JavaScript 키'인지, 그리고 도메인 등록이 되었는지 확인해주세요.");
    };
    document.head.appendChild(script);
  }
};

const initMap = () => {
  const options = {
    center: new window.kakao.maps.LatLng(props.lat, props.lng),
    level: 3
  };
  mapInstance.value = new window.kakao.maps.Map(mapContainer.value, options);
  fetchStations();
  
  // Re-center if props change? Maybe not needed for initial load.
};

const fetchStations = async () => {
  try {
    // Assuming backend is running on localhost:8080
    const response = await axios.get('http://localhost:8080/stations');
    if (response.data && response.data.success) {
      stations.value = response.data.data;
      console.log("Stations loaded:", stations.value.length);
      renderMarkers();
    }
  } catch (error) {
    console.error("Failed to fetch stations:", error);
  }
};

const emit = defineEmits(['marker-click']);

// Marker Images
const markerImages = {
    GREEN: "https://maps.google.com/mapfiles/ms/icons/green-dot.png",
    BLUE: "https://maps.google.com/mapfiles/ms/icons/blue-dot.png",
    GRAY: "https://maps.google.com/mapfiles/ms/icons/red-dot.png", // Using Red as 'Busy/Unavailable' or could find a gray one
    DEFAULT: "https://maps.google.com/mapfiles/ms/icons/blue-dot.png"
};

const renderMarkers = () => {
  // Clear existing markers
  markers.value.forEach(marker => marker.setMap(null));
  markers.value = [];

  stations.value.forEach(station => {
    
    const markerPosition = new window.kakao.maps.LatLng(station.lat, station.lng);
    
    // Determine Marker Image
    let imageSrc = '/markers/marker_gray.svg'; // Default/Gray
    if (station.markerColor === 'GREEN') imageSrc = '/markers/marker_green.svg';
    else if (station.markerColor === 'BLUE') imageSrc = '/markers/marker_blue.svg';
    
    // Create MarkerImage
    const imageSize = new window.kakao.maps.Size(40, 40); 
    const markerImage = new window.kakao.maps.MarkerImage(imageSrc, imageSize);

    // Create Marker with image
    const marker = new window.kakao.maps.Marker({
      position: markerPosition,
      image: markerImage,
      clickable: true // Explicitly enable clicking
    });
    
    marker.setMap(mapInstance.value);
    
    // Add click listener
    window.kakao.maps.event.addListener(marker, 'click', function() {
        console.log("Marker clicked:", station.stationId);
        emit('marker-click', station.stationId);
    });

    markers.value.push(marker);
  });
};

onMounted(() => {
  loadKakaoMap();
});
</script>

<template>
  <div class="map-wrapper">
    <div ref="mapContainer" class="kakao-map"></div>
    
    <!-- Legend UI -->
    <div class="map-legend">
      <div class="legend-item">
        <span class="dot green"></span>
        <span class="label">이용 가능</span>
      </div>
      <div class="legend-item">
        <span class="dot blue"></span>
        <span class="label">일부 이용 가능</span>
      </div>
      <div class="legend-item">
        <span class="dot gray"></span>
        <span class="label">이용 불가</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.map-wrapper {
  position: relative;
  width: 100%;
  height: 100%;
  min-height: 400px;
}
.kakao-map {
  width: 100%;
  height: 100%;
}

/* Legend Styles */
.map-legend {
  position: absolute;
  bottom: 20px;
  left: 20px;
  z-index: 10;
  background: rgba(255, 255, 255, 0.9);
  padding: 10px 15px;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  display: inline-block;
}

.dot.green { background-color: #4CAF50; }
.dot.blue { background-color: #2196F3; }
.dot.gray { background-color: #9E9E9E; }

.label {
  font-size: 0.85rem;
  font-weight: 600;
  color: #333;
}
</style>

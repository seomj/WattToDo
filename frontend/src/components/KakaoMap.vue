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
    
    // Default Marker (No custom image) to ensure clickability
    const marker = new window.kakao.maps.Marker({
      position: markerPosition
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
  </div>
</template>

<style scoped>
.map-wrapper {
  width: 100%;
  height: 100%;
  min-height: 400px;
}
.kakao-map {
  width: 100%;
  height: 100%;
}
</style>

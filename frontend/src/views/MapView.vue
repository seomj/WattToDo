<script setup>
import { computed, ref, watch, onMounted, inject } from 'vue';
import axios from 'axios';
import KakaoMap from '../components/KakaoMap.vue';
import StationDetailModal from '../components/StationDetailModal.vue';

const props = defineProps(['user', 'targetStationId']);

const showDetailModal = ref(false);
const selectedStation = ref(null);
const searchResults = ref([]);
const searchCount = ref(0);


const showAlert = inject('showAlert');

// Search State
const keyword = ref('');
const selectedCity = ref('');
const selectedDistrict = ref('');

// Region Data
const koreaRegions = {
    "서울특별시": ["강남구", "강동구", "강북구", "강서구", "관악구", "광진구", "구로구", "금천구", "노원구", "도봉구", "동대문구", "동작구", "마포구", "서대문구", "서초구", "성동구", "성북구", "송파구", "양천구", "영등포구", "용산구", "은평구", "종로구", "중구", "중랑구"],
    "부산광역시": ["강서구", "금정구", "기장군", "남구", "동구", "동래구", "부산진구", "북구", "사상구", "사하구", "서구", "수영구", "연제구", "영도구", "중구", "해운대구"],
    "대구광역시": ["군위군", "남구", "달서구", "달성군", "동구", "북구", "서구", "수성구", "중구"],
    "인천광역시": ["강화군", "계양구", "남동구", "동구", "미추홀구", "부평구", "서구", "연수구", "옹진군", "중구"],
    "광주광역시": ["광산구", "남구", "동구", "북구", "서구"],
    "대전광역시": ["대덕구", "동구", "서구", "유성구", "중구"],
    "울산광역시": ["남구", "동구", "북구", "울주군", "중구"],
    "세종특별자치시": ["세종특별자치시"],
    "경기도": ["가평군", "고양시", "과천시", "광명시", "광주시", "구리시", "군포시", "김포시", "남양주시", "동두천시", "부천시", "성남시", "수원시", "시흥시", "안산시", "안성시", "안양시", "양주시", "양평군", "여주시", "연천군", "오산시", "용인시", "의왕시", "의정부시", "이천시", "파주시", "평택시", "포천시", "하남시", "화성시"],
    "강원특별자치도": ["강릉시", "고성군", "동해시", "삼척시", "속초시", "양구군", "양양군", "영월군", "원주시", "인제군", "정선군", "철원군", "춘천시", "태백시", "평창군", "홍천군", "화천군", "횡성군"],
    "충청북도": ["괴산군", "단양군", "보은군", "영동군", "옥천군", "음성군", "제천시", "증평군", "진천군", "청주시", "충주시"],
    "충청남도": ["계룡시", "공주시", "금산군", "논산시", "당진시", "보령시", "부여군", "서산시", "서천군", "아산시", "예산군", "천안시", "청양군", "태안군", "홍성군"],
    "전라북도": ["고창군", "군산시", "김제시", "남원시", "무주군", "부안군", "순창군", "완주군", "익산시", "임실군", "장수군", "전주시", "정읍시", "진안군"],
    "전라남도": ["강진군", "고흥군", "곡성군", "광양시", "구례군", "나주시", "담양군", "목포시", "무안군", "보성군", "순천시", "신안군", "여수시", "영광군", "영암군", "완도군", "장성군", "장흥군", "진도군", "함평군", "해남군", "화순군"],
    "경상북도": ["경산시", "경주시", "고령군", "구미시", "군위군", "김천시", "문경시", "봉화군", "상주시", "성주군", "안동시", "영덕군", "영양군", "영주시", "영천시", "예천군", "울릉군", "울진군", "의성군", "청도군", "청송군", "칠곡군", "포항시"],
    "경상남도": ["거제시", "거창군", "고성군", "김해시", "남해군", "밀양시", "사천시", "산청군", "양산시", "의령군", "진주시", "창녕군", "창원시", "통영시", "하동군", "함안군", "함양군", "합천군"],
    "제주특별자치도": ["서귀포시", "제주시"]
};

const sidoOptions = Object.keys(koreaRegions);
const gugunOptions = computed(() => {
    if (!selectedCity.value) return [];
    return koreaRegions[selectedCity.value] || [];
});

const handleSearch = async () => {
    let url = '';
    
    // 1. 키워드 검색 우선
    if (keyword.value.trim()) {
        url = `http://localhost:8080/stations/keyword-search?keyword=${encodeURIComponent(keyword.value.trim())}`;
    } 
    // 2. 지역 선택 검색
    else if (selectedCity.value && selectedDistrict.value) {
        url = `http://localhost:8080/stations/search?city=${selectedCity.value}&district=${selectedDistrict.value}`;
    } else {
        showAlert({ title: '검색 안내', message: "검색어를 입력하거나 지역을 선택해주세요.", emoji: '🔍' });
        return;
    }

    try {
        console.log("Fetching search results from:", url); // Debug Log
        const response = await fetch(url);
        const result = await response.json();
        console.log("Search result:", result); // Debug Log
        if (result.success) {
            searchResults.value = result.data;
            searchCount.value = result.data.length;
        }
    } catch (error) {
        console.error("Search failed:", error);
    }
};

const handleReset = () => {
    keyword.value = '';
    selectedCity.value = '';
    selectedDistrict.value = '';
    searchResults.value = [];
    searchCount.value = 0;
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

// Auto-open station if targetStationId is provided
watch(() => props.targetStationId, (newId) => {
    if (newId) {
        handleMarkerClick(newId);
    }
}, { immediate: true });

const closeDetail = () => {
    showDetailModal.value = false;
    selectedStation.value = null;
};
</script>

<template>
  <div class="map-view-container">
    
    <!-- Left Sidebar -->
    <div class="sidebar">
        <!-- Selector Area -->
        <div class="search-form">
            <!-- Keyword Input -->
            <div class="input-group">
                <input 
                    type="text" 
                    v-model="keyword" 
                    placeholder="충전소명 또는 주소 검색" 
                    class="form-input"
                    @keyup.enter="handleSearch"
                />
            </div>
            
            <div class="select-group">
                <select v-model="selectedCity" class="form-select">
                    <option value="" disabled selected>시/도</option>
                    <option v-for="sido in sidoOptions" :key="sido" :value="sido">
                        {{ sido }}
                    </option>
                </select>
                <select v-model="selectedDistrict" class="form-select" :disabled="!selectedCity">
                    <option value="" disabled selected>시/구/군</option>
                    <option v-for="gugun in gugunOptions" :key="gugun" :value="gugun">
                        {{ gugun }}
                    </option>
                </select>
            </div>
            
            <div class="btn-group">
                <button class="btn btn-reset" @click="handleReset">초기화</button>
                <button class="btn btn-search" @click="handleSearch">검색</button>
            </div>
        </div>

        <!-- Result Count -->
        <div class="result-info" v-if="searchCount > 0">
            검색결과 <span class="highlight">{{ searchCount }}</span> 건
        </div>

        <!-- Result List -->
        <div class="result-list">
            <div 
                v-for="station in searchResults" 
                :key="station.stationId" 
                class="station-item"
                @click="handleMarkerClick(station.stationId)"
            >
                <div class="station-name">{{ station.stationName }}</div>
                <div class="station-status">
                    <span 
                        class="status-dot" 
                        :class="{ 
                            'green': station.availableCount > 0, 
                            'blue': station.availableCount === 0 && station.totalCount > 0,
                            'gray': station.totalCount === 0 
                        }"
                    >●</span>
                    {{ station.availableCount }} / {{ station.totalCount }}
                </div>
            </div>
            <div v-if="searchResults.length === 0" class="empty-state">
                <p>검색 결과가 없습니다.</p>
            </div>
        </div>
    </div>

    <!-- Map Area -->
    <div class="map-container">
      <KakaoMap @marker-click="handleMarkerClick" />
      
      <!-- Detail Modal Overlay -->
      <StationDetailModal 
          :show="showDetailModal" 
          :station="selectedStation"
          :user="user"
          @close="closeDetail"
          @status-updated="(status) => $emit('status-updated', status)"
      />
    </div>
  </div>
</template>

<style scoped>
.map-view-container {
  display: flex;
  flex: 1;
  height: 100%;
  width: 100%;
  overflow: hidden;
}

/* Sidebar Styles */
.sidebar {
    width: 450px; /* Widened Sidebar */
    height: 100%;
    background-color: white;
    border-right: 1px solid #e5e7eb;
    display: flex;
    flex-direction: column;
    z-index: 10;
    box-shadow: 2px 0 10px rgba(0,0,0,0.05);
}

.search-form {
    padding: 1.5rem;
    border-bottom: 1px solid #f3f4f6;
    display: flex;
    flex-direction: column;
    gap: 12px;
}

.input-group {
    width: 100%;
}

.form-input {
    width: 100%;
    box-sizing: border-box; /* Fix: Include padding in width calculation */
    padding: 0.85rem 1rem;
    border: 1px solid #e5e7eb;
    border-radius: 8px;
    font-size: 1rem;
    outline: none;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
    transition: all 0.2s;
}

.form-input:focus {
    border-color: #3b82f6;
    box-shadow: 0 4px 6px rgba(59, 130, 246, 0.1);
}

.select-group {
    display: flex;
    gap: 10px;
}

.form-select {
    flex: 1;
    padding: 0.75rem;
    border: 1px solid #e5e7eb;
    border-radius: 8px;
    font-size: 0.95rem;
    outline: none;
    cursor: pointer;
    background-color: #f9fafb;
}

.form-select:focus {
    border-color: #3b82f6;
}

.btn-group {
    display: flex;
    gap: 10px;
}

.btn {
    flex: 1;
    padding: 0.75rem;
    border-radius: 8px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s;
    border: none;
}

.btn-reset {
    background-color: white;
    border: 1px solid #e5e7eb;
    color: #374151;
}

.btn-reset:hover {
    background-color: #f3f4f6;
}

.btn-search {
    background-color: #004DFF;
    color: white;
}

.btn-search:hover {
    background-color: #003acc;
}

.result-info {
    padding: 1rem 1.5rem;
    font-size: 1rem;
    border-bottom: 1px solid #f3f4f6;
    font-weight: 500;
}

.highlight {
    color: #004DFF;
    font-weight: 700;
}

.result-list {
    flex: 1;
    overflow-y: auto;
    background-color: #fafbfc;
}

.station-item {
    background-color: white;
    padding: 1.25rem 1.5rem;
    border-bottom: 1px solid #f3f4f6;
    cursor: pointer;
    transition: background-color 0.2s;
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.station-item:hover {
    background-color: #f0f4ff;
}

.station-name {
    font-weight: 600;
    font-size: 1rem;
    color: #1f2937;
}

.station-status {
    font-size: 0.9rem;
    color: #6b7280;
    display: flex;
    align-items: center;
    gap: 5px;
    background-color: white;
    padding: 4px 8px;
    border-radius: 12px;
    border: 1px solid #e5e7eb;
}

.status-dot {
    font-size: 0.8rem;
}

.status-dot.green { color: #4CAF50; }
.status-dot.blue { color: #2196F3; }
.status-dot.gray { color: #9E9E9E; }

.empty-state {
    padding: 2rem;
    text-align: center;
    color: #9ca3af;
}

/* Map Area */
.map-container {
    flex: 1;
    height: 100%;
    position: relative;
}

:deep(.kakao-map) {
  width: 100% !important;
  height: 100% !important;
}
</style>

<script setup>
import { ref, onMounted } from 'vue'
import ActivityDetailModal from '../components/ActivityDetailModal.vue'
import MapModal from '../components/MapModal.vue'
import { getRecommendations, getEstimatedTime } from '../api/activity'

const isExpandedSearch = ref(false)
const recommendations = ref([])
const hasSearched = ref(false)
const isLoading = ref(false)
const errorMessage = ref('')

// Filter State
const chargeTime = ref(30)
const isEcoFriendly = ref(false) 
const travelTime = ref(10) // Minutes, 10 min increments
const selectedCategory = ref([])
const usePublicTransport = ref(false)
const personnel = ref(1)
const selectedPurpose = ref([])
const selectedPreference = ref('')

// User Location
const userLocation = ref({ lat: 37.5547, lng: 126.9707 }) // Default: Seoul Station
const isLocating = ref(false)
const locationDisplayName = ref('내 위치 확인 중...')

/**
 * Get current location coordinates as a Promise
 */
const fetchCoordinates = () => {
  return new Promise((resolve, reject) => {
    if (!navigator.geolocation) {
      reject(new Error('Geolocation not supported'))
      return
    }
    
    navigator.geolocation.getCurrentPosition(
      (pos) => resolve({ lat: pos.coords.latitude, lng: pos.coords.longitude }),
      (err) => reject(err),
      { timeout: 10000 }
    )
  })
}

/**
 * Convert coordinates to human-readable address using Kakao Maps
 */
const updateLocationName = (lat, lng) => {
  if (!window.kakao || !window.kakao.maps || !window.kakao.maps.services) {
    locationDisplayName.value = `${lat.toFixed(4)}, ${lng.toFixed(4)}`
    return
  }

  const geocoder = new kakao.maps.services.Geocoder()
  geocoder.coord2Address(lng, lat, (result, status) => {
    if (status === kakao.maps.services.Status.OK) {
      const addr = result[0].address
      locationDisplayName.value = `${addr.region_1depth_name} ${addr.region_2depth_name} ${addr.region_3depth_name} 인근`
    } else {
      locationDisplayName.value = `${lat.toFixed(4)}, ${lng.toFixed(4)}`
    }
  })
}

/**
 * Main function to refresh user location
 */
const refreshLocation = async () => {
  isLocating.value = true
  try {
    const coords = await fetchCoordinates()
    userLocation.value = coords
    updateLocationName(coords.lat, coords.lng)
  } catch (error) {
    console.warn('Geolocation failed:', error)
    locationDisplayName.value = '서울역 (기본 위치)'
    userLocation.value = { lat: 37.5547, lng: 126.9707 }
  } finally {
    isLocating.value = false
  }
}

// Emoji Mapping
const emojiMap = {
  cafe: '☕',
  park: '🌲',
  restaurant: '🍴',
  library: '📚',
  shopping: '🛍️',
  gym: '👟',
  culture: '🎨',
  nature: '🌿',
  walk: '👟',
  store: '🏪'
}

const getEmoji = (key) => emojiMap[key?.toLowerCase()] || '📍'

// Toggle Search Mode
const toggleSearch = () => {
  isExpandedSearch.value = !isExpandedSearch.value
}

// Fetch Recommendations
const handleSearch = async () => {
  // Ensure we have the latest location if it's still default or loading
  if (isLocating.value) {
    errorMessage.value = '위치 정보를 가져오는 중입니다. 잠시 후 다시 시도해주세요.'
    return
  }

  isLoading.value = true
  errorMessage.value = ''
  hasSearched.value = true
  
  try {
    const req = {
      userId: 1, // Mock user ID
      latitude: userLocation.value.lat,
      longitude: userLocation.value.lng,
      chargingTime: chargeTime.value,
      ecoFriendly: isEcoFriendly.value,
      publicTransport: usePublicTransport.value,
      travelTime: travelTime.value,
      personCount: personnel.value,
      purposes: selectedPurpose.value,
      locations: selectedCategory.value,
      preferences: selectedPreference.value
    }

    const response = await getRecommendations(req)
    
    // Map backend response to frontend format
    recommendations.value = response.recommendations.map((item, index) => ({
      id: index,
      name: item.placeName,
      category: item.category,
      icon: getEmoji(item.imageUrl),
      tags: item.isEcoFriendly ? ['친환경'] : [],
      desc: item.description,
      address: item.address,
      distance: `${item.distanceMeter}m`,
      walkTime: `도보 ${item.travelTimeMin}분`,
      lat: item.latitude,
      lng: item.longitude,
      phone: item.phone,
      placeUrl: item.placeUrl
    }))
  } catch (error) {
    console.error('Failed to get recommendations:', error)
    errorMessage.value = error.message
    recommendations.value = []
  } finally {
    isLoading.value = false
  }
}

// Lifecycle Hooks
onMounted(async () => {
  // 1. Get Initial Location
  refreshLocation()

  // 2. Fetch Estimated Charge Time
  try {
    const data = await getEstimatedTime(1) // Mock user ID
    if (data.estimatedTime) {
      chargeTime.value = data.estimatedTime
    }
  } catch (error) {
    console.warn('Failed to fetch estimated charge time.')
  }
})

// Modal State
const selectedActivity = ref(null)
const showDetailModal = ref(false)
const showMapModal = ref(false)

const openDetail = (item) => {
  selectedActivity.value = item
  showDetailModal.value = true
}

const handleOpenMap = () => {
  showDetailModal.value = false
  showMapModal.value = true
}
</script>

<template>
  <div class="activity-container">
    <div class="header-section">
      <h1>✨ AI 기반 추천 활동</h1>
      <p class="subtitle">충전 시간과 선호도에 맞춘 맞춤형 장소를 추천받으세요</p>
    </div>

    <!-- Search Box -->
    <div class="search-card">
      <div class="card-header">
        <span class="card-title">검색 조건</span>
        <button class="toggle-btn" @click="toggleSearch">
          {{ isExpandedSearch ? '접기' : '상세 검색' }}
        </button>
      </div>

      <div class="filter-section">
        <!-- Basic: Charge Time -->
        <div class="filter-group">
          <label>🕒 충전 시간 <span class="required">*</span></label>
          <div class="counter-control">
            <button @click="chargeTime > 5 ? chargeTime -= 5 : null">-</button>
            <div class="input-wrapper">
              <input type="number" v-model="chargeTime" class="time-input" />
              <span>분</span>
            </div>
            <button @click="chargeTime += 5">+</button>
          </div>
        </div>

        <!-- Expanded Filters -->
        <div v-if="isExpandedSearch" class="expanded-filters">
          <div class="filters-grid">
            <!-- Row 1: Toggles & Counters -->
            <div class="filter-row top-controls">
              <div class="filter-group checkbox-group">
                 <label>🌿 친환경 여부</label>
                 <div class="checkbox-wrapper">
                     <label class="checkbox-label">
                       <input type="checkbox" v-model="isEcoFriendly" />
                       포함
                     </label>
                 </div>
              </div>

               <div class="filter-group checkbox-group">
                <label>🚌 대중교통</label>
                <div class="checkbox-wrapper">
                  <label class="checkbox-label">
                    <input type="checkbox" v-model="usePublicTransport" />
                    이용 가능
                  </label>
                </div>
              </div>

              <div class="filter-group">
                <label>⏱️ 이동 시간</label>
                <div class="counter-control">
                  <button @click="travelTime > 10 ? travelTime -= 10 : null">-</button>
                  <div class="input-wrapper">
                    <input type="number" v-model="travelTime" class="time-input" />
                    <span>분</span>
                  </div>
                  <button @click="travelTime += 10">+</button>
                </div>
              </div>

              <div class="filter-group">
                <label>👥 인원</label>
                <div class="counter-control">
                  <button @click="personnel > 1 ? personnel-- : null">-</button>
                  <div class="input-wrapper">
                    <input type="number" v-model="personnel" class="time-input" />
                    <span>명</span>
                  </div>
                  <button @click="personnel++">+</button>
                </div>
              </div>
            </div>

            <!-- Row 2: Purpose -->
            <div class="filter-group full-width">
               <label>🎯 목적</label>
               <div class="chip-group">
                 <button 
                   v-for="purp in ['업무/공부', '휴식', '식사', '운동', '쇼핑', '관광']" 
                   :key="purp"
                   class="chip"
                   :class="{ active: selectedPurpose.includes(purp) }"
                   @click="selectedPurpose.includes(purp) ? selectedPurpose = selectedPurpose.filter(p => p !== purp) : selectedPurpose.push(purp)"
                 >
                   {{ purp }}
                 </button>
               </div>
            </div>
            
            <!-- Row 3: Place -->
            <div class="filter-group full-width">
               <label>🏢 장소</label>
               <div class="chip-group">
                 <button 
                   v-for="cat in ['카페', '편의점', '공원', '산책로', '식당', '쇼핑몰', '서점', '도서관']" 
                   :key="cat"
                   class="chip"
                   :class="{ active: selectedCategory.includes(cat) }"
                   @click="selectedCategory.includes(cat) ? selectedCategory = selectedCategory.filter(c => c !== cat) : selectedCategory.push(cat)"
                 >
                   {{ cat }}
                 </button>
               </div>
            </div>

            <!-- Row 4: Preference -->
            <div class="filter-group full-width">
               <label>✨ 상세 선호도 (직접 입력)</label>
               <input 
                 type="text" 
                 v-model="selectedPreference" 
                 placeholder="예: 조용하고 사람이 적은 곳, 힙한 카페 등"
                 class="preference-input"
               />
            </div>
          </div>
        </div>
      </div>

      <!-- Location Status -->
      <div class="location-status-bar">
        <div class="location-info">
          <span class="loc-icon">📍</span>
          <span class="loc-text" :class="{ gray: isLocating }">
            {{ locationDisplayName }}
          </span>
          <div v-if="isLocating" class="loc-spinner"></div>
        </div>
        <button class="refresh-loc-btn" @click="refreshLocation" :disabled="isLocating">
          위치 갱신
        </button>
      </div>

      <button class="search-btn" @click="handleSearch" :disabled="isLocating || isLoading">
        <template v-if="isLoading">✨ AI가 장소를 찾는 중...</template>
        <template v-else>🔍 AI 추천 받기</template>
      </button>
    </div>

    <!-- Results Section -->
    <div v-if="hasSearched && !isLoading" class="results-section">
      <div v-if="errorMessage" class="error-state">
        <div class="error-icon">⚠️</div>
        <p class="error-message">{{ errorMessage }}</p>
        <button class="retry-btn" @click="handleSearch">다시 시도</button>
      </div>

      <template v-else>
        <div class="results-header">
          <span class="section-title">추천 장소 {{ recommendations.length }}곳</span>
          <span class="info-text">충전 시간 {{ chargeTime }}분 기준</span>
        </div>

        <div v-if="recommendations.length > 0" class="cards-grid">
          <div v-for="item in recommendations" :key="item.id" class="place-card">
            <div class="card-top">
              <div class="icon-area">{{ item.icon }}</div>
              <div class="text-area">
                <div class="place-name">
                  {{ item.name }}
                  <span v-if="item.tags.includes('친환경')" class="eco-badge">🌿</span>
                </div>
                <div class="place-category">{{ item.category }}</div>
              </div>
            </div>
            
            <p class="description">{{ item.desc }}</p>
            
            <div class="meta-info">
              <div class="meta-item">
                <span>📍</span>
                <span>{{ item.distance }}</span>
              </div>
              <span class="dot">•</span>
              <div class="meta-item">
                <span>🕒</span>
                <span>{{ item.walkTime }}</span>
              </div>
            </div>

            <div class="card-footer" @click="openDetail(item)">
              <span>상세 보기</span>
              <span class="arrow">></span>
            </div>
          </div>
        </div>

        <div v-else class="empty-state">
          <div class="empty-icon">🏜️</div>
          <p class="empty-title">추천 장소를 찾지 못했습니다</p>
          <p class="empty-desc">조건을 변경하여 다시 검색해보세요</p>
        </div>
      </template>
    </div>
    
    <!-- Loading State -->
    <div v-else-if="isLoading" class="loading-state">
      <div class="loader"></div>
      <p class="loading-text">AI가 최적의 장소를 찾고 있습니다...</p>
      <p class="loading-desc">잠시만 기다려주세요</p>
    </div>

    <!-- Empty State -->
    <div v-else class="empty-state">
      <div class="empty-icon">✨</div>
      <p class="empty-title">검색 조건을 설정해주세요</p>
      <p class="empty-desc">충전 시간과 선호 조건을 선택하고 'AI 추천 받기'를 클릭하세요</p>
    </div>

    <!-- Modals -->
    <ActivityDetailModal 
      :show="showDetailModal" 
      :activity="selectedActivity"
      @close="showDetailModal = false"
      @openmap="handleOpenMap"
    />
    
    <MapModal
      :show="showMapModal"
      :location="selectedActivity"
      @close="showMapModal = false"
    />
  </div>
</template>

<style scoped>
.activity-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 2rem 1rem;
}

.header-section {
  margin-bottom: 2rem;
}

h1 {
  font-size: 1.75rem;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 0.5rem;
}

.subtitle {
  color: #6b7280;
}

/* Search Card */
.search-card {
  background-color: white;
  border-radius: 16px;
  padding: 1.5rem;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  margin-bottom: 2rem;
  border: 1px solid #e5e7eb;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.card-title {
  font-weight: 600;
  color: #374151;
  font-size: 1.1rem;
}

.toggle-btn {
  background: none;
  border: none;
  color: #3b82f6;
  font-weight: 500;
  cursor: pointer;
}

.filter-section {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.counter-control {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background-color: #f3f4f6;
  padding: 0.25rem 0.5rem;
  border-radius: 9999px; /* Pill shape */
  width: fit-content;
  border: 1px solid #e5e7eb;
}

.counter-control button {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  border: none;
  background-color: white;
  color: #374151;
  font-weight: bold;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 1px 2px rgba(0,0,0,0.1);
  transition: all 0.2s;
}

.counter-control button:hover {
  background-color: #3b82f6;
  color: white;
}

.counter-control button:active {
  transform: scale(0.95);
}

.input-wrapper {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  margin: 0 0.5rem;
}

.time-input {
  width: 44px;
  text-align: center;
  border: none;
  background: transparent;
  padding: 0;
  font-size: 1.1rem;
  font-weight: 700;
  color: #1f2937;
  outline: none;
  /* Hide number arrows */
  -moz-appearance: textfield;
}

.time-input::-webkit-outer-spin-button,
.time-input::-webkit-inner-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

.input-wrapper span {
  font-weight: 500;
  color: #6b7280;
  font-size: 0.95rem;
}

.filter-group label {
  display: block;
  font-weight: 600;
  color: #4b5563;
  margin-bottom: 0.75rem;
  font-size: 0.95rem;
}

.required {
  color: #ef4444;
}

.chip-group {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.chip {
  background-color: #f3f4f6;
  border: 1px solid #e5e7eb;
  padding: 0.5rem 1rem;
  border-radius: 9999px;
  color: #4b5563;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 0.9rem;
}

.chip:hover {
  background-color: #e5e7eb;
}

.chip.active {
  background-color: #0055d4;
  color: white;
  border-color: #0055d4;
}

.preference-input {
  width: 100%;
  padding: 0.75rem 1rem;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  background-color: #f9fafb;
  font-size: 1rem;
  color: #1f2937;
  transition: all 0.2s;
  outline: none;
}

.preference-input:focus {
  border-color: #0055d4;
  background-color: white;
  box-shadow: 0 0 0 3px rgba(0, 85, 212, 0.1);
}

.search-btn {
  width: 100%;
  background-color: #0055d4;
  color: white;
  padding: 1rem;
  border-radius: 12px;
  border: none;
  font-weight: 600;
  font-size: 1.1rem;
  cursor: pointer;
}

.search-btn:hover {
  background-color: #0044aa;
}

/* Results */
.results-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.section-title {
  font-size: 1.1rem;
  font-weight: 600;
  color: #1f2937;
}

.info-text {
  color: #6b7280;
  font-size: 0.9rem;
}

.cards-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 2rem; /* Increased gap to prevent overlap */
}

.place-card {
  background-color: white;
  border: 1px solid #e5e7eb;
  border-radius: 16px;
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  transition: transform 0.2s, box-shadow 0.2s;
}

.place-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
}

.card-top {
  display: flex;
  gap: 1rem;
  margin-bottom: 1rem;
}

.icon-area {
  width: 48px;
  height: 48px;
  background-color: #eff6ff;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  color: #3b82f6;
}

.text-area {
  flex: 1;
}

.place-name {
  font-weight: 700;
  color: #1f2937;
  font-size: 1.1rem;
  margin-bottom: 0.25rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.eco-badge {
  color: #059669;
  font-size: 1rem;
}

.place-category {
  color: #6b7280;
  font-size: 0.9rem;
}

.description {
  color: #4b5563;
  font-size: 0.95rem;
  line-height: 1.5;
  margin-bottom: 1.5rem;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  height: 3rem; /* Fixed height for consistency */
}

.meta-info {
  color: #6b7280;
  font-size: 0.85rem;
  margin-bottom: 1.25rem;
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.dot {
  color: #d1d5db;
}

.card-footer {
  margin-top: auto;
  border-top: 1px solid #f3f4f6;
  padding-top: 1rem;
  display: flex;
  justify-content: flex-end;
  color: #3b82f6;
  font-weight: 600;
  cursor: pointer;
  font-size: 0.95rem;
  transition: all 0.2s;
}

.card-footer:hover {
  color: #2563eb;
  text-decoration: underline;
}

/* Empty State */
.empty-state {
  background-color: white;
  border-radius: 16px;
  padding: 4rem 2rem;
  text-align: center;
  border: 1px solid #e5e7eb;
}

.empty-icon {
  font-size: 3rem;
  color: #d1d5db;
  margin-bottom: 1rem;
}

.empty-title {
  font-size: 1.1rem;
  font-weight: 600;
  color: #374151;
  margin-bottom: 0.5rem;
}

.empty-desc {
  color: #9ca3af;
}

/* Loading State */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem 2rem;
  background-color: white;
  border-radius: 16px;
  border: 1px solid #e5e7eb;
}

.loader {
  width: 48px;
  height: 48px;
  border: 4px solid #f3f4f6;
  border-top: 4px solid #3b82f6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 1.5rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.loading-text {
  font-weight: 600;
  color: #374151;
  font-size: 1.1rem;
  margin-bottom: 0.5rem;
}

.loading-desc {
  color: #9ca3af;
}

/* Error State */
.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem 2rem;
  text-align: center;
}

.error-icon {
  font-size: 2.5rem;
  margin-bottom: 1rem;
}

.error-message {
  color: #ef4444;
  font-weight: 500;
  margin-bottom: 1.5rem;
}

.retry-btn {
  background-color: #f3f4f6;
  border: 1px solid #d1d5db;
  padding: 0.5rem 1.5rem;
  border-radius: 8px;
  color: #374151;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.retry-btn:hover {
  background-color: #e5e7eb;
}

/* Location Status Bar */
.location-status-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.875rem 1rem;
  background-color: #f9fafb;
  border-radius: 12px;
  margin-bottom: 1.5rem;
  border: 1px dashed #d1d5db;
}

.location-info {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.95rem;
  font-weight: 500;
  color: #374151;
}

.loc-text.gray {
  color: #9ca3af;
}

.loc-spinner {
  width: 14px;
  height: 14px;
  border: 2px solid #e5e7eb;
  border-top: 2px solid #3b82f6;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

.refresh-loc-btn {
  background: none;
  border: 1px solid #d1d5db;
  padding: 0.25rem 0.75rem;
  border-radius: 6px;
  font-size: 0.8rem;
  color: #6b7280;
  cursor: pointer;
  background-color: white;
  transition: all 0.1s;
}

.refresh-loc-btn:hover:not(:disabled) {
  background-color: #f3f4f6;
  border-color: #9ca3af;
}

.refresh-loc-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.search-btn:disabled {
  background-color: #9ca3af;
  cursor: not-allowed;
}

/* Responsive */
@media (max-width: 640px) {
  .cards-grid {
    grid-template-columns: 1fr;
  }
}



/* Grid Layout for Detailed Search */
.filters-grid {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.top-controls {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1.5rem;
  padding-bottom: 1.5rem;
  border-bottom: 1px solid #f3f4f6;
}

.filter-group.full-width {
  width: 100%;
}

.checkbox-wrapper {
  height: 40px; /* Match counter height */
  display: flex;
  align-items: center;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-weight: 500;
  color: #374151;
  cursor: pointer;
  font-size: 0.95rem;
}

.checkbox-label input {
  width: 18px;
  height: 18px;
  cursor: pointer;
}
</style>

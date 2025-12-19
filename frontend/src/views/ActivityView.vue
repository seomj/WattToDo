<script setup>
import { ref } from 'vue'
import ActivityDetailModal from '../components/ActivityDetailModal.vue'
import MapModal from '../components/MapModal.vue'

const isExpandedSearch = ref(false)
const recommendations = ref([])
const hasSearched = ref(false)

// Filter State
const chargeTime = ref(30)
const isEcoFriendly = ref(false) 
const travelTime = ref(10) // Minutes, 10 min increments
const selectedCategory = ref([])
const usePublicTransport = ref(false)
const personnel = ref(1)
const selectedPurpose = ref([])
const selectedPreference = ref([])

// Toggle Search Mode
const toggleSearch = () => {
  isExpandedSearch.value = !isExpandedSearch.value
}

// Mock Search Action
const handleSearch = () => {
  hasSearched.value = true
  // Mock Data
  recommendations.value = [
    {
      id: 1,
      name: '그린리프 카페',
      category: '카페',
      icon: '☕',
      tags: ['친환경'],
      desc: '친환경 인테리어와 유기농 원두를 사용하는 조용한 카페입니다. 1인 작업 공간이 잘 마련되어 있습니다.',
      address: '서울시 강남구 강남대로 398',
      distance: '350m',
      walkTime: '도보 5분',
      hours: '08:00 - 22:00',
      phone: '02-1234-5678',
      website: 'www.greenleaf.com',
      lat: 37.498095,
      lng: 127.027610
    },
    {
      id: 2,
      name: '센트럴 공원',
      category: '공원',
      icon: '🌲',
      tags: ['친환경'],
      desc: '도심 속 힐링 공간으로 산책로와 벤치가 잘 갖춰져 있습니다. 나무가 많아 조용히 쉬기 좋습니다.',
      address: '서울시 강남구 테헤란로 152',
      distance: '420m',
      walkTime: '도보 6분',
      hours: '24시간 개방',
      phone: '-',
      website: '-',
      lat: 37.500000,
      lng: 127.030000
    },
    {
      id: 3,
      name: '북앤그린 서점',
      category: '서점',
      icon: '📚',
      tags: ['친환경'],
      desc: '독립 서점으로 환경 도서 큐레이션이 잘되어 있습니다. 조용히 독서할 수 있는 공간이 마련되어 있습니다.',
      address: '서울시 강남구 역삼로 123',
      distance: '280m',
      walkTime: '도보 4분',
      hours: '10:00 - 20:00',
      phone: '02-9876-5432',
      website: 'www.bookngreen.com',
      lat: 37.495000,
      lng: 127.025000
    }
  ]
}

// Modal State
const selectedActivity = ref(null)
const showDetailModal = ref(false)
const showMapModal = ref(false)

const openDetail = (item) => {
  selectedActivity.value = item
  showDetailModal.value = true
}

const handleOpenMap = () => {
  // Use selectedActivity location
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
                   v-for="purp in ['휴식', '식사', '공부', '운동', '쇼핑', '관광']" 
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
               <label>✨ 선호도</label>
               <div class="chip-group">
                 <button 
                   v-for="pref in ['조용한 곳', '사람 적은 곳', '빠르게 다녀올 곳', '넓은 공간', '실내', '실외']" 
                   :key="pref"
                   class="chip"
                   :class="{ active: selectedPreference.includes(pref) }"
                   @click="selectedPreference.includes(pref) ? selectedPreference = selectedPreference.filter(p => p !== pref) : selectedPreference.push(pref)"
                 >
                   {{ pref }}
                 </button>
               </div>
            </div>
          </div>
        </div>
      </div>

      <button class="search-btn" @click="handleSearch">
        🔍 AI 추천 받기
      </button>
    </div>

    <!-- Results Section -->
    <div v-if="hasSearched" class="results-section">
      <div class="results-header">
        <span class="section-title">추천 장소 {{ recommendations.length }}곳</span>
        <span class="info-text">충전 시간 {{ chargeTime }}분 기준</span>
      </div>

      <div class="cards-grid">
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
            <span>📍 {{ item.distance }}</span>
            <span>•</span>
            <span>🕒 {{ item.walkTime }}</span>
          </div>

          <div class="card-footer" @click="openDetail(item)">
            <span>상세 보기</span>
            <span class="arrow">></span>
          </div>
        </div>
      </div>
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
  gap: 1rem;
}

.place-card {
  background-color: white;
  border: 1px solid #e5e7eb;
  border-radius: 16px;
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
  height: 100%;
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
}

.meta-info {
  margin-top: auto;
  color: #6b7280;
  font-size: 0.9rem;
  margin-bottom: 1rem;
  display: flex;
  gap: 0.5rem;
}

.card-footer {
  border-top: 1px solid #f3f4f6;
  padding-top: 1rem;
  display: flex;
  justify-content: space-between;
  color: #3b82f6;
  font-weight: 600;
  cursor: pointer;
  font-size: 0.95rem;
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

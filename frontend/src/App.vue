<script setup>
import { ref } from 'vue'
import Header from './components/Header.vue'
import ChargingModal from './components/ChargingModal.vue'
import AnalysisResultModal from './components/AnalysisResultModal.vue'
import RegistrationCompleteModal from './components/RegistrationCompleteModal.vue'
import ActivityView from './views/ActivityView.vue'

// Routing State
const currentView = ref('HOME') // 'HOME', 'ACTIVITY'

// Mock User State
const user = ref({
  id: 1,
  name: '김민수',
  status: 'CHARGING' // 'CHARGING' or 'IDLE'
})

// Modal State
const showChargingModal = ref(false)
const showResultModal = ref(false)
const showCompleteModal = ref(false)
const chargingStep = ref('confirm') // 'confirm' or 'upload'

const handleOpenChargingModal = () => {
  chargingStep.value = 'confirm'
  showChargingModal.value = true
}

const handleCloseCharging = () => {
  showChargingModal.value = false
}

const handleAnalyze = () => {
  // Triggered manually after file selection
  // Mock analysis delay
  setTimeout(() => {
    showChargingModal.value = false
    showResultModal.value = true
  }, 1000)
}

const handleResultConfirm = () => {
  showResultModal.value = false
  showCompleteModal.value = true
}

const handleResultRetake = () => {
  showResultModal.value = false
  chargingStep.value = 'upload'
  showChargingModal.value = true
}

const handleCompleteConfirm = () => {
  showCompleteModal.value = false
  user.value.status = 'IDLE' // Update status after final confirmation
}
</script>

<template>
  <Header 
    :user="user" 
    :current-view="currentView"
    @open-charging-modal="handleOpenChargingModal" 
    @navigate="(view) => currentView = view"
  />

  <main class="main-content">
    <div v-if="currentView === 'HOME'">
      <div class="search-bar">
      <span class="search-icon">🔍</span>
      <input type="text" placeholder="충전소 검색 / 장소 검색" />
      <button class="location-btn">➤</button>
    </div>
    
    <!-- Placeholder for map or other content -->
    <div class="map-placeholder">
      (Map View Area)
    </div>
    </div>

    <ActivityView v-if="currentView === 'ACTIVITY'" />
  </main>

  <ChargingModal 
    :show="showChargingModal" 
    :initial-step="chargingStep"
    @close="handleCloseCharging"
    @analyze="handleAnalyze"
  />

  <AnalysisResultModal
    :show="showResultModal"
    @confirm="handleResultConfirm"
    @retake="handleResultRetake"
  />

  <RegistrationCompleteModal
    :show="showCompleteModal"
    @confirm="handleCompleteConfirm"
  />
</template>

<style scoped>
.main-content {
  padding: 1rem;
  background-color: #f9fafb;
  min-height: calc(100vh - 64px);
}

.search-bar {
  background-color: white;
  border-radius: 9999px;
  padding: 0.75rem 1.5rem;
  display: flex;
  align-items: center;
  gap: 1rem;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  max-width: 800px;
  margin: 0 auto;
}

.search-icon {
  color: #9ca3af;
}

input {
  flex: 1;
  border: none;
  font-size: 1rem;
  outline: none;
  color: #374151;
}

.location-btn {
  background: none;
  border: none;
  cursor: pointer;
  color: #3b82f6;
  font-size: 1.25rem;
}

.map-placeholder {
  margin-top: 2rem;
  height: 400px;
  border: 2px dashed #e5e7eb;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #9ca3af;
}
</style>

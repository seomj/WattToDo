<script setup>
const props = defineProps({
  show: Boolean,
  result: {
    type: Object,
    default: () => ({
      stationName: '강남역 초고속 충전소',
      chargeKwh: 45,
      durationMin: 24,
      cost: 18900,
      co2Reduction: 12.5
    })
  }
})

const emit = defineEmits(['close', 'confirm', 'retake'])
</script>

<template>
  <div v-if="show" class="modal-overlay">
    <div class="modal-content">
      <div class="icon-circle green">
        ✓
      </div>
      <h2>분석 완료</h2>
      <p class="subtitle">아래 정보가 맞는지 확인해주세요</p>
      
      <div class="result-card">
        <div class="row">
          <span class="label">충전소</span>
          <span class="value">{{ result.stationName }}</span>
        </div>
        <div class="divider"></div>
        <div class="row">
          <span class="label">충전량</span>
          <span class="value action-text">{{ result.chargeKwh }} kWh</span>
        </div>
        <div class="row">
          <span class="label">충전 시간</span>
          <span class="value">{{ result.durationMin }}분</span>
        </div>
        <div class="row">
          <span class="label">충전 비용</span>
          <span class="value action-text">₩{{ result.cost.toLocaleString() }}</span>
        </div>
      </div>

      <div class="button-group">
        <button class="btn btn-outline" @click="$emit('retake')">다시 촬영</button>
        <button class="btn btn-primary" @click="$emit('confirm')">확인</button>
      </div>
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
  z-index: 100;
}

.modal-content {
  background-color: white;
  border-radius: 16px;
  padding: 2rem;
  width: 90%;
  max-width: 400px;
  text-align: center;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.icon-circle {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2rem;
  margin: 0 auto 1rem;
}

.icon-circle.green {
  background-color: #4ade80;
  color: white;
}

h2 {
  font-size: 1.25rem;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 0.5rem;
}

.subtitle {
  color: #6b7280;
  font-size: 0.95rem;
  margin-bottom: 2rem;
}

.result-card {
  background-color: #f9fafb;
  border-radius: 12px;
  padding: 1.5rem;
  margin-bottom: 1.5rem;
  text-align: left;
}

.row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 0.5rem;
}

.row:last-child {
  margin-bottom: 0;
}

.label {
  color: #6b7280;
  font-weight: 500;
}

.value {
  color: #1f2937;
  font-weight: 600;
}

.value.action-text {
  color: #3b82f6;
}

.divider {
  height: 1px;
  background-color: #e5e7eb;
  margin: 1rem 0;
}

.eco-banner {
  background-color: #eff6ff;
  color: #1e40af;
  padding: 0.75rem;
  border-radius: 8px;
  font-size: 0.9rem;
  font-weight: 500;
  margin-bottom: 2rem;
}

.button-group {
  display: flex;
  gap: 1rem;
}

.btn {
  flex: 1;
  padding: 0.75rem;
  border-radius: 8px;
  font-weight: 600;
  font-size: 1rem;
  cursor: pointer;
  border: none;
}

.btn-outline {
  background-color: white;
  border: 1px solid #d1d5db;
  color: #374151;
}

.btn-primary {
  background-color: #3b82f6;
  color: white;
}
</style>

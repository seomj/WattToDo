<script setup>
const props = defineProps({
  show: Boolean,
  co2Amount: {
    type: Number,
    default: 1260
  }
})

const emit = defineEmits(['confirm'])
</script>

<template>
  <div v-if="show" class="modal-overlay">
    <div class="modal-content">
      <div class="icon-circle party">
        🎉
      </div>
      <h2>등록 완료!</h2>
      <p class="subtitle">충전 내역이 정상적으로 등록되었습니다</p>
      
      <div class="info-card">
        <div class="info-title">누적 CO₂ 감축량</div>
        <div class="info-value">{{ co2Amount.toLocaleString() }} kg</div>
        <div class="info-sub">+12.5 kg</div>
      </div>

      <button class="btn btn-primary full-width" @click="$emit('confirm')">확인</button>
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
  padding: 2.5rem 2rem;
  width: 90%;
  max-width: 360px;
  text-align: center;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.icon-circle {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  border: 1px solid #e5e7eb;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2rem;
  margin: 0 auto 1.5rem;
}

.icon-circle.party {
  background-color: #4ade80; /* Green based on image */
  color: white;
  border: none;
}

h2 {
  font-size: 1.5rem;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 0.5rem;
}

.subtitle {
  color: #6b7280;
  font-size: 1rem;
  margin-bottom: 2rem;
}

.info-card {
  background-color: #f0f9ff; /* Light blue background */
  border-radius: 12px;
  padding: 1.5rem;
  margin-bottom: 2rem;
}

.info-title {
  color: #4b5563;
  font-size: 0.9rem;
  margin-bottom: 0.5rem;
}

.info-value {
  color: #1f2937;
  font-size: 1.75rem;
  font-weight: 700;
  margin-bottom: 0.25rem;
}

.info-sub {
  color: #6b7280;
  font-size: 0.85rem;
}

.btn {
  padding: 0.875rem;
  border-radius: 8px;
  font-weight: 600;
  font-size: 1rem;
  cursor: pointer;
  border: none;
  transition: opacity 0.2s;
}

.btn:hover {
  opacity: 0.9;
}

.btn-primary {
  background-color: #0055d4; /* Deep Blue from screenshot */
  color: white;
}

.full-width {
  width: 100%;
}
</style>

<script setup>
import { ref } from 'vue';

const props = defineProps({
  show: Boolean,
  stationName: String
});

const emit = defineEmits(['close', 'start']);

const targetKwh = ref(0);
const startKwh = ref(0);
const chargerCapacity = ref(0);

const handleStart = () => {
    if (targetKwh.value <= 0 || chargerCapacity.value <= 0) {
        alert("올바른 값을 입력해주세요.");
        return;
    }
    emit('start', {
        targetKwh: targetKwh.value,
        startKwh: startKwh.value,
        chargerCapacity: chargerCapacity.value
    });
};
</script>
<template>
  <div v-if="show" class="modal-overlay" @click.self="$emit('close')">
    <div class="modal-content">
      <div class="modal-header">
        <h2>충전 시작</h2>
        <p class="station-label">{{ stationName }}</p>
      </div>

      <div class="form-body">
        <div class="input-group">
          <label>목표 충전량 (kWh)</label>
          <input type="number" v-model="targetKwh" placeholder="예: 40" step="0.1" />
        </div>
        
        <div class="input-group">
          <label>현재 잔여량 (kWh)</label>
          <input type="number" v-model="startKwh" placeholder="예: 10" step="0.1" />
        </div>

        <div class="input-group">
          <label>충전기 용량 (kW)</label>
          <input type="number" v-model="chargerCapacity" placeholder="예: 50" step="0.1" />
        </div>
      </div>

      <div class="button-group">
        <button class="btn btn-outline" @click="$emit('close')">취소</button>
        <button class="btn btn-primary" @click="handleStart">시작하기</button>
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
  z-index: 200;
}

.modal-content {
  background-color: white;
  border-radius: 16px;
  padding: 2rem;
  width: 90%;
  max-width: 400px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.modal-header h2 {
  margin: 0;
  font-size: 1.5rem;
  color: #111827;
}

.station-label {
  color: #6b7280;
  font-size: 0.9rem;
  margin-top: 0.25rem;
  margin-bottom: 1.5rem;
}

.form-body {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
  margin-bottom: 2rem;
}

.input-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.input-group label {
  font-size: 0.9rem;
  font-weight: 600;
  color: #374151;
}

.input-group input {
  padding: 0.75rem;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 1rem;
}

.button-group {
  display: flex;
  gap: 1rem;
}

.btn {
  flex: 1;
  padding: 0.875rem;
  border-radius: 8px;
  font-weight: 600;
  font-size: 1rem;
  cursor: pointer;
  border: none;
  transition: all 0.2s;
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

.btn-primary:hover {
  background-color: #2563eb;
}
</style>

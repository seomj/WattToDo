<script setup>
defineProps({
  show: Boolean,
  title: String,
  message: String,
  stats: {
    type: Object,
    default: null // { label: '누적 CO2 감축량', value: '1,260 kg', subValue: '+12.5 kg' }
  },
  buttonText: {
    type: String,
    default: '확인'
  },
  emoji: {
    type: String,
    default: null
  }
})

defineEmits(['close'])
</script>

<template>
  <Transition name="fade-up">
    <div v-if="show" class="custom-alert-overlay" @click.self="$emit('close')">
      <div class="alert-card">
        <div v-if="emoji" class="icon-wrapper">
          <div class="icon-circle">
            <span class="emoji">{{ emoji }}</span>
          </div>
        </div>

        <h2 class="alert-title">{{ title }}</h2>
        <p class="alert-message">{{ message }}</p>

        <div v-if="stats" class="stats-box">
          <p class="stats-label">{{ stats.label }}</p>
          <p class="stats-value">{{ stats.value }}</p>
          <p v-if="stats.subValue" class="stats-sub">{{ stats.subValue }}</p>
        </div>

        <button class="confirm-btn" @click="$emit('close')">
          {{ buttonText }}
        </button>
      </div>
    </div>
  </Transition>
</template>

<style scoped>
.custom-alert-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background-color: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  backdrop-filter: blur(2px);
}

.alert-card {
  background-color: white;
  border-radius: 24px;
  padding: 2.5rem 2rem 2rem;
  width: 90%;
  max-width: 380px;
  text-align: center;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
  position: relative;
}

.icon-wrapper {
  margin-bottom: 1.5rem;
  display: flex;
  justify-content: center;
}

.icon-circle {
  width: 72px;
  height: 72px;
  background-color: #22c55e;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(34, 197, 94, 0.3);
}

.emoji {
  font-size: 2.5rem;
}

.alert-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: #111827;
  margin-bottom: 0.75rem;
}

.alert-message {
  color: #4b5563;
  font-size: 1.05rem;
  line-height: 1.5;
  margin-bottom: 2rem;
}

.stats-box {
  background-color: #f0fdf4;
  border-radius: 16px;
  padding: 1.5rem;
  margin-bottom: 2rem;
  border: 1px solid #dcfce7;
}

.stats-label {
  font-size: 0.9rem;
  color: #15803d;
  margin-bottom: 0.5rem;
}

.stats-value {
  font-size: 2rem;
  font-weight: 800;
  color: #166534;
  margin-bottom: 0.25rem;
}

.stats-sub {
  font-size: 0.95rem;
  font-weight: 600;
  color: #22c55e;
}

.confirm-btn {
  width: 100%;
  padding: 1rem;
  background-color: #2563eb;
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 1.1rem;
  font-weight: 700;
  cursor: pointer;
  transition: background-color 0.2s;
}

.confirm-btn:hover {
  background-color: #1d4ed8;
}

/* Transitions */
.fade-up-enter-active,
.fade-up-leave-active {
  transition: all 0.3s ease-out;
}

.fade-up-enter-from {
  opacity: 0;
  transform: translateY(20px);
}

.fade-up-leave-to {
  opacity: 0;
  transform: translateY(10px);
}
</style>

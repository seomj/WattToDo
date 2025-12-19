<script setup>
import { computed } from 'vue'

const props = defineProps({
  user: {
    type: Object,
    required: true
  },
  currentView: {
    type: String,
    default: 'HOME'
  }
})

const emit = defineEmits(['open-charging-modal', 'navigate'])

const isCharging = computed(() => props.user.status === 'CHARGING')
</script>

<template>
  <header class="header">
    <div class="logo-area" @click="$emit('navigate', 'HOME')" style="cursor: pointer;">
      <div class="logo-icon">⚡</div>
      <span class="logo-text">WattToDo</span>
    </div>

    <nav class="nav-area">
      <a 
        href="#" 
        class="nav-item" 
        :class="{ active: currentView === 'HOME' }"
        @click.prevent="$emit('navigate', 'HOME')"
      >
        <span class="icon">🏠</span> 홈
      </a>
      <a 
        href="#" 
        class="nav-item"
        :class="{ active: currentView === 'ACTIVITY' }"
        @click.prevent="$emit('navigate', 'ACTIVITY')"
      >
        <span class="icon">🧭</span> 활동
      </a>
      <a href="#" class="nav-item">
        <span class="icon">⚡</span> 마이 그린
      </a>
    </nav>

    <div class="user-area">
      <button 
        v-if="isCharging" 
        class="charging-status-btn"
        @click="$emit('open-charging-modal')"
      >
        ⚡ 충전 중
      </button>
      
      <div class="user-profile">
        <div class="avatar">👤</div>
        <span class="username">{{ user.name }}</span>
      </div>
    </div>
  </header>
</template>

<style scoped>
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 2rem;
  height: 64px;
  background-color: white;
  border-bottom: 1px solid #e5e7eb;
  position: sticky;
  top: 0;
  z-index: 50;
}

.logo-area {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-weight: 700;
  font-size: 1.25rem;
  color: #3b82f6; /* Blue-500 */
}

.logo-icon {
  background-color: #3b82f6;
  color: white;
  width: 28px;
  height: 28px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1rem;
}

.nav-area {
  display: flex;
  align-items: center;
  gap: 2rem;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 0.375rem;
  text-decoration: none;
  color: #6b7280; /* Gray-500 */
  font-size: 0.95rem;
  padding: 0.5rem 0.75rem;
  border-radius: 9999px;
  transition: background-color 0.2s;
}

.nav-item:hover {
  background-color: #f3f4f6;
}

.nav-item.active {
  color: #374151; /* Gray-700 */
  background-color: #f3f4f6;
  font-weight: 600;
  border: 1px solid #e5e7eb;
}

.user-area {
  display: flex;
  align-items: center;
  gap: 1.5rem;
}

.charging-status-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background-color: #65a34e; /* Green-600 approx based on image */
  color: white;
  padding: 0.4rem 1rem;
  border-radius: 9999px;
  font-weight: 600;
  font-size: 0.9rem;
  border: none;
  cursor: pointer;
  transition: opacity 0.2s;
}

.charging-status-btn:hover {
  opacity: 0.9;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.avatar {
  width: 32px;
  height: 32px;
  background-color: #3b82f6;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1rem;
}

.username {
  font-size: 0.95rem;
  font-weight: 500;
  color: #374151;
}
</style>

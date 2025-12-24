<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import Header from './components/Header.vue';
import MapView from './views/MapView.vue';
import ActivityView from './views/ActivityView.vue';
import LoginView from './views/LoginView.vue';
import SignupView from './views/SignupView.vue';
import MyPageView from './views/MyPageView.vue';
import ChargingModal from './components/ChargingModal.vue';
import CustomAlert from './components/CustomAlert.vue';
import axios from 'axios';
import { provide } from 'vue';
import { resetActivityStore } from './stores/activityStore';

const currentView = ref('HOME');
const targetStationId = ref(null);

// User Data (Initially null = not logged in)
const user = ref(null);

const currentComponent = computed(() => {
  switch (currentView.value) {
    case 'HOME': return MapView;
    case 'ACTIVITY': return ActivityView;
    case 'LOGIN': return LoginView;
    case 'SIGNUP': return SignupView;
    case 'MYPAGE': return MyPageView;
    default: return MapView;
  }
});

const handleNavigate = (view, params = null) => {
  currentView.value = view;
  if (params && params.stationId) {
    targetStationId.value = params.stationId;
  } else {
    targetStationId.value = null;
  }
};

const handleLoginSuccess = (userData) => {
    user.value = userData; // Set user data (name, status, etc.)
    currentView.value = 'HOME'; // Redirect home
};

const handleUpdateUser = (updatedData) => {
    // Merge updated info into existing user state
    user.value = { ...user.value, ...updatedData };
};

const handleLogout = () => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    resetActivityStore(); // Reset activity search on logout
    user.value = null;
    window.location.href = '/'; 
};

const handleWithdrawSuccess = () => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    user.value = null;
    alert("회원 탈퇴가 완료되었습니다. 그동안 이용해주셔서 감사합니다.");
    window.location.href = '/';
};

// Security Guard: Redirect to HOME if user logs out while on a protected page
watch(user, (newUser) => {
    if (!newUser && (currentView.value === 'MYPAGE' || currentView.value === 'ACTIVITY')) {
        currentView.value = 'HOME';
    }
});

const handleStatusUpdate = (status) => {
    const previousStatus = user.value?.status;
    if (user.value) {
        user.value.status = status;
    }
    // Reset activity search when charging ends
    if (previousStatus === 'CHARGING' && status !== 'CHARGING') {
        resetActivityStore();
    }
};

const showChargingModal = ref(false);

const alertState = ref({
    show: false,
    title: '',
    message: '',
    stats: null,
    emoji: null
});

const showAlert = (options) => {
    alertState.value = {
        show: true,
        title: options.title || '알림',
        message: options.message || '',
        stats: options.stats || null,
        emoji: options.emoji || null
    };
};

const closeAlert = () => {
    alertState.value.show = false;
};

provide('showAlert', showAlert);

// Restore Session on Mount
onMounted(async () => {
    const token = localStorage.getItem('accessToken');
    if (token) {
        try {
            const response = await axios.get('http://localhost:8080/myinfo', {
                headers: { 'Authorization': `Bearer ${token}` }
            });
            if (response.data.success) {
                user.value = response.data.data;
            }
        } catch (error) {
            console.warn("Session restoration failed:", error);
            localStorage.removeItem('accessToken');
            localStorage.removeItem('refreshToken');
        }
    }
});
</script>

<template>
  <div class="app-container">
    <Header 
      :user="user" 
      :current-view="currentView"
      @navigate="handleNavigate"
      @logout="handleLogout"
      @open-charging-modal="showChargingModal = true"
    />
    <main class="main-body">
      <component 
        :is="currentComponent" 
        :user="user"
        :target-station-id="targetStationId"
        @login-success="handleLoginSuccess"
        @navigate="handleNavigate"
        @logout="handleLogout"
        @withdraw="handleWithdrawSuccess"
        @update-user="handleUpdateUser"
        @status-updated="handleStatusUpdate"
      />
    </main>

    <ChargingModal 
      :show="showChargingModal"
      @close="showChargingModal = false"
      @analyze="showChargingModal = false; handleStatusUpdate('ACTIVE')"
    />

    <CustomAlert 
      :show="alertState.show"
      :title="alertState.title"
      :message="alertState.message"
      :stats="alertState.stats"
      :emoji="alertState.emoji"
      @close="closeAlert"
    />
  </div>
</template>

<style>
/* Global Reset */
body {
  margin: 0;
  padding: 0;
  width: 100vw;
  height: 100vh;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, 'Open Sans', 'Helvetica Neue', sans-serif;
  overflow: hidden; /* Prevent body scroll, let specific views handle it */
}

#app {
  width: 100%;
  height: 100%;
}

.app-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
}

.main-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden; /* Children handle their own overflow */
  background-color: #f9fafb;
}

/* Scrollbar styling for webkit */
::-webkit-scrollbar {
  width: 8px;
}
::-webkit-scrollbar-track {
  background: #f1f1f1; 
}
::-webkit-scrollbar-thumb {
  background: #c1c1c1; 
  border-radius: 4px;
}
::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8; 
}
</style>

<script setup>
import { ref, computed } from 'vue';
import Header from './components/Header.vue';
import MapView from './views/MapView.vue';
import ActivityView from './views/ActivityView.vue';
import LoginView from './views/LoginView.vue';
import SignupView from './views/SignupView.vue';

const currentView = ref('HOME');

// User Data (Initially null = not logged in)
const user = ref(null);

const currentComponent = computed(() => {
  switch (currentView.value) {
    case 'HOME': return MapView;
    case 'ACTIVITY': return ActivityView;
    case 'LOGIN': return LoginView;
    case 'SIGNUP': return SignupView;
    default: return MapView;
  }
});

const handleNavigate = (view) => {
  currentView.value = view;
};

const handleLoginSuccess = (userData) => {
    user.value = userData; // Set user data (name, status, etc.)
    currentView.value = 'HOME'; // Redirect home
};
</script>

<template>
  <div class="app-container">
    <Header 
      :user="user" 
      :current-view="currentView"
      @navigate="handleNavigate"
    />
    <main class="main-body">
      <component 
        :is="currentComponent" 
        @login-success="handleLoginSuccess"
        @navigate="handleNavigate"
      />
    </main>
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
  overflow-y: auto; /* Allow scrolling content */
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

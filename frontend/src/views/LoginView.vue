<script setup>
import { ref } from 'vue';
import axios from 'axios';

const emit = defineEmits(['login-success']);

const email = ref('');
const password = ref('');
const errorMessage = ref('');

const handleLogin = async () => {
    errorMessage.value = '';
    
    if (!email.value || !password.value) {
        errorMessage.value = '이메일과 비밀번호를 입력해주세요.';
        return;
    }

    try {
        const response = await axios.post('http://localhost:8080/auth/login', {
            email: email.value,
            password: password.value
        });

        if (response.data && response.data.accessToken) {
            // Emit success event with token info or just signal success
            // In a real app we'd decode the JWT to get user info, but for this demo 
            // we might mock the user object or fetch profile if an endpoint exists.
            // Since there is no /me endpoint visible yet, we will mock the user object for now 
            // OR we can decode it if we had a library.
            // Let's assume for now we just pass a mock user object on success consistent with "Kim Min-su"
            
            // Checking if we can get user info. The login response has accessToken and name.
            const userData = {
                name: response.data.name || "사용자", // Access name from payload
                email: email.value,
                status: "ACTIVE" // Mock status for now
            };
            
            emit('login-success', userData);
        }
    } catch (error) {
        console.error("Login failed:", error);
        errorMessage.value = error.response?.data?.message || "로그인에 실패했습니다.";
    }
};
</script>

<template>
  <div class="login-container">
    <div class="login-card">
        <h2 class="title">로그인</h2>
        
        <div class="form-group">
            <label>이메일</label>
            <input 
                type="email" 
                v-model="email" 
                placeholder="example@email.com" 
                class="input-field"
                @keyup.enter="handleLogin"
            />
        </div>

        <div class="form-group">
            <label>비밀번호</label>
            <input 
                type="password" 
                v-model="password" 
                placeholder="비밀번호 입력" 
                class="input-field"
                @keyup.enter="handleLogin"
            />
        </div>

        <div v-if="errorMessage" class="error-msg">{{ errorMessage }}</div>

        <button class="btn-login" @click="handleLogin">로그인</button>
    </div>
  </div>
</template>

<style scoped>
.login-container {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 100vh;
    background-color: #f3f4f6;
}

.login-card {
    background: white;
    padding: 2.5rem;
    border-radius: 16px;
    box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
    width: 100%;
    max-width: 400px;
}

.title {
    font-size: 1.5rem;
    font-weight: 700;
    color: #111827;
    margin-bottom: 2rem;
    text-align: center;
}

.form-group {
    margin-bottom: 1.5rem;
}

.form-group label {
    display: block;
    margin-bottom: 0.5rem;
    font-size: 0.9rem;
    font-weight: 600;
    color: #374151;
}

.input-field {
    width: 100%;
    padding: 0.75rem;
    border: 1px solid #d1d5db;
    border-radius: 8px;
    font-size: 1rem;
    outline: none;
    box-sizing: border-box;
    transition: border-color 0.2s;
}

.input-field:focus {
    border-color: #3b82f6;
    border-width: 2px;
}

.btn-login {
    width: 100%;
    padding: 0.85rem;
    background-color: #3b82f6;
    color: white;
    border: none;
    border-radius: 8px;
    font-size: 1rem;
    font-weight: 600;
    cursor: pointer;
    transition: background-color 0.2s;
    margin-top: 0.5rem;
}

.btn-login:hover {
    background-color: #2563eb;
}

.error-msg {
    color: #ef4444;
    font-size: 0.9rem;
    margin-bottom: 1rem;
    text-align: center;
}
</style>

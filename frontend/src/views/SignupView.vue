<script setup>
import { ref, inject } from 'vue';
import axios from 'axios';

const emit = defineEmits(['signup-success', 'navigate']);
const showAlert = inject('showAlert');

const email = ref('');
const password = ref('');
const name = ref('');
const errorMessage = ref('');

const handleSignup = async () => {
    errorMessage.value = '';
    
    if (!email.value || !password.value || !name.value) {
        errorMessage.value = '모든 정보를 입력해주세요.';
        return;
    }

    try {
        await axios.post('http://localhost:8080/auth/signup', {
            email: email.value,
            password: password.value,
            name: name.value
        });
        
        showAlert({
            title: '가입 완료',
            message: '회원가입이 완료되었습니다. 로그인해주세요.',
            emoji: '🎉'
        });
        emit('navigate', 'LOGIN'); // Navigate to login page
        
    } catch (error) {
        console.error("Signup failed:", error);
        if (error.response) {
            errorMessage.value = error.response.data.message || 
                                 `회원가입 실패 (${error.response.status})`;
        } else {
            errorMessage.value = "서버 연결에 실패했습니다.";
        }
        
        showAlert({
            title: '가입 실패',
            message: errorMessage.value,
            emoji: '❌'
        });
    }
};

const goToLogin = () => {
    emit('navigate', 'LOGIN');
};
</script>

<template>
  <div class="signup-container">
    <div class="signup-card">
        <h2 class="title">회원가입</h2>
        
        <div class="form-group">
            <label>이름</label>
            <input 
                type="text" 
                v-model="name" 
                placeholder="홍길동" 
                class="input-field"
            />
        </div>

        <div class="form-group">
            <label>이메일</label>
            <input 
                type="email" 
                v-model="email" 
                placeholder="example@email.com" 
                class="input-field"
            />
        </div>

        <div class="form-group">
            <label>비밀번호</label>
            <input 
                type="password" 
                v-model="password" 
                placeholder="비밀번호 입력" 
                class="input-field"
                @keyup.enter="handleSignup"
            />
        </div>

        <div v-if="errorMessage" class="error-msg">{{ errorMessage }}</div>

        <button class="btn-signup" @click="handleSignup">가입하기</button>
        
        <div class="footer-links">
            <span class="link-text" @click="goToLogin">이미 계정이 있으신가요? 로그인</span>
        </div>
    </div>
  </div>
</template>

<style scoped>
.signup-container {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 100vh;
    background-color: #f3f4f6;
}

.signup-card {
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

.btn-signup {
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

.btn-signup:hover {
    background-color: #2563eb;
}

.error-msg {
    color: #ef4444;
    font-size: 0.9rem;
    margin-bottom: 1rem;
    text-align: center;
}

.footer-links {
    margin-top: 1.5rem;
    text-align: center;
    font-size: 0.9rem;
}

.link-text {
    color: #6b7280;
    cursor: pointer;
    text-decoration: underline;
}

.link-text:hover {
    color: #3b82f6;
}
</style>

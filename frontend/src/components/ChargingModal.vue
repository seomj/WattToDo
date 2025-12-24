<script setup>
import { ref, watch, inject } from 'vue'
import axios from 'axios'

const props = defineProps({
  show: Boolean,
  initialStep: {
    type: String,
    default: 'confirm'
  }
})

const emit = defineEmits(['close', 'analyze'])

const showAlert = inject('showAlert');

const isProcessing = ref(false);
const recordId = ref(null);
const parsedResult = ref(null);
const step = ref('confirm'); // 'confirm', 'upload', 'analyzing', 'result'
const selectedFile = ref(null);

const fetchActiveRecord = async () => {
    try {
        const token = localStorage.getItem('accessToken');
        const response = await axios.get('http://localhost:8080/charge-records/me', {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        const activeRecord = response.data.find(r => r.status === 'CHARGING');
        if (activeRecord) {
            recordId.value = activeRecord.recordId;
        } else {
            showAlert({ title: '알림', message: "활성화된 충전 기록을 찾을 수 없습니다.", emoji: '❓' });
            emit('close');
        }
    } catch (error) {
        console.error("Failed to fetch records:", error);
    }
};

const handleAnalyze = async () => {
    if (!selectedFile.value || !recordId.value) return;

    isProcessing.value = true;
    step.value = 'analyzing';

    try {
        const token = localStorage.getItem('accessToken');
        const formData = new FormData();
        formData.append('file', selectedFile.value);

        // 1. Upload & Parse
        const uploadRes = await axios.post(`http://localhost:8080/charge-records/${recordId.value}/receipt`, formData, {
            headers: { 
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'multipart/form-data'
            }
        });

        if (uploadRes.data.success) {
            parsedResult.value = uploadRes.data.data.parsed;
            // 2. Automatically confirm or show confirmation step? 
            // The request says "충전을 종료하겠냐는 로직으로 넘어가야 해"
            // I'll show the result and then confirm.
            step.value = 'result';
        }
    } catch (error) {
        console.error("Analysis failed:", error);
        showAlert({ title: '분석 실패', message: "이미지 분석에 실패했습니다.", emoji: '❌' });
        step.value = 'upload';
    } finally {
        isProcessing.value = false;
    }
};

const handleConfirm = async () => {
    try {
        const token = localStorage.getItem('accessToken');
        const response = await axios.post(`http://localhost:8080/charge-records/${recordId.value}/confirm`, {
            chargedKwh: parsedResult.value.chargedKwh,
            chargingCost: parsedResult.value.chargingCost,
            durationText: parsedResult.value.durationText
        }, {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (response.data.success) {
            const carbonSaved = response.data.data.carbonSaved || 0;
            const totalCarbonSaved = response.data.data.totalCarbonSaved || 0;
            showAlert({
                title: '등록 완료!',
                message: '충전 내역이 정상적으로 등록되었습니다.',
                stats: {
                    label: '누적 CO₂ 감축량',
                    value: `${totalCarbonSaved.toFixed(1)} kg`,
                    subValue: `+${carbonSaved.toFixed(1)} kg`,
                    emoji: '✅'
                }
            });
        }
        
        emit('analyze'); // This will trigger status update to ACTIVE in App.vue
    } catch (error) {
        console.error("Confirmation failed:", error);
        if (error.response && error.response.status === 401) {
            showAlert({ title: '인증 만료', message: "로그인 세션이 만료되었습니다. 다시 로그인해주세요.", emoji: '🔑' });
        } else {
            showAlert({ title: '오류', message: "충전 종료 처리 중 오류가 발생했습니다.", emoji: '⚠️' });
        }
    }
};

const handleFileChange = (event) => {
  const file = event.target.files[0]
  if (file) {
    selectedFile.value = file
  }
}

const handleForceStop = async () => {
    if (!recordId.value) return;

    try {
        const token = localStorage.getItem('accessToken');
        const response = await axios.delete(`http://localhost:8080/charge-records/${recordId.value}/cancel`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        showAlert({
            title: '충전 취소',
            message: '충전이 취소되었습니다. 충전 기록은 저장되지 않습니다.',
            emoji: '🚫'
        });
        
        emit('analyze'); // This will trigger status update to ACTIVE in App.vue
    } catch (error) {
        console.error("Force stop failed:", error);
        showAlert({ title: '오류', message: "충전 취소 중 오류가 발생했습니다.", emoji: '⚠️' });
    }
};

watch(() => props.show, (newVal) => {
  if (newVal) {
    step.value = props.initialStep
    selectedFile.value = null
    parsedResult.value = null
    fetchActiveRecord();
  }
})
</script>

<template>
  <div v-if="show" class="modal-overlay">
    <div class="modal-content">
      <!-- Step 1: Confirm Stop -->
      <div v-if="step === 'confirm'" class="step-confirm">
        <div class="icon-circle orange">
          ⚡
        </div>
        <h2>충전을 종료하시겠습니까?</h2>
        <p class="subtitle">충전 내역을 인증해주세요</p>
        
        <div class="button-group">
          <button class="btn btn-outline" @click="$emit('close')">취소</button>
          <button class="btn btn-danger" @click="handleForceStop">강제 종료</button>
          <button class="btn btn-primary" @click="step = 'upload'">인증하기</button>
        </div>
      </div>

      <!-- Step 2: Upload Image -->
      <div v-if="step === 'upload'" class="step-upload">
        <h2>충전 내역 인증</h2>
        <p class="subtitle">충전기 화면 또는 영수증을 촬영해주세요</p>

        <div class="upload-area">
          <div class="camera-icon">📷</div>
          <p>이미지 업로드</p>
          <span class="small-text">클릭하여 파일 선택</span>
          <input type="file" @change="handleFileChange" accept="image/*" class="file-input" />
        </div>
        
        <div v-if="selectedFile" class="file-info">
          선택된 파일: {{ selectedFile.name }}
        </div>

        <div class="button-group">
          <button class="btn btn-outline" @click="$emit('close')">취소</button>
          <button 
            class="btn" 
            :class="selectedFile ? 'btn-primary' : 'btn-disabled'"
            :disabled="!selectedFile"
            @click="handleAnalyze"
          >
            {{ isProcessing ? '분석 중...' : '분석하기' }}
          </button>
        </div>
      </div>

      <!-- Step 3: Analyzing -->
      <div v-if="step === 'analyzing'" class="step-analyzing">
        <div class="spinner"></div>
        <p>이미지 분석 중...</p>
      </div>

      <!-- Step 4: Result Confirmation (Editable) -->
      <div v-if="step === 'result' && parsedResult" class="step-result">
        <h2>분석 결과 확인</h2>
        <p class="subtitle">추출된 정보가 맞는지 확인하고 수정해주세요</p>
        
        <div class="result-form">
            <div class="input-group">
                <label>충전량 (kWh)</label>
                <input type="number" v-model.number="parsedResult.chargedKwh" step="0.01" />
            </div>
            <div class="input-group">
                <label>충전 금액 (원)</label>
                <input type="number" v-model.number="parsedResult.chargingCost" />
            </div>
            <div class="input-group">
                <label>충전 시간 (HH:mm)</label>
                <input type="text" v-model="parsedResult.durationText" placeholder="예: 00:30" />
            </div>
        </div>

        <div class="button-group" style="margin-top: 2rem;">
          <button class="btn btn-outline" @click="step = 'upload'">재촬영</button>
          <button class="btn btn-primary" @click="handleConfirm">확인 및 종료</button>
        </div>
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

.icon-circle.orange {
  background-color: #f97316;
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
  background-color: #3b82f6; /* Blue */
  color: white;
}

.btn-danger {
  background-color: #ef4444; /* Red */
  color: white;
}

.btn-danger:hover {
  background-color: #dc2626;
}

.btn-disabled {
  background-color: #d1d5db;
  color: white;
  cursor: not-allowed;
}

.upload-area {
  border: 2px dashed #d1d5db;
  border-radius: 12px;
  padding: 2rem;
  margin-bottom: 2rem;
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
}

.upload-area:hover {
  border-color: #3b82f6;
  background-color: #f9fafb;
}

.file-input {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  opacity: 0;
  cursor: pointer;
}

.camera-icon {
  font-size: 2rem;
  color: #6b7280;
}

.file-info {
  margin-bottom: 1rem;
  color: #3b82f6;
  font-size: 0.9rem;
}

/* Result Form Styles */
.result-form {
    display: flex;
    flex-direction: column;
    gap: 1.25rem;
    text-align: left;
    background-color: #f9fafb;
    padding: 1.5rem;
    border-radius: 12px;
    border: 1px solid #e5e7eb;
}

.input-group {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
}

.input-group label {
    font-size: 0.85rem;
    font-weight: 600;
    color: #6b7280;
}

.input-group input {
    padding: 0.75rem;
    border: 1px solid #d1d5db;
    border-radius: 8px;
    font-size: 1rem;
    color: #111827;
    background-color: white;
}

.input-group input:focus {
    border-color: #3b82f6;
    outline: none;
    box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.spinner {
    width: 40px;
    height: 40px;
    border: 4px solid #f3f3f4;
    border-top: 4px solid #3b82f6;
    border-radius: 50%;
    margin: 0 auto 1rem;
    animation: spin 1s linear infinite;
}

@keyframes spin {
    0% { transform: rotate(0deg); }
    100% { transform: rotate(360deg); }
}
</style>

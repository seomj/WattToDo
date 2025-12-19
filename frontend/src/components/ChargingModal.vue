<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  show: Boolean,
  initialStep: {
    type: String,
    default: 'confirm'
  }
})

const emit = defineEmits(['close', 'analyze'])

const step = ref('confirm') // 'confirm', 'upload', 'analyzing'
const selectedFile = ref(null)

const handleFileChange = (event) => {
  const file = event.target.files[0]
  if (file) {
    selectedFile.value = file
  }
}

watch(() => props.show, (newVal) => {
  if (newVal) {
    step.value = props.initialStep
    selectedFile.value = null
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
            @click="$emit('analyze')"
          >
            분석하기
          </button>
        </div>
      </div>

      <!-- Step 3: Analyzing (Optional visual) -->
      <div v-if="step === 'analyzing'" class="step-analyzing">
        <div class="spinner"></div>
        <p>이미치 분석 중...</p>
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
</style>

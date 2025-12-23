<script setup>
const props = defineProps({
  show: Boolean,
  activity: {
    type: Object,
    default: () => ({
      name: '그린리프 카페',
      category: '카페',
      tags: ['친환경'],
      desc: '친환경 인테리어와 유기농 원두를 사용하는 조용한 카페입니다. 1인 작업 공간이 잘 마련되어 있습니다.',
      address: '서울시 강남구 강남대로 398',
      distance: '350m',
      walkTime: '도보 5분',
      hours: '08:00 - 22:00',
      phone: '02-1234-5678',
      website: 'www.greenleaf.com'
    })
  }
})

const emit = defineEmits(['close', 'openmap'])
</script>

<template>
  <div v-if="show" class="modal-overlay">
    <div class="modal-content">
      <div class="modal-header">
        <div class="title-area">
          <div class="icon-box">☕</div> <!-- Dynamic icon based on category logic could go here -->
          <div>
            <div class="title-row">
              <h2>{{ activity.name }}</h2>
              <span v-for="tag in activity.tags" :key="tag" class="tag">{{ tag }}</span>
            </div>
            <span class="category">{{ activity.category }}</span>
          </div>
        </div>
        <button class="close-btn" @click="$emit('close')">×</button>
      </div>

      <div class="body-content">
        <section class="desc-section">
          <h3>소개</h3>
          <p>{{ activity.desc }}</p>
        </section>

        <div class="info-grid">
          <div class="info-card wide">
            <span class="label">📍 위치</span>
            <div class="value-block">
              <span class="address-text">{{ activity.address }}</span>
              <span class="sub-text">{{ activity.distance }} • {{ activity.walkTime }}</span>
            </div>
          </div>
          <div class="info-card">
            <span class="label">📞 전화번호</span>
            <div class="value-block">
              <span>{{ activity.phone || '정보 없음' }}</span>
            </div>
          </div>
          <div class="info-card">
            <span class="label">🌐 웹사이트</span>
            <div class="value-block">
              <a v-if="activity.placeUrl" :href="activity.placeUrl" target="_blank" class="link">카카오맵에서 보기</a>
              <span v-else>정보 없음</span>
            </div>
          </div>
        </div>
      </div>

      <div class="footer-actions">
        <button class="btn btn-outline" @click="$emit('close')">닫기</button>
        <button class="btn btn-primary" @click="$emit('openmap')">📍 위치 확인</button>
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
  z-index: 150;
}

.modal-content {
  background-color: white;
  border-radius: 16px;
  width: 90%;
  max-width: 600px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
}

.modal-header {
  padding: 1.5rem;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  border-bottom: 1px solid #f3f4f6;
}

.title-area {
  display: flex;
  gap: 1rem;
}

.icon-box {
  width: 48px;
  height: 48px;
  background-color: #eff6ff;
  color: #3b82f6;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
}

.title-row {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.25rem;
}

h2 {
  font-size: 1.25rem;
  font-weight: 700;
  color: #1f2937;
  margin: 0;
}

.tag {
  background-color: #ecfdf5;
  color: #059669;
  font-size: 0.75rem;
  padding: 0.1rem 0.5rem;
  border-radius: 9999px;
  border: 1px solid #a7f3d0;
}

.category {
  color: #6b7280;
  font-size: 0.9rem;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  color: #9ca3af;
  cursor: pointer;
  padding: 0;
  line-height: 1;
}

.body-content {
  padding: 1.5rem;
}

.desc-section {
  margin-bottom: 2rem;
}

h3 {
  font-size: 0.9rem;
  color: #6b7280;
  margin-bottom: 0.5rem;
  font-weight: 500;
}

p {
  color: #374151;
  line-height: 1.5;
  margin: 0;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.info-card {
  background-color: #f9fafb;
  padding: 1rem;
  border-radius: 12px;
}

.info-card.wide {
  grid-column: span 2;
}

.label {
  display: block;
  font-size: 0.85rem;
  color: #6b7280;
  margin-bottom: 0.5rem;
}

.value-block {
  display: flex;
  flex-direction: column;
  gap: 0.2rem;
  font-weight: 500;
  color: #1f2937;
  font-size: 0.95rem;
  word-break: keep-all;
}

.address-text {
  line-height: 1.4;
  color: #1f2937;
}

.sub-text {
  font-size: 0.8rem;
  color: #9ca3af;
}

.link {
  color: #374151;
  text-decoration: none;
}

.footer-actions {
  padding: 1.5rem;
  border-top: 1px solid #f3f4f6;
  display: flex;
  gap: 1rem;
}

.btn {
  flex: 1;
  padding: 0.875rem;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  border: none;
  font-size: 1rem;
}

.btn-outline {
  background-color: white;
  border: 1px solid #d1d5db;
  color: #374151;
}

.btn-primary {
  background-color: #0055d4;
  color: white;
}
</style>

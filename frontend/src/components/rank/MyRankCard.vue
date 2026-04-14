<template>
  <div v-if="rank" class="my-rank-card">
    <div class="my-rank-content">
      <el-avatar :size="64" :src="rank.avatar || defaultAvatar" class="my-avatar">
        {{ avatarFallback }}
      </el-avatar>
      <div class="my-rank-info">
        <div class="my-nickname">{{ rank.nickname || rank.username || '我' }}</div>
        <div class="my-rank-position">
          <span class="rank-label">当前排名</span>
          <span class="rank-number">{{ rank.rank > 0 ? `第 ${rank.rank} 名` : '未上榜' }}</span>
        </div>
        <div class="my-score">
          <span class="score-label">{{ scoreLabel }}</span>
          <span class="score-value">{{ rank.score }}{{ scoreUnit }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  rank: { type: Object, default: null },
  type: { type: String, default: 'points' },
})

const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
const avatarFallback = computed(() => (props.rank?.nickname || props.rank?.username || '我').slice(0, 1))
const scoreLabel = computed(() => props.type === 'points' ? '总积分' : '连续打卡')
const scoreUnit = computed(() => props.type === 'points' ? ' 分' : ' 天')
</script>

<style scoped>
.my-rank-card {
  background: linear-gradient(135deg, var(--ws-primary) 0%, var(--ws-primary-light) 100%);
  border-radius: var(--ws-radius-lg);
  padding: 24px;
  margin-bottom: 24px;
  color: white;
}

.my-rank-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

.my-avatar {
  border: 3px solid rgba(255, 255, 255, 0.3);
}

.my-rank-info {
  flex: 1;
}

.my-nickname {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 8px;
}

.my-rank-position {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 4px;
}

.rank-label {
  font-size: 14px;
  opacity: 0.9;
}

.rank-number {
  font-size: 24px;
  font-weight: 700;
}

.my-score {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.score-label {
  font-size: 14px;
  opacity: 0.9;
}

.score-value {
  font-size: 20px;
  font-weight: 600;
}
</style>

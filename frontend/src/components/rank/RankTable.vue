<template>
  <el-skeleton :rows="10" animated v-if="loading" />
  <template v-else>
    <el-table
      v-if="list.length > 0"
      :data="list"
      stripe
      class="rank-table"
      :row-class-name="getRowClassName"
    >
      <el-table-column label="排名" width="100" align="center">
        <template #default="{ $index }">
          <div class="rank-cell">
            <div v-if="$index === 0" class="rank-badge rank-gold">
              <el-icon><Trophy /></el-icon>
            </div>
            <div v-else-if="$index === 1" class="rank-badge rank-silver">
              <el-icon><Trophy /></el-icon>
            </div>
            <div v-else-if="$index === 2" class="rank-badge rank-bronze">
              <el-icon><Trophy /></el-icon>
            </div>
            <span v-else class="rank-number-plain">{{ $index + 1 }}</span>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="用户" min-width="200">
        <template #default="{ row }">
          <div class="user-cell">
            <el-avatar :size="40" :src="row.avatar || defaultAvatar" />
            <span class="user-nickname">{{ row.nickname || row.username }}</span>
            <el-tag v-if="isCurrentUser(row)" type="primary" size="small" effect="plain">我</el-tag>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="分数" width="150" align="right">
        <template #default="{ row }">
          <span class="score-display">{{ row.score }}{{ scoreUnit }}</span>
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-if="list.length === 0" description="暂无榜单数据" :image-size="120" />
  </template>
</template>

<script setup>
import { computed } from 'vue'
import { Trophy } from '@element-plus/icons-vue'
import { useUserStore } from '../../stores/user'

const props = defineProps({
  list: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  type: { type: String, default: 'points' },
})

const userStore = useUserStore()
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const scoreUnit = computed(() => props.type === 'points' ? ' 分' : ' 天')
const currentUserId = computed(() => userStore.userInfo?.id)

const isCurrentUser = (row) => row.userId === currentUserId.value

const getRowClassName = ({ row }) => isCurrentUser(row) ? 'current-user-row' : ''
</script>

<style scoped>
.rank-table {
  margin-top: 16px;
}

.rank-cell {
  display: flex;
  align-items: center;
  justify-content: center;
}

.rank-badge {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}

.rank-gold {
  background: linear-gradient(135deg, #FFD700 0%, #FFA500 100%);
  color: white;
  box-shadow: 0 2px 8px rgba(255, 215, 0, 0.4);
}

.rank-silver {
  background: linear-gradient(135deg, #C0C0C0 0%, #A0A0A0 100%);
  color: white;
  box-shadow: 0 2px 8px rgba(192, 192, 192, 0.4);
}

.rank-bronze {
  background: linear-gradient(135deg, #CD7F32 0%, #B87333 100%);
  color: white;
  box-shadow: 0 2px 8px rgba(205, 127, 50, 0.4);
}

.rank-number-plain {
  font-size: 16px;
  font-weight: 600;
  color: var(--ws-text-muted);
}

.user-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-nickname {
  font-size: 14px;
  color: var(--ws-text-title);
}

.score-display {
  font-size: 16px;
  font-weight: 600;
  color: var(--ws-primary);
}

:deep(.current-user-row) {
  background-color: var(--ws-primary-lighter) !important;
}

:deep(.current-user-row:hover > td) {
  background-color: var(--ws-primary-lighter) !important;
}

:deep(.el-table__row.current-user-row td) {
  background-color: var(--ws-primary-lighter);
}

@media (max-width: 768px) {
  .rank-table :deep(.el-table) {
    overflow-x: auto;
  }

  .user-cell {
    gap: 8px;
  }

  .rank-badge {
    width: 28px;
    height: 28px;
    font-size: 14px;
  }
}
</style>

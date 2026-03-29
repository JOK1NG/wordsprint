<template>
  <section class="page-section">
    <div class="page-header">
      <div>
        <p class="eyebrow">Rankings</p>
        <h1>排行榜</h1>
        <p class="page-description">看看谁是学习达人，向优秀看齐！</p>
      </div>
    </div>

    <el-card shadow="never" class="rank-card">
      <el-alert
        v-if="errorText"
        :title="errorText"
        type="error"
        :closable="false"
        show-icon
        class="load-error-alert"
      >
        <template #default>
          <el-button link type="primary" @click="loadRankData">重试加载</el-button>
        </template>
      </el-alert>

      <el-tabs v-model="activeTab" @tab-change="handleTabChange" class="rank-tabs">
        <el-tab-pane label="积分榜" name="points" />
        <el-tab-pane label="连续打卡榜" name="streak" />
      </el-tabs>

      <!-- 我的排名卡片 -->
      <div v-if="myRank" class="my-rank-card">
        <div class="my-rank-content">
          <el-avatar :size="64" :src="myRank.avatar || defaultAvatar" class="my-avatar" />
          <div class="my-rank-info">
            <div class="my-nickname">{{ myRank.nickname || myRank.username || '我' }}</div>
            <div class="my-rank-position">
              <span class="rank-label">当前排名</span>
              <span class="rank-number">{{ myRank.rank > 0 ? `第 ${myRank.rank} 名` : '未上榜' }}</span>
            </div>
            <div class="my-score">
              <span class="score-label">{{ scoreLabel }}</span>
              <span class="score-value">{{ myRank.score }}{{ scoreUnit }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 榜单表格 -->
      <el-skeleton :rows="10" animated v-if="loading" />
      <template v-else>
        <el-table
          :data="rankList"
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

        <el-empty
          v-if="rankList.length === 0"
          description="暂无榜单数据"
          :image-size="120"
        />
      </template>
    </el-card>
  </section>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Trophy } from '@element-plus/icons-vue'
import { getPointsRank, getStreakRank } from '../../api/rank'
import { useUserStore } from '../../stores/user'
import { extractErrorMessage } from '../../utils/error'

const userStore = useUserStore()
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const activeTab = ref('points')
const loading = ref(false)
const rankList = ref([])
const myRank = ref(null)
const errorText = ref('')

const scoreLabel = computed(() => {
  return activeTab.value === 'points' ? '总积分' : '连续打卡'
})

const scoreUnit = computed(() => {
  return activeTab.value === 'points' ? ' 分' : ' 天'
})

const currentUserId = computed(() => {
  return userStore.userInfo?.id
})

const isCurrentUser = (row) => {
  return row.userId === currentUserId.value
}

const getRowClassName = ({ row }) => {
  return isCurrentUser(row) ? 'current-user-row' : ''
}

const loadRankData = async () => {
  loading.value = true
  errorText.value = ''
  try {
    let res
    if (activeTab.value === 'points') {
      res = await getPointsRank(20)
    } else {
      res = await getStreakRank(20)
    }

    if (res.code === 200) {
      rankList.value = res.data?.list || []
      // 包装 myRank 为对象格式，兼容后端返回
      if (res.data?.myRank && res.data.myScore !== undefined) {
        myRank.value = {
          rank: res.data.myRank,
          score: res.data.myScore,
          nickname: userStore.userInfo?.nickname,
          avatar: userStore.userInfo?.avatar
        }
      } else {
        myRank.value = null
      }
    } else {
      rankList.value = []
      myRank.value = null
      errorText.value = res.message || '获取榜单失败'
    }
  } catch (error) {
    rankList.value = []
    myRank.value = null
    errorText.value = extractErrorMessage(error, '获取榜单失败')
  } finally {
    loading.value = false
  }
}

const handleTabChange = () => {
  loadRankData()
}

onMounted(() => {
  loadRankData()
})
</script>

<style scoped>
.rank-card {
  min-height: 500px;
}

.rank-tabs {
  margin-bottom: 20px;
}

.load-error-alert {
  margin-bottom: 12px;
}

/* 我的排名卡片 */
.my-rank-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
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

/* 榜单表格 */
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
  color: #606266;
}

.user-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-nickname {
  font-size: 14px;
  color: #303133;
}

.score-display {
  font-size: 16px;
  font-weight: 600;
  color: #409eff;
}

/* 高亮当前用户行 */
:deep(.current-user-row) {
  background-color: #ecf5ff !important;
}

:deep(.current-user-row:hover > td) {
  background-color: #d9ecff !important;
}

:deep(.el-table__row.current-user-row td) {
  background-color: #ecf5ff;
}
</style>

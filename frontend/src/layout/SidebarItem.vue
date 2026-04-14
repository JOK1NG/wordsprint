<template>
  <router-link :to="to" class="sidebar-item">
    <span class="nav-icon">
      <el-icon><component :is="icon" /></el-icon>
    </span>
    <span class="sidebar-item-label">{{ label }}</span>
    <span v-if="badge" class="sidebar-item-badge">{{ badge }}</span>
  </router-link>
</template>

<script setup>
defineProps({
  to: {
    type: String,
    required: true,
  },
  icon: {
    type: [Object, String],
    required: true,
  },
  label: {
    type: String,
    required: true,
  },
  badge: {
    type: [Number, String],
    default: null,
  },
})
</script>

<style scoped>
.sidebar-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-radius: var(--ws-radius-sm);
  color: var(--ws-text-body);
  font-weight: 500;
  font-size: 14px;
  transition: background var(--ws-transition-normal), color var(--ws-transition-normal);
  position: relative;
  text-decoration: none;
}

.sidebar-item:hover {
  background: var(--ws-primary-lighter);
  color: var(--ws-primary);
}

.sidebar-item.router-link-exact-active {
  background: var(--ws-primary-lighter);
  color: var(--ws-primary);
  font-weight: 600;
}

.sidebar-item.router-link-exact-active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 8px;
  bottom: 8px;
  width: 3px;
  border-radius: 0 3px 3px 0;
  background: var(--ws-primary);
}

.nav-icon {
  font-size: 18px;
  width: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.sidebar-item-label {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.sidebar-item-badge {
  min-width: 20px;
  height: 20px;
  padding: 0 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--ws-radius-full);
  background: var(--ws-danger);
  color: #fff;
  font-size: 11px;
  font-weight: 700;
  flex-shrink: 0;
}
</style>

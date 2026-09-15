<script setup lang="ts">
/*
 * Dashboard/Timeline/Goal 3개 화면을 오가야 해서 만든 최소 네비게이션.
 * 각 화면 헤더에 링크를 따로따로 두지 않고 여기서 한 번만 관리한다.
 */
</script>

<template>
  <nav class="app-nav" aria-label="주요 화면 이동">
    <RouterLink to="/dashboard" class="app-nav__link">대시보드</RouterLink>
    <RouterLink to="/timeline" class="app-nav__link">타임라인</RouterLink>
    <RouterLink to="/goals" class="app-nav__link">목표</RouterLink>
  </nav>
</template>

<style scoped>
.app-nav {
  display: flex;
  gap: var(--space-4);
}

.app-nav__link {
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
  text-decoration: none;
  white-space: nowrap;
}

.app-nav__link:hover {
  color: var(--color-primary);
}

.app-nav__link.router-link-exact-active {
  color: var(--color-primary);
  font-weight: var(--font-weight-medium);
}
</style>

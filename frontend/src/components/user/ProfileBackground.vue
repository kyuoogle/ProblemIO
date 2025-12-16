<template>
  <div class="profile-background" :style="backgroundStyle" :class="backgroundClass">
    <div class="profile-content" :style="contentStyle">
      <slot></slot>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useCustomItemStore } from '@/stores/customItemStore';
import { resolveImageUrl } from '@/lib/image';

const props = defineProps({
  user: {
    type: Object,
    required: true
  },
  // 미리보기 목적으로 테마 ID 오버라이드 허용
  previewThemeId: {
    type: [String, Number],
    default: null
  }
});

const customItemStore = useCustomItemStore();

const themeConfig = computed(() => {
  const themeId = props.previewThemeId !== null ? props.previewThemeId : props.user?.profileTheme;
  if (!themeId) return null;
  return customItemStore.getItemConfig('THEME', themeId);
});

const backgroundStyle = computed(() => {
  const theme = themeConfig.value;
  if (!theme) return {}; // 기본 .p-card 스타일 사용

  if (theme.image) {
    return {
      backgroundImage: `url('${resolveImageUrl(theme.image)}')`,
      backgroundSize: 'cover',
      backgroundPosition: 'center',
      backgroundRepeat: 'no-repeat',
      ...theme.style
    };
  }
  
  return theme.style || {};
});

const backgroundClass = computed(() => {
    // 기본 스타일(배경, 테두리, 그림자)을 상속받기 위해 p-card 클래스 추가
    return `p-card ${themeConfig.value?.class || ''}`;
});

// 텍스트 색상은 콘텐츠 래퍼에 적용하거나 상속해야 함
const contentStyle = computed(() => {
    const theme = themeConfig.value;
    if (!theme) return {};
    
    const textColor = theme.textColor;
    if (textColor) {
        return {
            color: textColor,
            '--text-color': textColor,
            '--text-color-secondary': textColor,
            '--color-heading': textColor,
            '--p-text-color': textColor 
        };
    }
    return {};
});
</script>

<style scoped>
.profile-background {
  border-radius: 12px;
  overflow: hidden; /* Ensure background doesn't bleed */
  transition: all 0.3s ease;
  position: relative;
}

.profile-content {
    position: relative;
    z-index: 1;
    height: 100%;
}
</style>

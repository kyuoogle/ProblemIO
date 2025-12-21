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
  },
  // [New] 직접 설정 객체 주입 (Admin Preview용)
  previewConfig: {
    type: Object,
    default: null
  }
});

const customItemStore = useCustomItemStore();

const themeConfig = computed(() => {
  // 1. 직접 Config가 주입되면 그것을 최우선으로 사용
  if (props.previewConfig) return props.previewConfig;

  // 2. ID 기반 조회
  const themeId = props.previewThemeId !== null ? props.previewThemeId : props.user?.profileTheme;
  if (!themeId) return null;
  return customItemStore.getItemConfig('THEME', themeId);
});

const backgroundStyle = computed(() => {
  const theme = themeConfig.value;
  if (!theme) return {}; // 기본 .p-card 스타일 사용

  if (theme.image) {
    // Shorthand background to override any default card background colors.
    // Spread theme.style FIRST so the image overrides any background defined there
    return {
      ...theme.style,
      background: 'none !important', // Reset shorthand first
      backgroundColor: 'transparent !important', // Ensure color doesn't conflict
      backgroundImage: `url('${resolveImageUrl(theme.image)}') !important`,
      backgroundSize: 'cover !important',
      backgroundPosition: 'center !important',
      backgroundRepeat: 'no-repeat !important',
    };
  }
  
  return theme.style || {};
});

const backgroundClass = computed(() => {
    // 기본 스타일(배경, 테두리, 그림자)을 상속받기 위해 p-card 클래스 추가
    // 단, Custom theme이 적용된 경우 다크모드 강제 스타일(배경색 등)을 피하기 위해 p-card를 제거함
    const theme = themeConfig.value;
    const isCustom = theme && Object.keys(theme).length > 0;
    
    // 'default' 테마이거나 테마가 없는 경우에만 p-card 적용
    const isDefault = !theme || theme.id === 'default' || theme.name === 'Default';
    
    return `${isDefault ? 'p-card' : ''} ${isCustom ? 'EditCustom' : ''} ${theme?.class || ''}`;
});

// 텍스트 색상은 콘텐츠 래퍼에 적용하거나 상속해야 함
const contentStyle = computed(() => {
    const theme = themeConfig.value;
    if (!theme) return {};
    
    const textColor = theme.textColor;
    if (textColor) {
        return {
            color: `${textColor} !important`,
            '--text-color': `${textColor} !important`,
            '--text-color-secondary': `${textColor} !important`,
            '--color-heading': `${textColor} !important`,
            '--p-text-color': `${textColor} !important`
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

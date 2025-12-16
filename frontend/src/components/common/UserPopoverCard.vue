<template>
  <!-- 내용 -->
    <!-- 배경 이미지 적용: decoration이 있으면 그 이미지를 배경으로 사용 -->
    <div 
      class="w-96 p-3 relative overflow-hidden rounded-xl popover-content"
      :style="popoverStyle" 
    >
      <!-- 배경 오버레이 (텍스트 가독성 + 디자인) -->
      <div 
         class="absolute inset-0 z-0 transition-all duration-300"
         :class="popoverStyle.overlayStyle"
      ></div>

      <!-- 실제 컨텐츠 - z-index로 배경 위에 올림 -->
      <div class="relative z-10">
        <!-- 상단: 아바타 + 닉네임 + 버튼 -->
        <div class="flex items-center gap-4">

          <UserAvatar
            :user="profile"
            size="large"
            class="mr-1 font-bold"
            :class="{ 'surface-200 text-700': !profile?.profileImageUrl }"
          />

          <div class="flex-1 min-w-0">
            <p class="font-semibold text-base truncate cursor-pointer hover:underline" @click="$emit('click-profile')">
              {{ profile.nickname || "알 수 없는 사용자" }}
            </p>
            <p v-if="profile.statusMessage" class="text-sm opacity-80 mt-1 break-words">
              {{ profile.statusMessage }}
            </p>
          </div>

          <!-- 오른쪽 상단 버튼 -->
           <!-- 미리보기에서는 버튼을 숨기거나 더미로 표시 -->
          <slot name="action-button"></slot>
        </div>

        <!-- 하단: 팔로워 / 팔로잉 (오른쪽 아래 정렬) -->
        <div class="mt-6 flex justify-end gap-6 text-right text-sm">
          <div>
            <p class="font-semibold text-base">
              {{ profile.followerCount || 0 }}
            </p>
            <p class="text-xs opacity-60 mt-0.5">팔로워</p>
          </div>
          <div>
            <p class="font-semibold text-base">
              {{ profile.followingCount || 0 }}
            </p>
            <p class="text-xs opacity-60 mt-0.5">팔로잉</p>
          </div>
        </div>

        <!-- 프로필 보기 버튼 -->
        <slot name="bottom-button"></slot>
      </div>
    </div>
</template>

<script setup>
import { computed } from 'vue';
import UserAvatar from '@/components/common/UserAvatar.vue'
import { useCustomItemStore } from '@/stores/customItemStore';
import { resolveImageUrl } from '@/lib/image';

const props = defineProps({
    profile: {
        type: Object,
        required: true
    },
    // Optional override for decoration, used for preview
    previewDecorationId: {
        type: [String, Number],
        default: null
    }
});

defineEmits(['click-profile']);

const customItemStore = useCustomItemStore();

const popoverStyle = computed(() => {
  // 미리보기 ID가 있으면 그것을 사용, 없으면 프로필의 설정을 사용
  const decoId = props.previewDecorationId !== null ? props.previewDecorationId : props.profile?.popoverDecoration;
  
  if (decoId) {
    const deco = customItemStore.getItemConfig('POPOVER', decoId);
      if (deco) {
        // [수정] !important를 붙여서 우선순위 강제
        const varsStyle = deco.textColor ? {
            '--text-color': `${deco.textColor} !important`,
            '--text-color-secondary': `${deco.textColor} !important`
        } : {};

        const baseStyle = {
            color: deco.textColor || 'inherit',
            // 오버레이 스타일: 기본값 제거 (이미지 가림 방지)
            overlayStyle: deco.overlayStyle || '', 
            textStyle: deco.textStyle || {},
            buttonStyle: deco.buttonStyle || {},
            ...varsStyle,
            ...(deco.style || {})
        };

        if (deco.image) {
            return {
                backgroundImage: `url('${resolveImageUrl(deco.image)}')`,
                backgroundSize: 'cover',
                backgroundPosition: 'center',
                ...baseStyle
            }
        }
        return baseStyle;
    }
  }
  return { overlayStyle: '', textStyle: {}, buttonStyle: {} };
});
</script>

<style scoped>
/* 텍스트 색상 강제 상속을 위한 스타일 */
.popover-content p,
.popover-content span,
.popover-content i {
  color: inherit !important;
}
</style>

<template>
  <OverlayPanel ref="op" class="border-none rounded-xl shadow-lg p-0 popover-panel" :style="popoverStyle">
    <!-- 로딩 / 에러 -->
    <div v-if="loading" class="w-96 p-6 text-center text-sm text-gray-500">프로필 정보를 불러오는 중입니다.</div>
    <div v-else-if="error" class="w-96 p-6 text-center text-sm text-red-500">
      {{ error }}
    </div>

    <!-- 내용 -->
    <!-- 배경 이미지 적용: decoration이 있으면 그 이미지를 배경으로 사용 -->
    <div 
      v-else 
      class="w-96 p-3 relative overflow-hidden rounded-xl popover-content"
      :style="{ color: popoverStyle.color, ...popoverStyle.textStyle }" 
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
            <p class="font-semibold text-base truncate cursor-pointer hover:underline" @click.stop="goToProfile">
              {{ profile.nickname || "알 수 없는 사용자" }}
            </p>
            <p v-if="profile.statusMessage" class="text-sm opacity-80 mt-1 break-words">
              {{ profile.statusMessage }}
            </p>
          </div>

          <!-- 오른쪽 상단 버튼 -->
          <Button v-if="!profile.me" :label="profile.following ? '팔로잉' : '팔로우'" size="small" :outlined="profile.following" class="!text-xs !px-3 whitespace-nowrap" @click.stop="onToggleFollow" :style="popoverStyle.buttonStyle" />
          <Button v-else icon="pi pi-cog" rounded outlined size="small" class="!text-xs !px-3" @click.stop="goToProfileEdit" :style="popoverStyle.buttonStyle" />
        </div>

        <!-- 하단: 팔로워 / 팔로잉 (오른쪽 아래 정렬) -->
        <div class="mt-6 flex justify-end gap-6 text-right text-sm">
          <div>
            <p class="font-semibold text-base">
              {{ profile.followerCount }}
            </p>
            <p class="text-xs opacity-60 mt-0.5">팔로워</p>
          </div>
          <div>
            <p class="font-semibold text-base">
              {{ profile.followingCount }}
            </p>
            <p class="text-xs opacity-60 mt-0.5">팔로잉</p>
          </div>
        </div>

        <!-- 프로필 보기 버튼 -->
        <Button class="w-full mt-4 !text-sm" severity="secondary" outlined label="프로필 보기" @click="goToProfile" :style="popoverStyle.buttonStyle" />
      </div>
    </div>
  </OverlayPanel>
</template>

<script setup>
import { ref, reactive, computed } from "vue";
import { useRouter } from "vue-router";
import OverlayPanel from "primevue/overlaypanel";
import Button from "primevue/button";
import { getUserPopover, followUser, unfollowUser } from "@/api/user";
import UserAvatar from '@/components/common/UserAvatar.vue' // 유저 아바타 불러오기 
import { POPOVER_DECORATIONS } from '@/constants/popoverConfig' 
import { resolveImageUrl } from '@/lib/image' 

const op = ref(null);
const router = useRouter();

const loading = ref(false);
const error = ref("");

const profile = reactive({
  userId: null,
  nickname: "",
  profileImageUrl: "",
  statusMessage: "",
  following: false, // 내가 이 유저를 팔로우 중인지
  me: false, // 나 자신인지
  followerCount: 0,
  followingCount: 0,
  popoverDecoration: null,
  avatarDecoration: null,
  profileTheme: null,
});

const avatarUrl = computed(() => {
  if (!profile.profileImageUrl) return null;

  // 이미 절대 경로라면 그대로 사용
  if (profile.profileImageUrl.startsWith("http")) {
    return profile.profileImageUrl;
  }

  // 서버에서 "/uploads/..." 처럼 내려온다고 가정
  return `http://localhost:8080${profile.profileImageUrl}`;
});

// 업로드 경로 보정: DB에 파일명만 있으면 /upload/profile/ 붙여줌
const avatarSrc = computed(() => {
  const url = profile.profileImageUrl;
  if (!url) return "";
  if (url.startsWith("http") || url.startsWith("/")) return url;
  if (url.startsWith("http") || url.startsWith("/")) return url;
  return `/upload/profile/${url}`;
});

const popoverStyle = computed(() => {
  if (profile.popoverDecoration) {
    const deco = POPOVER_DECORATIONS[profile.popoverDecoration];
      if (deco) {
        // [수정] !important를 붙여서 우선순위 강제
        const colorStyle = deco.textColor ? `color: ${deco.textColor} !important;` : '';
        const varsStyle = deco.textColor ? {
            '--text-color': `${deco.textColor} !important`,
            '--text-color-secondary': `${deco.textColor} !important`
        } : {};

        const baseStyle = {
            color: deco.textColor || 'inherit',
            overlayStyle: deco.overlayStyle || 'bg-white/60 backdrop-blur-[1px]',
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

async function fetchProfile(userId) {
  loading.value = true;
  error.value = "";
  try {
    const data = await getUserPopover(userId);
    Object.assign(profile, data);
  } catch (e) {
    console.error(e);
    error.value = "프로필 정보를 불러오지 못했습니다.";
  } finally {
    loading.value = false;
  }
}

async function onToggleFollow() {
  if (!profile.userId) return;

  try {
    if (profile.following) {
      await unfollowUser(profile.userId);
      profile.following = false;
      if (profile.followerCount > 0) profile.followerCount--;
    } else {
      await followUser(profile.userId);
      profile.following = true;
      profile.followerCount++;
    }
  } catch (e) {
    console.error(e);
  }
}

function goToProfile() {
  if (!profile.userId) return;
  router.push({
    // 실제 유저 프로필 라우트에 맞춰서 수정
    path: `/users/${profile.userId}`,
  });
  op.value?.hide();
}

function goToProfileEdit() {
  // 내 프로필 편집 페이지로 이동 (라우트에 맞게 수정)
  router.push("/mypage/edit");
  op.value?.hide();
}

/**
 * 부모 컴포넌트에서 사용:
 * const userPopoverRef = ref(null)
 * <UserPopover ref="userPopoverRef" />
 * @click="(e) => userPopoverRef.open(e, user.id)"
 */
function open(event, userId) {
  profile.userId = userId;
  fetchProfile(userId);
  op.value?.toggle(event);
}

defineExpose({ open });
</script>

<style scoped>
/* 텍스트 색상 강제 상속을 위한 스타일 */
.popover-content p,
.popover-content span,
.popover-content i {
  color: inherit !important;
}

/* 버튼 내부 텍스트는 버튼 스타일을 따르도록 예외 처리 가능하나, 
   네온 테마의 경우 버튼 텍스트도 네온색으로 통일감을 주는 것이 좋을 수 있음.
   만약 버튼은 제외하고 싶다면 :not(.p-button-label) 등을 사용.
   여기서는 일단 전체 컨셉 통일을 위해 다 둡니다. */
</style>

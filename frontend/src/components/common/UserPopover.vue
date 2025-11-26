<template>
  <OverlayPanel ref="op" class="border-none rounded-xl shadow-lg p-0">
    <!-- 로딩 / 에러 -->
    <div v-if="loading" class="w-96 p-6 text-center text-sm text-gray-500">프로필 정보를 불러오는 중입니다.</div>
    <div v-else-if="error" class="w-96 p-6 text-center text-sm text-red-500">
      {{ error }}
    </div>

    <!-- 내용 -->
    <div v-else class="w-96 p-6">
      <!-- 상단: 아바타 + 닉네임 + 버튼 -->
      <div class="flex items-center gap-4">
        <Avatar
          :image="avatarSrc || undefined"
          :label="!avatarSrc && profile.nickname ? profile.nickname.charAt(0).toUpperCase() : ''"
          :icon="!avatarSrc && !profile.nickname ? 'pi pi-user' : undefined"
          shape="circle"
          size="xlarge"
          class="border border-gray-200"
        />

        <div class="flex-1 min-w-0">
          <p class="font-semibold text-base truncate cursor-pointer hover:underline" @click.stop="goToProfile">
            {{ profile.nickname || "알 수 없는 사용자" }}
          </p>
          <p v-if="profile.statusMessage" class="text-sm text-gray-500 mt-1 break-words">
            {{ profile.statusMessage }}
          </p>
        </div>

        <!-- 오른쪽 상단 버튼 -->
        <Button v-if="!profile.me" :label="profile.following ? '팔로잉' : '팔로우'" size="small" :outlined="profile.following" class="!text-xs !px-3 whitespace-nowrap" @click.stop="onToggleFollow" />
        <Button v-else icon="pi pi-cog" rounded outlined size="small" class="!text-xs !px-3" @click.stop="goToProfileEdit" />
      </div>

      <!-- 하단: 팔로워 / 팔로잉 (오른쪽 아래 정렬) -->
      <div class="mt-6 flex justify-end gap-6 text-right text-sm">
        <div>
          <p class="font-semibold text-base">
            {{ profile.followerCount }}
          </p>
          <p class="text-xs text-gray-500 mt-0.5">팔로워</p>
        </div>
        <div>
          <p class="font-semibold text-base">
            {{ profile.followingCount }}
          </p>
          <p class="text-xs text-gray-500 mt-0.5">팔로잉</p>
        </div>
      </div>

      <!-- 프로필 보기 버튼 -->
      <Button class="w-full mt-4 !text-sm" severity="secondary" outlined label="프로필 보기" @click="goToProfile" />
    </div>
  </OverlayPanel>
</template>

<script setup>
import { ref, reactive, computed } from "vue";
import { useRouter } from "vue-router";
import OverlayPanel from "primevue/overlaypanel";
import Avatar from "primevue/avatar";
import Button from "primevue/button";
import { getUserPopover, followUser, unfollowUser } from "@/api/user";

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
});

// 업로드 경로 보정: DB에 파일명만 있으면 /upload/profile/ 붙여줌
const avatarSrc = computed(() => {
  const url = profile.profileImageUrl;
  if (!url) return "";
  if (url.startsWith("http") || url.startsWith("/")) return url;
  return `/upload/profile/${url}`;
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

<style scoped></style>

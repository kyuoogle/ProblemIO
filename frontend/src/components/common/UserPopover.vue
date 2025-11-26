<template>
  <OverlayPanel ref="op" class="border-none rounded-xl shadow-lg p-0">
    <!-- 로딩 / 에러 -->
    <div v-if="loading" class="w-72 p-4 text-center text-sm text-gray-500">프로필 정보를 불러오는 중입니다.</div>
    <div v-else-if="error" class="w-72 p-4 text-center text-sm text-red-500">
      {{ error }}
    </div>

    <!-- 내용 -->
    <div v-else class="w-72 p-4">
      <!-- 상단: 아바타 + 닉네임 + 팔로우 버튼 -->
      <div class="flex items-center gap-3">
        <Avatar
          :image="profile.profileImageUrl || undefined"
          :label="!profile.profileImageUrl && profile.nickname ? profile.nickname.charAt(0).toUpperCase() : ''"
          :icon="!profile.profileImageUrl && !profile.nickname ? 'pi pi-user' : undefined"
          shape="circle"
          size="large"
          class="border border-gray-200"
        />

        <div class="flex-1 min-w-0">
          <p class="font-semibold text-sm truncate">
            {{ profile.nickname || "알 수 없는 사용자" }}
          </p>
          <p v-if="profile.statusMessage" class="text-xs text-gray-500 truncate mt-0.5">
            {{ profile.statusMessage }}
          </p>
        </div>

        <Button v-if="!profile.isMe" :label="profile.isFollowing ? '팔로잉' : '팔로우'" size="small" :outlined="profile.isFollowing" class="!text-xs !px-3" @click.stop="onToggleFollow" />
      </div>

      <!-- 중간: 팔로워 / 팔로잉 -->
      <div class="flex justify-around mt-4 text-center text-sm">
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

      <!-- 하단: 프로필 페이지로 이동 -->
      <Button class="w-full mt-4 !text-sm" severity="secondary" outlined label="프로필 보기" @click="goToProfile" />
    </div>
  </OverlayPanel>
</template>

<script setup>
import { ref, reactive } from "vue";
import { useRouter } from "vue-router";
import OverlayPanel from "primevue/overlaypanel";
import Avatar from "primevue/avatar";
import Button from "primevue/button";
import * as userApi from "@/api/user";

const op = ref(null);
const router = useRouter();

const loading = ref(false);
const error = ref("");

const profile = reactive({
  userId: null,
  nickname: "",
  profileImageUrl: "",
  statusMessage: "",
  isFollowing: false,
  isMe: false,
  followerCount: 0,
  followingCount: 0,
});

async function fetchProfile(userId) {
  loading.value = true;
  error.value = "";
  try {
    // TODO: 백엔드에서 userId 기반 팝오버용 요약 API 만들기
    // 예시: GET /api/users/{userId}/popover
    const { data } = await userApi.getUserPopover(userId);

    Object.assign(profile, {
      userId: data.userId,
      nickname: data.nickname,
      profileImageUrl: data.profileImageUrl,
      statusMessage: data.statusMessage,
      isFollowing: data.isFollowing,
      isMe: data.isMe,
      followerCount: data.followerCount,
      followingCount: data.followingCount,
    });
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
    if (profile.isFollowing) {
      // 언팔로우
      await userApi.unfollowUser(profile.userId);
      profile.isFollowing = false;
      if (profile.followerCount > 0) profile.followerCount--;
    } else {
      // 팔로우
      await userApi.followUser(profile.userId);
      profile.isFollowing = true;
      profile.followerCount++;
    }
  } catch (e) {
    console.error(e);
  }
}

function goToProfile() {
  if (!profile.userId) return;
  // 라우터 네임은 실제 프로젝트에서 사용하는 이름으로 수정
  router.push({
    name: "UserProfile",
    params: { userId: profile.userId },
  });
  op.value?.hide();
}

/**
 * 부모 컴포넌트에서 사용 예:
 * const popoverRef = ref(null)
 * <UserPopover ref="popoverRef" />
 * <span @click="(e) => popoverRef.open(e, user.id)">닉네임</span>
 */
function open(event, userId) {
  profile.userId = userId;
  fetchProfile(userId);
  op.value?.toggle(event);
}

defineExpose({ open });
</script>

<style scoped></style>
 
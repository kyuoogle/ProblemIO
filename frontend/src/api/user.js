import apiClient from "./axios";

// 유저 공개 프로필 조회
export const getUserProfile = async (userId) => {
  const response = await apiClient.get(`/users/${userId}`);
  return response.data.data;
};

// 내 프로필 수정 (닉네임 + 상태메시지 + 프로필 이미지)
export const updateMyProfile = async (formData) => {
  const response = await apiClient.patch("/users/me", formData);
  return response.data.data;
};

// 비밀번호 변경
export const changePassword = async (currentPassword, newPassword) => {
  await apiClient.patch("/users/me/password", {
    oldPassword: currentPassword,
    newPassword: newPassword,
  });
};

// 회원 탈퇴
export const deleteAccount = async (password) => {
  await apiClient.post("/users/me/withdraw", {
    password,
  });
};

// 팔로우
export const followUser = async (userId) => {
  const response = await apiClient.post(`/follows/${userId}`);
  return response.data.data;
};

// 언팔로우
export const unfollowUser = async (userId) => {
  await apiClient.delete(`/follows/${userId}`);
};

// 내 팔로워 목록
export const getMyFollowers = async () => {
  const response = await apiClient.get("/users/me/followers");
  return response.data.data;
};

// 내 팔로잉 목록
export const getMyFollowings = async () => {
  const response = await apiClient.get("/users/me/followings");
  return response.data.data;
};

// 마이페이지 상단 요약
export const getMySummary = async () => {
  const response = await apiClient.get("/users/me/summary");
  return response.data.data;
};

// 내가 좋아요한 퀴즈 목록
export const getMyLikedQuizzes = async () => {
  const response = await apiClient.get("/users/me/liked-quizzes");
  return response.data.data;
};

// 팔로잉 유저들의 퀴즈 목록
export const getFollowingQuizzes = async (sort = "latest") => {
  const response = await apiClient.get(`/users/me/following-quizzes?sort=${sort}`);
  return response.data.data;
};

// 내 정보 조회
export const getMe = async () => {
  const response = await apiClient.get(`/users/me`);
  return response.data.data;
};

// 닉네임 중복 확인
export const checkNickname = async (nickname) => {
  // 요청: GET /user/checkNickname?nickname=...
  const response = await apiClient.get("/users/checkNickname", {
    params: { nickname },
  });

  return response.data.data;
};

const apiBaseRaw = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api').replace(/\/+$/, '')
const apiBaseUrl = apiBaseRaw.endsWith('/api') ? apiBaseRaw : `${apiBaseRaw}`
const staticBaseUrl = apiBaseUrl.replace(/\/api$/, '')

const s3BaseUrl = (import.meta.env.VITE_S3_BASE_URL).replace(/\/+$/, '')

export const resolveImageUrl = (url) => {
  if (!url) return url
  if (url.startsWith('http')) {
    const remapped = remapLegacyUploadUrl(url)
    return remapped || url
  }
  if (url.startsWith('blob:') || url.startsWith('data:')) return url
  
  // Public S3 paths
  if (url.startsWith('public/') || url.startsWith('/public/')) {
      const cleanPath = url.startsWith('/') ? url.substring(1) : url;
      return `${s3BaseUrl}/${cleanPath}`;
  }

  const prefix = url.startsWith('/') ? '' : '/'
  return `${staticBaseUrl}${prefix}${url}`
}

export const resolveQuizImages = (quiz) => {
  if (!quiz) return quiz
  const resolved = { ...quiz }
  resolved.thumbnailUrl = resolveImageUrl(quiz.thumbnailUrl)

  if (quiz.questions) {
    resolved.questions = quiz.questions.map((q) => ({
      ...q,
      imageUrl: resolveImageUrl(q.imageUrl),
    }))
  }
  return resolved
}

// 레거시 로컬 업로드 URL을 S3 경로로 변환
const remapLegacyUploadUrl = (url) => {
  const lower = url.toLowerCase()
  const marker = '/uploads/'

  // host가 포함된 경우
  if (lower.startsWith('http://localhost:8080/uploads/') || lower.startsWith('https://localhost:8080/uploads/')) {
    const idx = lower.indexOf(marker)
    const path = url.substring(idx + marker.length)
    return buildS3FromUploads(path)
  }

  // 절대 경로만 온 경우
  if (lower.startsWith('/uploads/')) {
    const path = url.substring(marker.length)
    return buildS3FromUploads(path)
  }

  return null
}

const buildS3FromUploads = (path) => {
  const clean = path.startsWith('/') ? path.substring(1) : path
  const lower = clean.toLowerCase()

  if (lower.startsWith('questions/')) {
    const rest = clean.substring('questions/'.length)
    return `${s3BaseUrl}/public/upload/questions/${rest}`
  }

  // fallback: uploads/* → public/upload/*
  return `${s3BaseUrl}/public/upload/${clean}`
}

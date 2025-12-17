const apiBaseRaw = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api').replace(/\/+$/, '')
const apiBaseUrl = apiBaseRaw.endsWith('/api') ? apiBaseRaw : `${apiBaseRaw}`
const staticBaseUrl = apiBaseUrl.replace(/\/api$/, '')

const s3BaseUrl = (import.meta.env.VITE_S3_BASE_URL).replace(/\/+$/, '')

export const resolveImageUrl = (url) => {
  if (!url) return url
  if (url.startsWith('http') || url.startsWith('blob:') || url.startsWith('data:')) return url
  
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

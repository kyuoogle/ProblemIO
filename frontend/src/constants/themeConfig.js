const S3_PUBLIC_BASE = (import.meta.env.VITE_S3_BASE_URL || '').replace(/\/+$/, '');

const withS3Public = (path) => {
  if (!path) return path;
  if (!S3_PUBLIC_BASE) return path; // fallback to original path when env is missing
  const clean = path.startsWith('/') ? path.slice(1) : path;
  return `${S3_PUBLIC_BASE}/${clean}`;
};

export const PROFILE_THEMES = {
  default: { 
    name: 'Default', 
    // No specific style needed, adapts to global Light/Dark mode
  },
  cybercity: {
    name: 'Cybercity',
    image: withS3Public('public/theme/cybercity.jpg'),
    textColor: '#ffffff',
    style: {
      textShadow: '0 0 5px #00eaff, 0 0 10px #00eaff',
      WebkitTextStroke: '1px #00eaff',
      border: '2px solid #00eaff',
      animation: 'neon-pulse 2s infinite ease-in-out, border-flow 4s infinite linear'
    }
  }
};

const S3_PUBLIC_BASE = (import.meta.env.VITE_S3_BASE_URL || '').replace(/\/+$/, '');

const withS3Public = (path) => {
  if (!path) return path;
  if (!S3_PUBLIC_BASE) return path;
  const clean = path.startsWith('/') ? path.slice(1) : path;
  return `${S3_PUBLIC_BASE}/${clean}`;
};

export const POPOVER_DECORATIONS = {
  default: {
    name: 'Default',
    style: { backgroundColor: 'var(--color-bg-card)', color: 'var(--color-text-main)' }, // Use vars for Dark Mode
    textColor: 'var(--color-text-main)' 
  },
  cybercity: {
    name: 'Cybercity',
    image: withS3Public('public/popover/cybercity.jpg'),
    textColor: '#ffffff',
    textStyle: {
      textShadow: '0 0 5px #00eaff, 0 0 10px #00eaff',
      WebkitTextStroke: '1px #00eaff',
      animation: 'neon-pulse 2s infinite ease-in-out'
    },
    buttonStyle: {
      backgroundColor: 'rgba(0, 0, 0, 0.7)',
      borderWidth: '2px',
      borderStyle: 'solid',
      color: '#ffffff',
      animation: 'border-flow 4s infinite linear',
      transition: 'all 0.3s ease'
    }
  }
};

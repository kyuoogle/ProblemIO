export const PROFILE_THEMES = {
  default: { 
    name: 'Default', 
    // Remove hardcoded styles so it adapts to Dark Mode (base.css)
  },
  pastel_pink: { 
    name: 'Pastel Pink',
    style: { backgroundColor: '#FFB7B2 !important' },
    textColor: '#4a154b' // Deep Purple-Brown
  },
  pastel_blue: { 
    name: 'Pastel Blue',
    style: { backgroundColor: '#Aec6cf !important' },
    textColor: '#0f3c4c' // Deep Blue-Teal
  },
  pastel_green: { 
    name: 'Pastel Green',
    style: { backgroundColor: '#B2F7EF !important' },
    textColor: '#064e42' // Deep Green
  },
  pastel_yellow: { 
    name: 'Pastel Yellow',
    style: { backgroundColor: '#FDFD96 !important' },
    textColor: '#5f5e00' // Dark Olive
  },
  pastel_purple: { 
    name: 'Pastel Purple',
    style: { backgroundColor: '#C3B1E1 !important' },
    textColor: '#2d1a49' // Deep Purple
  },
  cybercity: {
    name: 'Cybercity',
    image: 'public/theme/cybercity.jpg',
    textColor: '#ffffff',
    style: {
      textShadow: '0 0 5px #00eaff, 0 0 10px #00eaff',
      WebkitTextStroke: '1px #00eaff'
    }
  }
};
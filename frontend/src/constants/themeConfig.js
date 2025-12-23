// [디버깅용] 환경변수 대신 직접 주소 입력
const S3_PUBLIC_BASE = "https://s3.ap-northeast-2.amazonaws.com/s3-problemio";

const withS3Public = (path) => {
  if (!path) return path;
  if (!S3_PUBLIC_BASE) return path; // fallback to original path when env is missing

  // S3 URL 생성
  const clean = path.startsWith("/") ? path.slice(1) : path;
  return `${S3_PUBLIC_BASE}/${clean}`;
};

export const PROFILE_THEMES = {
  default: {
    name: "Default",
    // No specific style needed, adapts to global Light/Dark mode
  },
  cybercity: {
    name: "Cybercity",
    image: withS3Public("public/theme/cybercity.jpg"),
    textColor: "#ffffff",
    style: {
      textShadow: "0 0 5px #00eaff, 0 0 10px #00eaff !important",
      WebkitTextStroke: "1px #00eaff",
      border: "2px solid #00eaff !important",
      animation: "neon-pulse 2s infinite ease-in-out, border-flow 4s infinite linear !important",
    },
  },

  aurora_night: {
    name: "Aurora Night",
    textColor: "#e0f7ff",
    style: {
      background: "linear-gradient(270deg, #1c1c3c, #3b2f63, #355c7d, #1c1c3c) !important",
      backgroundSize: "400% 400% !important",
      animation: "gradient-move 10s ease infinite !important",
    },
  },
  sunset_vibes: {
    name: "Sunset Vibes",
    textColor: "#5d3a3a",
    style: {
      background: "linear-gradient(45deg, #ff9a9e 0%, #fad0c4 99%, #fad0c4 100%) !important",
      backgroundSize: "200% 200% !important",
      animation: "gradient-move 5s ease infinite alternate !important",
    },
  },
  cyber_punk: {
    name: "Cyber Punk",
    textColor: "#00ff41",
    style: {
      backgroundColor: "#050505 !important",
      backgroundImage:
        "linear-gradient(0deg, transparent 24%, rgba(0, 255, 0, .05) 25%, rgba(0, 255, 0, .05) 26%, transparent 27%, transparent 74%, rgba(0, 255, 0, .05) 75%, rgba(0, 255, 0, .05) 76%, transparent 77%, transparent), linear-gradient(90deg, transparent 24%, rgba(0, 255, 0, .05) 25%, rgba(0, 255, 0, .05) 26%, transparent 27%, transparent 74%, rgba(0, 255, 0, .05) 75%, rgba(0, 255, 0, .05) 76%, transparent 77%, transparent) !important",
      backgroundSize: "50px 50px !important",
      boxShadow: "inset 0 0 50px #000 !important",
    },
  },
  golden_luxury: {
    name: "Golden Luxury",
    textColor: "#5a3e1a",
    style: {
      background: "linear-gradient(120deg, #f6d365 0%, #fda085 100%) !important",
      position: "relative !important",
      animation: "shimmer 3s infinite linear !important", // restored shimmer for luxury feel
      backgroundSize: "200% auto !important",
    },
  },

  matrix_code: {
    name: "Matrix Code",
    image: withS3Public("public/theme/cyberpunk.jpg"),
    textColor: "#00FF00",
    style: {
      backgroundColor: "#000 !important",
      fontFamily: "monospace !important",
      border: "2px solid #00FF00 !important",
      boxShadow: "0 0 10px #00FF00 !important",
    },
  },

  minimalist_float: {
    name: "Minimalist Float",
    textColor: "#111827",
    style: {
      backgroundColor: "#fff !important",
      backgroundImage: "radial-gradient(#e5e7eb 2px, transparent 2px) !important",
      backgroundSize: "20px 20px !important",
      animation: "floating 3s ease-in-out infinite !important",
    },
  },
};

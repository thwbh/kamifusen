/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./src/**/*.{js,jsx,ts,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        'tui-dark': 'var(--tui-dark)',
        'tui-darker': 'var(--tui-darker)',
        'tui-light': 'var(--tui-light)',
        'tui-accent': 'var(--tui-accent)',
        'tui-accent-hover': 'var(--tui-accent-hover)',
        'tui-close-accent': 'var(--tui-close-accent)',
        'tui-red': 'var(--tui-red)',
        'tui-green': 'var(--tui-green)',
        'tui-yellow': 'var(--tui-yellow)',
        'tui-border': 'var(--tui-border)',
        'tui-muted': 'var(--tui-muted)',
      },
      fontFamily: {
        'mono': ['var(--tui-font)', 'ui-monospace', 'SFMono-Regular', 'Consolas', 'Liberation Mono', 'Menlo', 'monospace'],
      },
      animation: {
        'pulse-slow': 'pulse 3s cubic-bezier(0.4, 0, 0.6, 1) infinite',
        'fade-in': 'fadeIn 0.5s ease-in-out',
      },
      keyframes: {
        fadeIn: {
          '0%': { opacity: '0' },
          '100%': { opacity: '1' },
        }
      }
    },
  },
  plugins: [],
}
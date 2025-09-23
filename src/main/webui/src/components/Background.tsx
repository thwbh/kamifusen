import React from 'react';

interface BackgroundProps {
  children: React.ReactNode
  showRedLines?: boolean
}

const Background: React.FC<BackgroundProps> = ({ children, showRedLines = true }) => {
  return (
    <div className="min-h-screen bg-tui-dark relative">
      {/* Diagonal red lines - top left with fade */}
      {showRedLines && (
        <div className="fixed top-0 left-0 w-screen h-screen pointer-events-none overflow-hidden z-5">
          <svg viewBox="0 0 1920 1080" className="w-full h-full" style={{ minWidth: '100vw', minHeight: '100vh' }}>
            <defs>
              <linearGradient id="redFade1" x1="0%" y1="0%" x2="60%" y2="60%">
                <stop offset="0%" stopColor="var(--tui-red)" stopOpacity="0.8" />
                <stop offset="40%" stopColor="var(--tui-red)" stopOpacity="0.4" />
                <stop offset="100%" stopColor="var(--tui-red)" stopOpacity="0" />
              </linearGradient>
              <linearGradient id="redFade2" x1="0%" y1="0%" x2="60%" y2="60%">
                <stop offset="0%" stopColor="#cc3333" stopOpacity="0.6" />
                <stop offset="40%" stopColor="#cc3333" stopOpacity="0.3" />
                <stop offset="100%" stopColor="#cc3333" stopOpacity="0" />
              </linearGradient>
            </defs>
            {Array.from({ length: 12 }, (_, i) => (
              <line
                key={i}
                x1={50 + i * 15}
                y1="0"
                x2={800 + i * 15}
                y2="1080"
                stroke={i % 2 === 0 ? "url(#redFade1)" : "url(#redFade2)"}
                strokeWidth={i % 3 === 0 ? "3" : "2"}
              />
            ))}
          </svg>
        </div>
      )}

      {/* Diagonal red lines - bottom right with fade */}
      {showRedLines && (
        <div className="fixed bottom-0 right-0 w-screen h-screen pointer-events-none overflow-hidden z-5">
          <svg viewBox="0 0 1920 1080" className="w-full h-full" style={{ minWidth: '100vw', minHeight: '100vh', transform: 'rotate(180deg)' }}>
            <defs>
              <linearGradient id="redFade3" x1="0%" y1="0%" x2="60%" y2="60%">
                <stop offset="0%" stopColor="var(--tui-red)" stopOpacity="0.7" />
                <stop offset="40%" stopColor="var(--tui-red)" stopOpacity="0.3" />
                <stop offset="100%" stopColor="var(--tui-red)" stopOpacity="0" />
              </linearGradient>
              <linearGradient id="redFade4" x1="0%" y1="0%" x2="60%" y2="60%">
                <stop offset="0%" stopColor="#aa2222" stopOpacity="0.5" />
                <stop offset="40%" stopColor="#aa2222" stopOpacity="0.2" />
                <stop offset="100%" stopColor="#aa2222" stopOpacity="0" />
              </linearGradient>
            </defs>
            {Array.from({ length: 10 }, (_, i) => (
              <line
                key={i}
                x1={50 + i * 15}
                y1="0"
                x2={800 + i * 15}
                y2="1080"
                stroke={i % 2 === 0 ? "url(#redFade3)" : "url(#redFade4)"}
                strokeWidth={i % 4 === 0 ? "2.5" : "2"}
              />
            ))}
          </svg>
        </div>
      )}


      {/* Content */}
      <div className="relative z-10">
        {children}
      </div>

      {/* Uniform dot grid pattern - like raster but with dots - over content */}
      <div
        className="fixed inset-0 pointer-events-none opacity-15 z-20"
        style={{
          backgroundImage: `
            radial-gradient(circle, rgba(255,255,255,0.6) 0.4px, transparent 0.4px)
          `,
          backgroundSize: '10px 10px'
        }}
      ></div>

      {/* Additional CRT scanline effect - over content */}
      <div
        className="fixed inset-0 pointer-events-none opacity-5 z-20"
        style={{
          backgroundImage: 'linear-gradient(transparent 50%, rgba(0,0,0,0.1) 50%)',
          backgroundSize: '100% 4px'
        }}
      ></div>
    </div>
  );
};

export default Background

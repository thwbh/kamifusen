import React from 'react';

interface SignOutProps {
  onRestart: () => void;
}

const SignOut: React.FC<SignOutProps> = ({ onRestart }) => {
  const handleKeyPress = (event: React.KeyboardEvent) => {
    if (event.key === 'Enter' || event.key === ' ') {
      onRestart();
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center px-8 animate-fade-in">
      <div className="text-center max-w-3xl">
        {/* Main heading */}
        <h1 className="text-tui-accent text-5xl md:text-6xl font-bold mb-12 tracking-widest uppercase">
          SIGNING OUT
        </h1>

        {/* Thank you message */}
        <h2 className="text-tui-light text-2xl mb-8 tracking-wide">
          Thank you for visiting!
        </h2>

        {/* Description */}
        <p className="text-tui-light text-lg leading-relaxed mb-16 max-w-2xl mx-auto">
          We appreciate you considering us for your development needs and look
          forward to getting in touch with you soon.
        </p>

        {/* Restart instruction */}
        <div className="text-tui-muted text-sm mb-8">
          <p>Press any key to restart the session</p>
        </div>

        {/* Hidden button for accessibility */}
        <button
          className="opacity-0 absolute"
          onClick={onRestart}
          onKeyDown={handleKeyPress}
          autoFocus
          tabIndex={0}
        >
          Restart
        </button>
      </div>
    </div>
  );
};

export default SignOut;

import kamifusenIcon from '../../assets/icons/logo_128.png';
import React from 'react';

const Header: React.FC = () => {
  return (
    <div className="flex items-center space-x-3">
      <div className="w-8 h-8 bg-tui-accent rounded-sm flex items-center justify-center">
        <img src={kamifusenIcon} alt="Ashiato Icon" className="w-6 h-6" />
      </div>
      <h1 className="text-tui-accent font-bold text-xl tracking-wider">KAMIFUSEN ADMIN</h1>
    </div>
  )
}

export default Header
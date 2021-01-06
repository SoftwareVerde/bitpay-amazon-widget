import React from 'react';
import './super-toast.scss';

const SuperToast: React.FC<{ title: string; caption: string; shopMode: boolean }> = ({ title, caption, shopMode }) => (
  <div className={`super-toast${shopMode ? ' super-toast--gradient' : ''}`}>
    <div className="super-toast__content">
      <div className="super-toast__content__icon">
        {shopMode ? (
          <img id="white" src="./../assets/icons/info-icon-white.svg" alt="info" />
        ) : (
          <img id="blue" src="./../assets/icons/info-icon-blue.svg" alt="info" />
        )}
      </div>
      <div className="super-toast__content__block">
        <div className="super-toast__content__block__title">{title}</div>
        <div className="super-toast__content__block__caption">{caption}</div>
      </div>
    </div>
  </div>
);

export default SuperToast;

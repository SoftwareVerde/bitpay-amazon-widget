import React from 'react';
import { Md5 } from 'ts-md5';
import './gravatar.scss';

const Gravatar: React.FC<{ email: string; size?: string | number }> = ({ email, size = 30 }) => {
  const emailHash = Md5.hashStr(email || '') as string;
  const defaultImg = `${process.env.API_ORIGIN}/img/wallet-logos/bitpay-wallet.png`;
  const url = `https://gravatar.com/avatar/${emailHash}.jpg?s=${+size * 4}&d=${defaultImg}`;

  return <img src={url} height={size} width={size} className="gravatar" alt="profile icon" />;
};

export default Gravatar;

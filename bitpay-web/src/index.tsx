import React from 'react';
import ReactDOM from 'react-dom';
import './index.css';
import {
    getBitPayMerchantFromUrl,
    fetchDirectoryAndMerchants
} from './services/merchant';
import { CardConfig } from './services/gift-card.types';
import { get } from './services/storage'
import App from './App';

setTimeout(async () => {
    // make sure we have merchants cached before Popup is rendered
    // this ensures it will list them on the initial page load
    const [newDirectory, newMerchants] = await fetchDirectoryAndMerchants();
    const merchant = await getBitPayMerchantFromUrl("https://amazon.com/", newMerchants)!;
    const cardConfigs = await get<CardConfig[]>('supportedGiftCards')
    let cardConfig: CardConfig | undefined = undefined;
    for (cardConfig of cardConfigs) {
        if (cardConfig.name == "Amazon.com") {
            break;
        }
    }

    let clientId = await get<string>('clientId');
    while (clientId == undefined) {
        clientId = await get<string>('clientId');
    }

    ReactDOM.render(
      <App clientId={clientId} merchant={merchant} cardConfig={cardConfig!} />,
      document.getElementById('app-root')
    );
}, 0);


import React from 'react';
import ReactDOM from 'react-dom';
import './index.css';
import './popup/styles.scss';
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
    const supportedGiftCards = await get<CardConfig[]>('supportedGiftCards');
    let cardConfig: CardConfig | undefined = undefined;
    for (cardConfig of supportedGiftCards) {
        if (cardConfig.name == "Amazon.com") {
            break;
        }
    }

    let clientId = await get<string>('clientId');
    while (clientId == undefined) {
        clientId = await get<string>('clientId');
    }

    let email = await get<string>('email');

    ReactDOM.render(
      <App
        clientId={clientId}
        emailAddress={email}
        merchant={merchant}
        cardConfig={cardConfig!}
        supportedGiftCards={supportedGiftCards}
        />,
      document.getElementById('app-root')
    );
}, 0);


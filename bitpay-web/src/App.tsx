import React, { useState } from 'react';
import { MemoryRouter as Router, Route, Switch } from 'react-router-dom';
import './App.css';
import {
  Merchant,
} from './services/merchant';
import { get } from './services/storage'
import { GiftCard, CardConfig } from './services/gift-card.types';
import { BitpayUser } from './services/bitpay-id';
import Amount from './popup/pages/amount/amount'
import Payment from './popup/pages/payment/payment'

const App: React.FC<{
    clientId: string,
    merchant: Merchant,
    cardConfig: CardConfig
}> = ({
    clientId,
    merchant,
    cardConfig
}) => {
  const initiallyCollapsed = false;
  const [amount, setAmount] = useState(0);
  const [email, setEmail] = useState(get<string>('email'));
  const [purchasedGiftCards, setPurchasedGiftCards] = useState(get<GiftCard[]>('purchasedGiftCards'));
  let [user, setUser] = useState(get<BitpayUser>('bitpayUser'));
  let supportedMerchant = merchant;

  let initialEntries: any = [{
      pathname: "/amount/Amazon.com",
      state: {
          merchant: supportedMerchant,
          cardConfig: cardConfig,
          isFirstPage: true
      }
  }];
  return (
    <div className="App">
      <div>
        <Router initialEntries={initialEntries} initialIndex={0}>
          <Switch>
            <Route
              path="/amount/:brand"
              render={(props): JSX.Element => (
                <Amount
                  clientId={clientId}
                  email={email}
                  initialAmount={amount}
                  initiallyCollapsed={initiallyCollapsed}
                  purchasedGiftCards={purchasedGiftCards}
                  setPurchasedGiftCards={setPurchasedGiftCards}
                  supportedMerchant={supportedMerchant}
                  user={user}
                  {...props}
                />
              )}
            />
            <Route
              path="/payment/:brand"
              render={(props): JSX.Element => (
                <Payment
                  setEmail={setEmail}
                  user={user}
                  purchasedGiftCards={purchasedGiftCards}
                  setPurchasedGiftCards={setPurchasedGiftCards}
                  supportedMerchant={supportedMerchant}
                  initiallyCollapsed={initiallyCollapsed}
                  {...props}
                />
              )}
            />
          </Switch>
        </Router>
      </div>
    </div>
  );
}

export default App;

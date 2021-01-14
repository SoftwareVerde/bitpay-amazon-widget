import React, { useState } from 'react';
import { MemoryRouter as Router, Route, Switch } from 'react-router-dom';
import './App.css';
import {
  Merchant,
} from './services/merchant';
//import { get } from './services/storage'
import { GiftCard, CardConfig } from './services/gift-card.types';
import { BitpayUser } from './services/bitpay-id';
import {
  updateCard,
} from './services/gift-card-storage';
import Amount from './popup/pages/amount/amount'
import Payment from './popup/pages/payment/payment'
import Card from './popup/pages/card/card';
import Wallet from './popup/pages/wallet/wallet';

const App: React.FC<{
    clientId: string,
    emailAddress: string | undefined,
    merchant: Merchant,
    cardConfig: CardConfig,
    supportedGiftCards: CardConfig[]
}> = ({
    clientId,
    emailAddress,
    merchant,
    cardConfig,
    supportedGiftCards
}) => {
  const initiallyCollapsed = false;
  const [amount, setAmount] = useState(0);
  const [email, setEmail] = useState(emailAddress);
  const [purchasedGiftCards, setPurchasedGiftCards] = useState([] as GiftCard[]);
  const [user, setUser] = useState(undefined as BitpayUser | undefined);
  const [initialEntries, setInitialEntries] = useState([{
      pathname: "/amount/Amazon.com",
      state: {
          merchant: merchant,
          cardConfig: cardConfig,
          isFirstPage: true
      }
  }]);
  const [initialIndex, setInitialIndex] = useState(0);

  const updateGiftCard = async (card: GiftCard): Promise<void> => {
    const newCards = await updateCard(card, purchasedGiftCards);
    setPurchasedGiftCards(newCards);
  };

  let supportedMerchant = merchant;

  return (
    <div className="App">
      <div>
        <Router initialEntries={initialEntries} initialIndex={initialIndex}>
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
            <Route
              path="/wallet"
              render={(props): JSX.Element => (
                <Wallet
                  supportedMerchant={supportedMerchant}
                  supportedGiftCards={supportedGiftCards}
                  purchasedGiftCards={purchasedGiftCards}
                  {...props}
                />
              )}
            />
            <Route
              path="/card/:id"
              exact
              render={(props): JSX.Element => (
                <Card purchasedGiftCards={purchasedGiftCards} updateGiftCard={updateGiftCard} {...props} />
              )}
            />
          </Switch>
        </Router>
      </div>
    </div>
  );
}

export default App;

import React from 'react';
import ReactDOM from 'react-dom';
import './index.css';
import { fetchDirectoryAndMerchants } from './services/merchant';
import App from './App';

setTimeout(async () => {
    // make sure we have merchants cached before Popup is rendered
    // this ensures it will list them on the initial page load
    await fetchDirectoryAndMerchants();

    ReactDOM.render(
      <App />,
      document.getElementById('app-root')
    );
}, 0);


# Bitcoin Cash Gift Cards

## About

This project is a modification of the BitPay Browser Extension so that users may purchase gift
cards without downloading and installing a browser extension.

<p align="center">
  <img width="375" src="https://bitpay.com/img/demos/extension-demo.gif" />
</p>

Visit https://github.com/bitpay/bitpay-browser-extension for details about the BitPay browser
extension.

All transactions route through BitPay unmodified from the original browser-extension version,
and core functionality is completely unchanged.

Sites may embed this content via an iframe to allow their users direct access to Amazon gift
cards, purchased via Bitcoin Cash.  Providing an email address is encouraged for customer
support via BitPay but is optional.

This project also contains a simple webserver to demo or the page as a single-page web app.
Usage of this webserver is optional and the content may also be directly embedded within website.

## Build &amp; Installation

### Linux / OSX

Ensure npm is installed on your machine.  Once installed, the entire build process is handled
via:

    ./scripts/make.sh

## Run/Demo

    cd out
    ./run.sh

## Embedding Widget into your Website

Embedding the widget on any webpage is very simple:

    <iframe class="bitpay" src="/bitpay.html"></iframe>

And add the following css to your webpage:

    iframe.bitpay {
        margin: auto;
        margin-top: 3em;
        border: none;
        display: block;
        width: 400px;
        height: 600px;
    }

## License

Code is released under the MIT License.


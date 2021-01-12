//import { browser, Tabs } from 'webextension-polyfill-ts';
import * as uuid from 'uuid';
import { dispatchEvent } from './services/analytics';
import { GiftCardInvoiceMessage } from './services/gift-card.types';
import {
  isBitPayAccepted,
  Merchant,
  fetchCachedMerchants,
  getBitPayMerchantFromUrl,
  fetchDirectoryAndMerchants
} from './services/merchant';
import { get, set } from './services/storage';
import { generatePairingToken } from './services/bitpay-id';

let cachedMerchants: Merchant[] | undefined;
let cacheDate = 0;
const cacheValidityDuration = 1000 * 60;

var popupWindow : any;
var giftCardInvoiceCallback : ((message: GiftCardInvoiceMessage) => GiftCardInvoiceMessage) | undefined;
//const windowIdResolveMap: { [windowId: number]: (message: GiftCardInvoiceMessage) => GiftCardInvoiceMessage } = {};

//function getIconPath(bitpayAccepted: boolean): string {
//  return `/assets/icons/favicon${bitpayAccepted ? '' : '-inactive'}-128.png`;
//}

function setIcon(bitpayAccepted: boolean): void {
  console.log("setIcon: " +  bitpayAccepted);
  //browser.browserAction.setIcon({ path: getIconPath(bitpayAccepted) });
}

async function getCachedMerchants(): Promise<Merchant[]> {
  return cachedMerchants || fetchCachedMerchants();
}

async function refreshCachedMerchants(): Promise<void> {
  cachedMerchants = await fetchCachedMerchants();
  cacheDate = Date.now();
}

async function refreshCachedMerchantsIfNeeded(): Promise<void> {
  if (Date.now() < cacheDate + cacheValidityDuration) return;
  return fetchDirectoryAndMerchants()
    .then(() => refreshCachedMerchants())
    .catch(err => console.log('Error refreshing merchants', err));
}

function isGiftCardInvoice(url: string): boolean {
  return url.startsWith(process.env.API_ORIGIN as string) && url.includes('/invoice?id=') && url.includes('view=popup');
}

// eslint-disable-next-line @typescript-eslint/no-explicit-any
async function sendMessageToTab(message: any/*, tab: any*/): Promise<void> {
  //return browser.tabs.sendMessage(tab.id as number, message);
  if (message !== undefined) {
    console.log("Not calling " + contentScriptCallbacks.length + " callbacks.");
    //for (var callback of contentScriptCallbacks) {
    //   callback(message);
    //}
  }
}

async function handleUrlChange(url: string/*, tab?: any*/): Promise<void> {
  const merchants = await getCachedMerchants();
  const bitpayAccepted = isBitPayAccepted(url, merchants);
  const merchant = getBitPayMerchantFromUrl(url, merchants);
  const promptAtCheckout = await get<boolean>('promptAtCheckout');
  const shouldPromptAtCheckout = typeof promptAtCheckout === 'undefined' ? true : promptAtCheckout;
  if (merchant /*&& tab*/ && shouldPromptAtCheckout) {
    sendMessageToTab({ merchant, name: 'SHOW_WIDGET_IN_PAY_MODE' }/*, tab*/);
  }
  await setIcon(bitpayAccepted || isGiftCardInvoice(url));
  await refreshCachedMerchantsIfNeeded();
  if (isGiftCardInvoice(url)) return;
  dispatchEvent({
    action: `setExtensionIcon:${
      merchant ? `active:${merchant.hasDirectIntegration ? 'direct' : 'giftCard'}` : `inactive`
    }`
  });
}

async function createClientIdIfNotExists(): Promise<void> {
  const clientId = await get<string>('clientId');
  const analyticsClientId = await get<string>('analyticsClientId');
  if (!clientId) {
    await set<string>('clientId', uuid.v4());
  }
  if (!analyticsClientId) {
    await set<string>('analyticsClientId', uuid.v4());
  }
  clientId ? dispatchEvent({ action: 'updatedExtension' }) : dispatchEvent({ action: 'installedExtension' });
}

//browser.browserAction.onClicked.addListener(async tab => {
//window.addEventListener("click", async (event: MouseEvent) => {
//  //const merchant = tab.url && getBitPayMerchantFromUrl(tab.url, await getCachedMerchants());
//  const merchants = await getCachedMerchants();
//  const merchant = getBitPayMerchantFromUrl(window.location.href, merchants);
//  await browser.runtime
//    .sendMessage(undefined, {
//      name: 'EXTENSION_ICON_CLICKED',
//      merchant
//    });
//    //.catch(() => browser.tabs.create({ url: `${process.env.API_ORIGIN}/extension?launchExtension=true` }));
//});

window.onload = async () => {
//browser.runtime.onInstalled.addListener(async () => {
//  const allTabs = await browser.tabs.query({});
//  allTabs.forEach(tab =>
//    browser.tabs.executeScript(tab.id, { file: 'js/contentScript.bundle.js' }).catch(() => undefined)
//  );
  await Promise.all([refreshCachedMerchantsIfNeeded(), createClientIdIfNotExists()]);
};

async function launchWindowAndListenForEvents({
  url/*,
  height = 735,
  width = 430*/
}: {
  url: string;
  //height: number;
  //width: number;
}): Promise<GiftCardInvoiceMessage> {
  //const { id, height: winHeight, width: winWidth } = await browser.windows.create({
  popupWindow = window.open(
    url,
    "invoice-frame"
  );

  const popupRoot = document.getElementById("app-root")!;
  const invoiceArea = document.getElementById("invoice-area")!;
  const invoiceFrame = document.getElementById("invoice-frame")! as HTMLIFrameElement;
  const cancelButton = document.getElementById("cancel-button")!;

  popupRoot.classList.add("hidden");
  invoiceArea.classList.remove("hidden");

  const promise = new Promise<GiftCardInvoiceMessage>(resolve => {
    giftCardInvoiceCallback = (data: any) : GiftCardInvoiceMessage => {
        resolve(data);

        // return to main screen
        invoiceArea.classList.add("hidden");
        popupRoot.classList.remove("hidden");
        invoiceFrame.src = '';

        return data;
    };
  });

  cancelButton.onclick = () => {
      const resolveFn = giftCardInvoiceCallback;
      return resolveFn && resolveFn({ data: { status: 'closed' } });
  };

  const message = await promise;
  return message;
}

async function pairBitpayId(payload: { secret: string; code?: string }): Promise<void> {
  await generatePairingToken(payload);
}

var contentScriptCallbacks: ((message: any, sender: any) => any)[] = [];

var browser: {[k: string]: any} = {};
browser.runtime = {}
//browser.runtime.getURL = (path: string) => {
//    return window.location.href;
//}
browser.runtime.onMessage = {};
browser.runtime.onMessage.addListener = (callback: (message: any, sender: any) => any) => {
    contentScriptCallbacks.push(callback);
};
browser.runtime.sendMessage = async (extensionId?: string, message: any, sender: object) => {
  //const { tab } = sender;
  if (extensionId !== undefined && message === undefined) {
    message = extensionId;
  }
  console.log(JSON.stringify(message) + " --- " + JSON.stringify(sender));
  switch (message && message.name) {
    case 'LAUNCH_TAB':
      //return browser.tabs.create({ url: message.url });
      console.log("LAUNCH_TAB: " + message.url);
      return;
    case 'LAUNCH_WINDOW':
      //return tab && launchWindowAndListenForEvents(message);
      console.log("LAUNCH_WINDOW");
      return launchWindowAndListenForEvents(message);
    case 'ID_CONNECTED': {
      console.log("ID_CONNECTED: " + message);
      //const resolveFn = windowIdResolveMap[tab?.windowId as number];
      const resolveFn = giftCardInvoiceCallback;
      giftCardInvoiceCallback = undefined;
      //browser.tabs.remove(tab?.id as number).catch(() => {
      //  if (tab?.id) {
      //    browser.tabs.executeScript(tab?.id as number, { code: 'window.close()' });
      //  }
      //});
      popupWindow.close();
      await pairBitpayId(message.data);
      return resolveFn && resolveFn({ data: { status: 'complete' } });
    }
    case 'INVOICE_EVENT': {
      console.log("INVOICE_EVENT");
      if (!message.data || !message.data.status) {
        return;
      }
      const resolveFn = giftCardInvoiceCallback;
      return resolveFn && resolveFn(message);
      return;
    }
    case 'REDIRECT':
      //return browser.tabs.update({
      //  url: message.url
      //});
      console.log("REDIRECT: " + message.url);
      return;
    case 'REFRESH_MERCHANT_CACHE':
      console.log("REFRESH_MERCHANT_CACHE");
      return refreshCachedMerchants();
    case 'TRACK':
      console.log("TRACK: " + message.event);
      return dispatchEvent(message.event);
    case 'URL_CHANGED':
      console.log("URL_CHANGED: " + message.url);
      //return message.url && handleUrlChange(message.url, tab);
      return message.url && handleUrlChange(message.url);
    default:
      //return tab && sendMessageToTab(message, tab);
      return sendMessageToTab(message);
  }
};

export { browser };


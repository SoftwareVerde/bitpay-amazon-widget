//import { browser } from 'webextension-polyfill-ts';
import { CardConfig } from './gift-card.types';

export const launchNewTab = (url: string): void => {
  // TODO: replace
  console.log("services/browser.ts: launchNewTab: " + url);
  //browser.runtime.sendMessage({
  //  name: 'LAUNCH_TAB',
  //  url
  //});
};

export const goToPage = (link: string): void => {
  const detectProtocolPresent = /^https?:\/\//i;
  const url = detectProtocolPresent.test(link) ? link : `https://${link}`;
  // TODO: replace
  console.log("services/browser.ts: goToPage: " + url);
  //browser.runtime.sendMessage({
  //  name: 'REDIRECT',
  //  url
  //});
};

export const dispatchUrlChange = (window: Window): void => {
  // TODO: replace
  console.log("services/browser.ts: dispatchUrlChange: " + window);
  //browser.runtime.sendMessage(undefined, {
  //  name: 'URL_CHANGED',
  //  url: window.location.href
  //});
};

export const dispatchAnalyticsEvent = (event: { [key: string]: string }): void => {
  // TODO: replace
  console.log("services/browser.ts: dispatchAnalyticsEvent: " + event.string);
  //browser.runtime.sendMessage(undefined, {
  //  name: 'TRACK',
  //  event
  //});
};

export const injectClaimInfo = (cardConfig: CardConfig, claimInfo: { claimCode: string; pin?: string }): void => {
  // TODO: replace
  console.log("services/browser.ts: injectClaimInfo: " + claimInfo.claimCode + " " + cardConfig);
  //browser.runtime.sendMessage(undefined, {
  //  name: 'INJECT_CLAIM_INFO',
  //  cssSelectors: cardConfig.cssSelectors,
  //  claimInfo
  //});
};

export const refreshMerchantCache = (): void => {
  // TODO: replace
  console.log("services/browser.ts: refreshMerchantCache");
  //browser.runtime.sendMessage(undefined, {
  //  name: 'REFRESH_MERCHANT_CACHE'
  //});
};

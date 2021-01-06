//import { browser } from 'webextension-polyfill-ts';

function getKeyString(key: string): string {
  return process.env.NODE_ENV === 'production' || process.env.API_ORIGIN === 'https://bitpay.com'
    ? key
    : `${process.env.API_ORIGIN}_${key}`;
}

export async function get<T>(key: string): Promise<T> {
  //const keys = await browser.storage.local.get(getKeyString(key));
  //return keys[getKeyString(key)];
  var value = window.localStorage.getItem(getKeyString(key));
  if (value === null) {
        return undefined as any;
  }
  return JSON.parse(value);
}

export function set<T>(key: string, value: T): Promise<void> {
  //return browser.storage.local.set({ [getKeyString(key)]: value });
  window.localStorage.setItem(getKeyString(key), JSON.stringify(value));
  return Promise.resolve();
}

export function remove(key: string): Promise<void> {
  window.localStorage.removeItem(getKeyString(key));
  return Promise.resolve();
  //return browser.storage.local.remove(getKeyString(key));
}

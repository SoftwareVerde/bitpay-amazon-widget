#!/bin/bash

BASE_DIR="$(dirname $0)/../"
cd $BASE_DIR

mkdir -p out/www
rm -rf out/www/*

export PATH=${PATH}:$(npm bin)

# build the bitpay web application
cd bitpay-web
yarn build
cd -
cp -R bitpay-web/dist/* out/www/

# rename the bitpay css files to facilitate being used as iframe widget 
mv out/www/index.html out/www/bitpay.html
mv out/www/css/main.css out/www/css/bitpay.css
sed -i.tmp -e 's/\/css\/main.css/\/css\/bitpay.css/' out/www/bitpay.html
rm out/www/bitpay.html.tmp

cp -R www/* out/www/.

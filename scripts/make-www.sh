rm -rf out/www/*
cd www/
ng build && \
    cd - && \
    cp -R www/dist/bch-gift-cards/* out/www

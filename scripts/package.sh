#!/bin/bash

BASE_DIR="$(dirname $0)/../"
cd $BASE_DIR

rm -rf out
mkdir out

[ -f README ] && cp README out/.

LOGS_DIR="out/logs/"
mkdir -p $LOGS_DIR
chmod -R 770 $LOGS_DIR

cp -R build/libs out/bin
cp -R conf out/conf

./scripts/make-www.sh

jarPath="$(ls out/bin/bitcoin-cash-gift-cards-*.jar | head -n 1)"
jarName="$(basename $jarPath)"

echo "#!/bin/bash" > out/run.sh
echo "cd \$(dirname \$0)" >> out/run.sh
echo "exec java -jar ./bin/$jarName conf/server.conf \$@" >> out/run.sh
chmod 770 out/run.sh


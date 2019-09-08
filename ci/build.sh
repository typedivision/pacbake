#!/bin/sh -e
CMDPATH=$(cd "$(dirname $0)" && pwd)

. "$CMDPATH"/setup.in

echo "==> build all"

bitbake world || EXIT=1

echo "==> store buildlog"

PACBASE=$(bitbake -e | grep "^PACBASE=" | cut -d= -f2 | xargs)
DEPLOY=$(bitbake -e | grep "^DEPLOY=" | cut -d= -f2 | xargs)

cd "$PACBASE"

mkdir buildlog
find . -path "*/temp/log/*" -exec cp --parent {} buildlog \;
tar -czf "$DEPLOY"/buildlog.tar.gz -C buildlog .

exit $EXIT

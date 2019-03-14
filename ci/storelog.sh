#!/bin/sh -e
#
# CI store logfiles script
#
CMDPATH=$(cd "$(dirname $0)" && pwd)
REPODIR=$(realpath "$CMDPATH"/..)

PATH="$REPODIR"/bitbake/bin:$PATH

cd "$REPODIR"/build

PACBASE=$(bitbake -e | grep "^PACBASE=" | cut -d= -f2 | xargs)
DEPLOY=$(bitbake -e | grep "^DEPLOY=" | cut -d= -f2 | xargs)

cd "$PACBASE"

mkdir buildlog
find . -path "*/temp/log/*" -exec cp --parent {} buildlog \;
tar -czf "$DEPLOY"/buildlog.tar.gz -C buildlog .

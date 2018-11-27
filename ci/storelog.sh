#!/bin/sh -e
#
# CI store logfiles script
#
CMDPATH=$(cd "$(dirname $0)" && pwd)
BASEDIR=$(realpath $CMDPATH/..)

PATH=$BASEDIR/bitbake/bin:$PATH

cd $BASEDIR/build

PROGRESS_DIR=$(bitbake -e | grep "^BB_DIR=" | cut -d= -f2 | xargs)
DEPLOY_DIR=$(bitbake -e | grep "^DEPLOY=" | cut -d= -f2 | xargs)

cd $PROGRESS_DIR

mkdir work.store
find . -path "*/temp/log/*" -exec cp --parent {} work.store \;
tar -czf $DEPLOY_DIR/work.tar.gz -C work.store .

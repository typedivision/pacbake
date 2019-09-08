#!/bin/sh -e
CMDPATH=$(cd $(dirname $0) && pwd)
BASEDIR=${CMDPATH%/*}
PROJECT=pacstage

echo "==> create docker image"

cd $CMDPATH
docker build --tag $PROJECT .

echo "==> run $PROJECT container"

docker run -it --rm --privileged -v $BASEDIR:/base -w /base $PROJECT $@

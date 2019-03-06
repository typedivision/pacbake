#!/bin/sh -e
#
# CI build script
#
CMDPATH=$(cd "$(dirname $0)" && pwd)
BASEDIR=$(realpath $CMDPATH/..)

STORE=/store
PKGCACHE=$STORE/pkgcache

PATH=$BASEDIR/bitbake/bin:$PATH

cd $BASEDIR
./pacstage-init.sh -C "$PKGCACHE"

cd build
{
  echo 'PKGCACHE   = "'$PKGCACHE'"'
  echo 'DL_DIR_TOP = "'$STORE'"'
  echo 'SSTATE_TOP = "'$STORE'"'
} > conf/local.conf

bitbake world

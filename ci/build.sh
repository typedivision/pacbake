#!/bin/sh -e
#
# CI build script
#
CMDPATH=$(cd "$(dirname $0)" && pwd)
BASEDIR=$(realpath $CMDPATH/..)

PATH=$BASEDIR/bitbake/bin:$PATH

cd $BASEDIR

./pacstage-init.sh -C /tmpstore/pkgcache

cd build
{
  echo 'PCACHE = "/tmpstore/pkgcache"'
  echo 'DL_DIR = "/tmpstore/pacDownloads"'
  echo 'SSTATE = "/tmpstore/pacState"'
} > conf/local.conf

bitbake image-busybox
bitbake image-basic

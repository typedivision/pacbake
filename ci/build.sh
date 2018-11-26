# CI build script
CMDPATH=$(cd "$(dirname $0)" && pwd)
BASEDIR=$(realpath $CMDPATH/..)

PATH=$BASEDIR/bitbake/bin:$PATH

cd $BASEDIR

./pacstage-init.sh -C /tmpstore/pkgcache

cd build
{
  echo 'PCACHE = "/tmpstore/pkgcache"'
  echo 'DL_DIR = "/tmpstore/bbDownload"'
  echo 'SSTATE = "/tmpstore/bbState"'
} > conf/local.conf

bitbake image-busybox
bitbake image-basic

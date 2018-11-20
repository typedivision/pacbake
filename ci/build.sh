# CI build script
CMDPATH=$(cd "$(dirname $0)" && pwd)
BASEDIR=$(realpath $CMDPATH/..)

PATH=$BASEDIR/bitbake/bin:$PATH

cd $BASEDIR
./pacstage-init.sh

cd build
bitbake image-busybox
bitbake image-basic

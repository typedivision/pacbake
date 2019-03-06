#!/bin/sh -e
#
# Initialize the pacstage build environment
#
CMDPATH=$(cd "$(dirname $0)" && pwd)

if [ "$1" = "-h" ] || [ "$1" = "--help" ]; then
  echo "Initialize the pacstage build environment."
  echo "usage: ${0##*/} [-C <pkgcache>]"
  echo && exit 0
fi

if ! [ "$(command -v pacman)" ]; then
  echo "==> ERROR: the pacstage build environment needs an archlinux base system to run"
  exit 1
fi

while getopts 'C:' opt; do
  case $opt in
    C) PKGCACHE=$OPTARG;;
    ?) exit 1
  esac
done

PACOPTS="--noconfirm --needed"

if [ "$PKGCACHE" ]; then
  PACOPTS+=" --cachedir $PKGCACHE"
fi

echo "==> Install host tools"
pacman -Sy $PACOPTS arch-install-scripts python3 wget ca-certificates tar gzip bzip2 xz unzip git bubblewrap tmux vim

BUILD_DIR="$CMDPATH"/build
if ! [ -d "$BUILD_DIR" ]; then
  echo "==> Create build dir"
  mkdir -p "$BUILD_DIR"/conf
  echo 'BBLAYERS = "${TOPDIR}/../meta"' > "$BUILD_DIR"/conf/bblayers.conf
fi

echo ">"
echo "> The pacstage build environment setup is done"
if ! [ "$(command -v bitbake)" ]; then
  echo "> You may need to set    PATH=$CMDPATH/bitbake/bin:\$PATH"
fi
echo "> move on and go to      $BUILD_DIR"
echo "> build a single recipe  bitbake <recipe>"
echo "> build just everything  bitbake world"
echo "> get a list of recipes  bitbake -s"
echo ">"

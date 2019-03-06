PD = "Utilities for rescue and embedded systems"
PV = "1.28.1"

HOMEPAGE = "https://www.busybox.net"
LICENSE = "GPL"

SRC_URI = " \
  https://www.busybox.net/downloads/${P}-${PV}.tar.bz2;md5sum=928919a21e34d5c5507d872a4fb7b9f4 \
  file://busybox.config \
  file://pkg \
"

HOST_DEPENDS = "make gcc"
DEPENDS = "crosstool-ng"

inherit pacman

step_devshell() {
  step_build devshell
}

step_build() {
  cd "${SRCDIR}"

  export CROSS_COMPILE="${TARGET_SYS}-"
  cat "${SRCBASE}"/busybox.config > .config

  if [ "$1" = devshell ]; then
    exec bash
  fi

  make
  make install
}

step_package() {
  cp -r "${SRCDIR}"/_install/. "${PKGDIR}"
  cp -r "${SRCBASE}"/pkg/. "${PKGDIR}"

  install -Dm644 "${SRCDIR}"/LICENSE -t "${LICDIR}"
}

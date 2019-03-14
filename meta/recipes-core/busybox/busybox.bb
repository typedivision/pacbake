PD = "Utilities for rescue and embedded systems"
PV = "1.28.1"

HOMEPAGE = "https://www.busybox.net"
LICENSE = "GPL"

SRC_URI = " \
  https://www.busybox.net/downloads/${S}.tar.bz2;md5sum=928919a21e34d5c5507d872a4fb7b9f4 \
  file://busybox.config \
  file://pkgfiles \
"

HOST_DEPENDS = "gcc"
DEPENDS = "crosstool-ng"

step_devshell() {
  step_build devshell
}

step_build() {
  cd "${SRCDIR}"/${S}

  export CROSS_COMPILE="${TARGET_SYS}-"
  cat "${SRCDIR}"/busybox.config > .config

  if [ "$1" = devshell ]; then
    exec bash
  fi

  make
  make install
}

step_install() {
  cd "${SRCDIR}"/${S}
  cp -a _install/. "${FILES_PKG}"
  cp -a "${SRCDIR}"/pkgfiles/. "${FILES_PKG}"

  install_license LICENSE "${FILES_PKG}"
}

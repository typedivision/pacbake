PD = "The Open Source toolkit for Secure Sockets Layer and Transport Layer Security"
PV = "1.0.2p"

HOMEPAGE = "https://www.openssl.org"
LICENSE = "BSD"

SRC_URI = " \
  https://www.openssl.org/source/${P}-${PV}.tar.gz;md5sum=ac5eb30bf5798aa14b1ae6d0e7da58df \
"

HOST_DEPENDS = "make"
DEPENDS = "crosstool-ng"

inherit pacman

step_build() {
  cd "${SRCDIR}"

  [ ${TARGET_ARCH} == aarch64 ] && target='linux-aarch64'

  ./Configure --prefix="${XROOT}"/usr \
    shared $target

  make
  make INSTALL_PREFIX="${SRCBASE}"/setup install
  cp -a "${SRCBASE}"/setup/. "${FILES_SETUP}"
}

step_package() {
  cd "${SRCBASE}"/setup/${XROOT}
  find usr/lib -name "*.so*" -exec cp --parents -a {} "${PKGDIR}" \;

  install -Dm644 "${SRCDIR}"/LICENSE -t "${LICDIR}"
}

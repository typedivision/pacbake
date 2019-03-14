PD = "The Open Source toolkit for Secure Sockets Layer and Transport Layer Security"
PV = "1.0.2p"

HOMEPAGE = "https://www.openssl.org"
LICENSE = "BSD"

SRC_URI = " \
  https://www.openssl.org/source/${S}.tar.gz;md5sum=ac5eb30bf5798aa14b1ae6d0e7da58df \
"

DEPENDS = "crosstool-ng"

step_build() {
  cd "${SRCDIR}"/${S}

  [ ${TARGET_ARCH} == aarch64 ] && target='linux-aarch64'

  ./Configure --prefix="${SDK_SYSROOT}"/usr \
    shared $target

  make
}

step_install() {
  cd "${SRCDIR}"/${S}
  make INSTALL_PREFIX="${FILES_DEVEL}" install

  cd "${FILES_DEVEL}"/${SDK_SYSROOT}
  find usr/lib -name "*.so*" -exec cp --parents {} "${FILES_PKG}" \;

  install_license "${SRCDIR}"/${S}/LICENSE "${FILES_PKG}"
}

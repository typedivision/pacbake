PD = "A Massively Spiffy Yet Delicately Unobtrusive Compression Library"
PV = "1.2.11"

HOMEPAGE = "http://zlib.net"
LICENSE = "custom"

SRC_URI = " \
  ${HOMEPAGE}/${S}.tar.gz;md5sum=1c9f62f0778697a09d36121ead88e08e \
"

DEPENDS = "crosstool-ng"

step_build() {
  cd "${SRCDIR}"/${S}

  ./configure --prefix=${SDK_SYSROOT}/usr

  make
}

step_install() {
  cd "${SRCDIR}"/${S}
  make DESTDIR="${FILES_DEVEL}" install

  cd "${FILES_DEVEL}"/${SDK_SYSROOT}
  find usr/lib -name "*.so*" -exec cp --parents {} "${FILES_PKG}" \;

  cd "${SRCDIR}"/${S}
  grep -A 24 '^  Copyright' zlib.h > LICENSE
  install_license LICENSE "${FILES_PKG}"
}

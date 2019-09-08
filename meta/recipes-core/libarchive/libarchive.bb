PD = "Library that can create and read several streaming archive formats"
PV = "3.3.3"

HOMEPAGE = "http://libarchive.org"
LICENSE = "BSD"

SRC_URI = " \
  https://libarchive.org/downloads/${S}.tar.gz;md5sum=4038e366ca5b659dae3efcc744e72120 \
"

DEPENDS = "crosstool-ng"

step_build() {
  cd "${SRCDIR}"/${S}

  ./configure \
    --host=${TARGET_SYS} \
    --prefix=${SDK_SYSROOT}/usr \
    --without-xml2 \
    --without-expat

  make
}

step_install() {
  cd "${SRCDIR}"/${S}
  make DESTDIR="${FILES_DEV}" install

  cd "${FILES_DEV}"/${SDK_SYSROOT}
  find usr/lib -name "*.so*" -exec cp --parents {} "${FILES_PKG}" \;

  install_license "${SRCDIR}"/${S}/COPYING "${FILES_PKG}"
}

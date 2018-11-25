PD = "Library that can create and read several streaming archive formats"
PV = "3.3.3"

HOMEPAGE = "http://libarchive.org"
LICENSE = "BSD"

SRC_URI = " \
  https://libarchive.org/downloads/${P}-${PV}.tar.gz;md5sum=4038e366ca5b659dae3efcc744e72120 \
"

HOST_DEPENDS = "make"
DEPENDS = "crosstool-ng"

inherit pacman

step_build() {
  cd "${SRCDIR}"

  ./configure \
    --host=${TARGET_SYS} \
    --prefix=${SDK_SYSROOT}/usr \
    --without-xml2 \
    --without-expat

  make
  make DESTDIR="${SRCBASE}"/setup install
  cp -a "${SRCBASE}"/setup/. "${FILES_SETUP}"
}

step_package() {
  cd "${SRCBASE}"/setup/${SDK_SYSROOT}
  find usr/lib -name "*.so*" -exec cp --parents -a {} "${PKGDIR}" \;

  install -Dm644 "${SRCDIR}"/COPYING -t "${LICDIR}"
}

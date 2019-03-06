PD = "Library for applications dealing with netlink sockets"
PV = "3.4.0"

HOMEPAGE = "https://github.com/thom311/libnl/"
LICENSE = "GPL"

SRC_URI = " \
  https://github.com/thom311/libnl/releases/download/${P}${@d.getVar('PV').replace('.','_')}/${P}-${PV}.tar.gz;md5sum=8f71910c03db363b41e2ea62057a4311 \
"

HOST_DEPENDS = "make flex bison"

DEPENDS = "crosstool-ng"

inherit pacman

step_build() {
  cd "${SRCDIR}"

  ./configure \
    --host=${TARGET_SYS} \
    --prefix=${SDK_SYSROOT}/usr \
    --sysconfdir=${SDK_SYSROOT}/etc \
    --disable-static

  make DESTDIR="${SRCBASE}"/setup install
  cp -a "${SRCBASE}"/setup/. "${FILES_SETUP}"
}

step_package() {
  cd "${SRCBASE}"/setup/${SDK_SYSROOT}
  find usr/lib -name "*.so*" -exec cp --parents -a {} "${PKGDIR}" \;

  install -Dm644 "${SRCDIR}"/COPYING -t "${LICDIR}"
}

PD = "Library for applications dealing with netlink sockets"
PV = "3.4.0"

HOMEPAGE = "https://github.com/thom311/libnl/"
LICENSE = "GPL"

SRC_URI = " \
  https://github.com/thom311/libnl/releases/download/${P}${@d.getVar('PV').replace('.','_')}/${S}.tar.gz;md5sum=8f71910c03db363b41e2ea62057a4311 \
"

HOST_DEPENDS = "flex bison"
DEPENDS = "crosstool-ng"

step_build() {
  cd "${SRCDIR}"/${S}

  ./configure \
    --host=${TARGET_SYS} \
    --prefix=${SDK_SYSROOT}/usr \
    --sysconfdir=${SDK_SYSROOT}/etc \
    --disable-static

  make
}

step_install() {
  cd "${SRCDIR}"/${S}
  make DESTDIR="${FILES_DEVEL}" install

  cd "${FILES_DEVEL}"/${SDK_SYSROOT}
  find usr/lib -name "*.so*" -exec cp --parents {} "${FILES_PKG}" \;

  install_license "${SRCDIR}"/${S}/COPYING "${FILES_PKG}"
}

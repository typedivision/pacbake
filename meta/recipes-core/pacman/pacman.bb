PD = "A library-based package manager with dependency support"
PV = "5.1.1"

HOMEPAGE = "http://www.archlinux.org/pacman/"
LICENSE = "GPL"

SRC_URI = " \
  https://sources.archlinux.org/other/${P}/${S}.tar.gz;md5sum=4da799005fe4d8c6f13fd80a4f67e96f \
"

DEPENDS = "crosstool-ng"
RDEPENDS = "libarchive openssl"

step_build() {
  cd "${SRCDIR}"/${S}

  ./configure \
    --host=${TARGET_SYS} \
    --prefix=/usr \
    --sysconfdir=/etc \
    --localstatedir=/var \
    --without-libcurl \
    --disable-doc

  make
}

step_install() {
  cd "${SRCDIR}"/${S}
  make DESTDIR="${SRCDIR}"/destdir install

  cd "${SRCDIR}"/destdir
  install -D usr/bin/pacman -t "${FILES_PKG}"/usr/bin
  install -D etc/pacman.conf -t "${FILES_PKG}"/etc
  find usr/lib -name "*.so*" -exec cp --parents {} "${FILES_PKG}" \;

  install_license "${SRCDIR}"/${S}/COPYING "${FILES_PKG}"
}

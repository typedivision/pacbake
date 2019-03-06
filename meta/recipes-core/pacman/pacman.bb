PD = "A library-based package manager with dependency support"
PV = "5.1.1"

HOMEPAGE = "http://www.archlinux.org/pacman/"
LICENSE = "GPL"

SRC_URI = " \
  https://sources.archlinux.org/other/${P}/${P}-${PV}.tar.gz;md5sum=4da799005fe4d8c6f13fd80a4f67e96f \
"

HOST_DEPENDS = "make pkgconfig"

DEPENDS = "crosstool-ng"
RDEPENDS = "libarchive openssl"

inherit pacman

step_build() {
  cd "${SRCDIR}"

  ./configure \
    --host=${TARGET_SYS} \
    --prefix=/usr \
    --sysconfdir=/etc \
    --localstatedir=/var \
    --without-libcurl \
    --disable-doc

  make
  make -C "${SRCDIR}" DESTDIR="${SRCBASE}"/setup install
}

step_package() {
  cd "${SRCBASE}"/setup
  install -D usr/bin/pacman -t "${PKGDIR}"/usr/bin
  install -D etc/pacman.conf -t "${PKGDIR}"/etc
  find usr/lib -name "*.so*" -exec cp --parents -a {} "${PKGDIR}" \;

  install -Dm644 "${SRCDIR}"/COPYING -t "${LICDIR}"
}

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
}

step_package() {
  make -C "${SRCDIR}" DESTDIR="${PKGDIR}" install
  rm -r "${PKGDIR}"/usr/{share,include,lib/pkgconfig}

  install -Dm644 "${SRCDIR}"/COPYING -t "${LICDIR}"
}

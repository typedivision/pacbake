PD = "nl80211 based CLI configuration utility for wireless devices"
PV = "5.0.1"

HOMEPAGE = "https://wireless.kernel.org/en/users/Documentation/iw"
LICENSE = ""

SRC_URI = " \
  https://www.kernel.org/pub/software/network/iw/iw-${PV}.tar.xz;md5sum=04d9ca2f20a11213985cea0c53bb3928 \
"

HOST_DEPENDS = "make pkgconfig"

DEPENDS = "crosstool-ng"
RDEPENDS = "libnl"

inherit pacman

step_build() {
  cd "${SRCDIR}"
  make
}

step_package() {
  cd "${SRCDIR}"
  make DESTDIR="${PKGDIR}" SBINDIR="/usr/bin" install
  rm -rf "${PKGDIR}"/usr/share/man

  install -Dm644 "${SRCDIR}"/COPYING -t "${LICDIR}"
}

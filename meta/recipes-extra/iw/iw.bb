PD = "nl80211 based CLI configuration utility for wireless devices"
PV = "5.0.1"

HOMEPAGE = "https://wireless.kernel.org/en/users/Documentation/iw"
LICENSE = "GPL"

SRC_URI = " \
  https://www.kernel.org/pub/software/network/${P}/${S}.tar.xz;md5sum=04d9ca2f20a11213985cea0c53bb3928 \
"

DEPENDS = "crosstool-ng"
RDEPENDS = "libnl"

step_build() {
  cd "${SRCDIR}"/${S}
  make
}

step_install() {
  cd "${SRCDIR}"/${S}

  make DESTDIR="${FILES_PKG}" SBINDIR="/usr/bin" install
  rm -rf "${FILES_PKG}"/usr/share/man

  install_license COPYING "${FILES_PKG}"
}

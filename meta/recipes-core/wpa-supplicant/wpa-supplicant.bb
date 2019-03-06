PD = "A utility providing key negotiation for WPA wireless networks"
PV = "2.6"

HOMEPAGE = "http://hostap.epitest.fi/wpa_supplicant"
LICENSE = "GPL"

SRC_URI = " \
  https://w1.fi/releases/wpa_supplicant-${PV}.tar.gz;md5sum=091569eb4440b7d7f2b4276dbfc03c3c \
  file://${P}.config \
"

HOST_DEPENDS = "make pkgconfig"

DEPENDS = "crosstool-ng libnl openssl"
RDEPENDS = "libnl openssl"

inherit pacman

step_build() {
  cd "${SRCBASE}/wpa_supplicant-${PV}/wpa_supplicant"

  cat "${SRCBASE}"/${P}.config > .config

  make 
}

step_package() {
  cd "${SRCBASE}/wpa_supplicant-${PV}"
  make -C wpa_supplicant DESTDIR="${PKGDIR}" BINDIR=/usr/bin install

  install -Dm644 COPYING -t "${LICDIR}"
}

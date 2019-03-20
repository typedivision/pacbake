PD = "A utility providing key negotiation for WPA wireless networks"
PV = "2.6"

HOMEPAGE = "http://hostap.epitest.fi/wpa_supplicant"
LICENSE = "GPL"

SRC_URI = " \
  https://w1.fi/releases/wpa_supplicant-${PV}.tar.gz;md5sum=091569eb4440b7d7f2b4276dbfc03c3c \
  file://wpa-supplicant.config \
"

DEPENDS = "crosstool-ng libnl openssl"
RDEPENDS = "libnl openssl"

step_build() {
  cd "${SRCDIR}/wpa_supplicant-${PV}/wpa_supplicant"

  cat "${SRCDIR}"/wpa-supplicant.config > .config
  make
}

step_install() {
  cd "${SRCDIR}/wpa_supplicant-${PV}"
  make -C wpa_supplicant DESTDIR="${FILES_PKG}" BINDIR=/usr/bin install

  install_license COPYING "${FILES_PKG}"
}

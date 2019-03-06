PD = "Base system files"

SRC_URI = " \
  file://config \
  file://files \
"

DEPENDS = "kconfig-frontends-native"
RDEPENDS = "busybox e2fsprogs neoinit kconfig-frontends"

inherit pacman

step_build() {
  cd "${SRCBASE}"/config
  kconfig-conf --olddefconfig Kconfig
}

step_package() {
  cp -r "${SRCBASE}"/files/. "${PKGDIR}"

  cp -r "${SRCBASE}"/config "${PKGDIR}"/etc/config
  ln -s config/.config "${PKGDIR}"/etc/sysconfig

  install -d "${PKGDIR}"/data
  install -d "${PKGDIR}"/volatile
}

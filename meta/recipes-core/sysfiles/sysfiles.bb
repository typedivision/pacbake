PD = "Base system files"

SRC_URI = " \
  file://config \
  file://pkg \
"

DEPENDS = "kconfig-frontends-native"
RDEPENDS = "busybox e2fsprogs neoinit kconfig-frontends wpa-supplicant"

inherit pacman

step_build() {
  cd "${SRCBASE}"/config
  kconfig-conf --olddefconfig Kconfig
}

step_package() {
  cp -r "${SRCBASE}"/pkg/. "${PKGDIR}"

  cp -r "${SRCBASE}"/config "${PKGDIR}"/etc/config
  ln -s config/.config "${PKGDIR}"/etc/sysconfig

  for d in data volatile var/run; do
    install -d "${PKGDIR}"/$d
  done
}

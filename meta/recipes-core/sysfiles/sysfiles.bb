PD = "Base system files"

SRC_URI = " \
  file://config \
  file://pkgfiles \
"

DEPENDS = "kconfig-frontends-native"
RDEPENDS = "busybox e2fsprogs neoinit kconfig-frontends wpa-supplicant"

step_install() {
  cd "${SRCDIR}"/config
  kconfig-conf --olddefconfig Kconfig

  install -d "${FILES_PKG}"/etc
  cp -a "${SRCDIR}"/config "${FILES_PKG}"/etc/config

  mv "${FILES_PKG}"/etc/config/.config "${FILES_PKG}"/etc/sysconfig
  ln -s ../sysconfig "${FILES_PKG}"/etc/config/.config

  install -d "${FILES_PKG}"/var/run
  install -d "${FILES_PKG}"/var/log
  install -d "${FILES_PKG}"/local/data
  install -d "${FILES_PKG}"/local/volatile

  cp -a "${SRCDIR}"/pkgfiles/. "${FILES_PKG}"
}

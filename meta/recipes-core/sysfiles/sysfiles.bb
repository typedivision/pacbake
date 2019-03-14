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

  for dir in etc data volatile var/run; do
    install -d "${FILES_PKG}"/$dir
  done

  cp -a "${SRCDIR}"/config "${FILES_PKG}"/etc/config
  ln -s config/.config "${FILES_PKG}"/etc/sysconfig

  cp -a "${SRCDIR}"/pkgfiles/. "${FILES_PKG}"
}

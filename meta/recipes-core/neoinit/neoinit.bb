PD = "a small yet feature-complete init"
PV = "${PV_SRC}"

HOMEPAGE = "https://github.com/typedivision/neoinit"
LICENSE = "GPL"

SRC_URI = " \
  git://github.com/typedivision/neoinit;protocol=https \
"

SRCREV = "559afcbe8a371bea8c7006973754dbdb6d93b7f5"

HOST_DEPENDS = "make"

DEPENDS = "crosstool-ng"

inherit pacman

step_build() {
  cd "${SRCBASE}"/git

  export CROSS=${TARGET_SYS}-
  make
}

step_package() {
  install -d "${PKGDIR}"/{sbin,bin,etc/minit}

  cd "${SRCBASE}"/git
  install minit pidfilehack write_proc hard-reboot "${PKGDIR}"/sbin
  install -m 4750 shutdown "${PKGDIR}"/sbin
  install msvc serdo "${PKGDIR}"/bin
  mkfifo -m 600 "${PKGDIR}"/etc/minit/{in,out}
  ln -s /sbin/minit "${PKGDIR}"/sbin/init

  install -Dm644 "${SRCBASE}"/git/COPYING -t "${LICDIR}"
}

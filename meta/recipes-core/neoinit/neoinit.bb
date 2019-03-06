PD = "a small yet feature-complete init"
PV = "${PV_SRC}"

HOMEPAGE = "https://github.com/typedivision/neoinit"
LICENSE = "GPL"

SRC_URI = " \
  git://github.com/typedivision/neoinit;protocol=https \
"

SRCREV = "8f64c13fbebd4e7b070c65c465e6dcd29270cb54"

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
  install minit hard-reboot "${PKGDIR}"/sbin
  install msvc "${PKGDIR}"/bin
  mkfifo -m 600 "${PKGDIR}"/etc/minit/{in,out}
  ln -s /sbin/minit "${PKGDIR}"/sbin/init

  install -Dm644 "${SRCBASE}"/git/COPYING -t "${LICDIR}"
}

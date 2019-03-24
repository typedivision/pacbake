PD = "a small yet feature-complete init"
PV = "${PV_REV}"

HOMEPAGE = "https://github.com/typedivision/neoinit"
LICENSE = "GPL"

SRC_URI = " \
  git://github.com/typedivision/neoinit;protocol=https \
"

SRCREV = "f091ebc307b038cd743aa109d1faaf2f6f865a40"

DEPENDS = "crosstool-ng"

step_build() {
  cd "${SRCDIR}"/git

  export CROSS=${TARGET_SYS}-
  make
}

step_install() {
  install -d "${FILES_PKG}"/{sbin,bin,etc/minit}

  cd "${SRCDIR}"/git
  install minit hard-reboot "${FILES_PKG}"/sbin
  install msvc "${FILES_PKG}"/bin
  mkfifo -m 600 "${FILES_PKG}"/etc/minit/{in,out}
  ln -s /sbin/minit "${FILES_PKG}"/sbin/init

  install_license "${SRCDIR}"/git/COPYING "${FILES_PKG}"
}

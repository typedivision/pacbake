PD = "a small yet feature-complete init"
PV = "${PV_REV}"

HOMEPAGE = "https://github.com/typedivision/neoinit"
LICENSE = "GPL"

SRC_URI = " \
  git://github.com/typedivision/neoinit;protocol=https \
"

SRCREV = "v1.0.0"

DEPENDS = "crosstool-ng"

step_build() {
  cd "${SRCDIR}"/git

  export CROSS=${TARGET_SYS}-
  make
}

step_install() {
  install -d "${FILES_PKG}"/{sbin,bin,etc/neoinit}

  cd "${SRCDIR}"/git
  install neoinit hard-reboot "${FILES_PKG}"/sbin
  install neorc "${FILES_PKG}"/bin
  mkfifo -m 600 "${FILES_PKG}"/etc/neoinit/{in,out}
  ln -s /sbin/neoinit "${FILES_PKG}"/sbin/init

  install_license "${SRCDIR}"/git/COPYING "${FILES_PKG}"
}

PD = "Versatile (cross-)toolchain generator"
PV = "${PV_SRC}"

HOMEPAGE = "http://crosstool-ng.org"
LICENSE = "GPL"

SRC_URI = " \
  git://github.com/crosstool-ng/crosstool-ng;protocol=https \
  file://${P}_${TARGET_ARCH}.config \
  file://0001-no-license-deployment-for-ct-itself.patch \
"

SRCREV = "616870f619ab97c31466c71b37ca07978dc9ed65"

HOST_DEPENDS = " \
  wget ca-certificates xz unzip \
  autoconf automake make gcc flex bison help2man \
"

inherit pacman
PACKAGES = "sysroot sysdebug"

step_devshell() {
  step_build devshell
}

step_prepare() {
  ## build crosstool-ng
  cd "${SRCBASE}"/git
  patch -Np1 -i "${SRCBASE}"/0001-no-license-deployment-for-ct-itself.patch

  export CC="${CC_BUILD}"
  ./bootstrap
  ./configure --prefix="${SRCBASE}"/local
  make install
}

step_build() {
  ## build toolchain
  mkdir "${SRCBASE}"/build
  cd "${SRCBASE}"/build

  unset CC CXX AR RANLIB
  export CT_DL_DIR="${DL_DIR}"/ct-ng
  export CT_PREFIX="${SDK_PREFIX}"
  export PATH="${SRCBASE}"/local/bin:$PATH

  mkdir -p "$CT_DL_DIR"
  cat "${SRCBASE}"/${P}_${TARGET_ARCH}.config > .config

  if [ "$1" = devshell ]; then
    exec bash
  fi

  ct-ng build
}

step_install() {
  cd ${SDK_PREFIX}
  mkdir -p "${FILES_SETUP}"/${SDK_PREFIX}
  cp -a bin lib libexec "${FILES_SETUP}"/${SDK_PREFIX}
  
  cd ${SDK_PREFIX}/${TARGET_SYS}
  mkdir -p "${FILES_SETUP}"/${SDK_PREFIX}/${TARGET_SYS}
  cp -a bin sysroot "${FILES_SETUP}"/${SDK_PREFIX}/${TARGET_SYS}
}

step_package_sysroot() {
  cd ${SDK_PREFIX}/${TARGET_SYS}/sysroot

  mkdir -p "${PKGDIR}"/lib "${PKGDIR}"/usr/lib
  cp -r lib/. usr/lib/. "${PKGDIR}"/lib
  ln -s lib "${PKGDIR}"/lib64
  ln -s lib "${PKGDIR}"/usr/lib64

  for ext in a o map pc py; do
    find "${PKGDIR}" -name "*.$ext" -exec rm {} \;
  done
  rm -r "${PKGDIR}"/lib/{audit,gconv}
  rm "${PKGDIR}"/lib/lib*san.*

  mkdir -p "${PKGDIR}"/usr/bin
  cp sbin/ldconfig "${PKGDIR}"/usr/bin
  cp usr/bin/ldd "${PKGDIR}"/usr/bin

  mkdir -p "${PKGDIR}"/usr/share/licenses
  for pkg in expat gcc glibc linux ncurses; do
    cp -r ${SDK_PREFIX}/share/licenses/$pkg "${PKGDIR}"/usr/share/licenses
  done

  find "${PKGDIR}" -type d -exec chmod 755 {} \;
}

step_package_sysdebug() {
  cd ${SDK_PREFIX}/${TARGET_SYS}

  mkdir -p "${PKGDIR}"/opt
  cp -r debug-root/usr/bin "${PKGDIR}"/opt

  mkdir -p "${PKGDIR}"/opt/lib
  cp -r sysroot/lib/lib*san.* "${PKGDIR}"/opt/lib

  mkdir -p "${PKGDIR}"/usr/share/licenses
  for pkg in gdb strace; do
    cp -r ${SDK_PREFIX}/share/licenses/$pkg "${PKGDIR}"/usr/share/licenses
  done

  find "${PKGDIR}" -type d -exec chmod 755 {} \;
}

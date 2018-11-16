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
  export TARBALLS_DIR="${DL_DIR}"/ct-ng
  export CT_PREFIX="${XPATH}"
  export PATH="${SRCBASE}"/local/bin:$PATH

  mkdir -p "$TARBALLS_DIR"
  cat "${SRCBASE}"/${P}_${TARGET_ARCH}.config > .config

  if [ "$1" = devshell ]; then
    exec bash
  fi

  ct-ng build
  cp -a --parents "${XPATH}" "${SRCBASE}"
}

step_install() {
  mkdir -p "${FILES_INSTALL}"/${XPATH}
  cp -a "${SRCBASE}"/${XPATH}/bin \ 
        "${SRCBASE}"/${XPATH}/lib \ 
        "${SRCBASE}"/${XPATH}/libexec \ 
        "${FILES_INSTALL}"/${XPATH}
  
  mkdir -p "${FILES_INSTALL}"/${XPATH}/${TARGET_SYS}
  cp -a "${SRCBASE}"/${XPATH}/${TARGET_SYS}/bin \
        "${SRCBASE}"/${XPATH}/${TARGET_SYS}/sysroot \
        "${FILES_INSTALL}"/${XPATH}/${TARGET_SYS}

  cp -a "${FILES_INSTALL}"/${XPATH} "${SYSBASE}"
}

step_package_sysroot() {
  cd "${SRCBASE}"/${XPATH}/${TARGET_SYS}/sysroot

  mkdir -p  "${PKGDIR}"/lib
  cp -r lib/. usr/lib/. "${PKGDIR}"/lib

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
    cp -r "${SRCBASE}"/${XPATH}/share/licenses/$pkg "${PKGDIR}"/usr/share/licenses
  done

  find "${PKGDIR}" -type d -exec chmod 755 {} \;
}

step_package_sysdebug() {
  cd "${SRCBASE}"/${XPATH}/${TARGET_SYS}

  mkdir -p "${PKGDIR}"/opt
  cp -r debug-root/usr/bin "${PKGDIR}"/opt

  mkdir -p "${PKGDIR}"/opt/lib
  cp -r sysroot/lib/lib*san.* "${PKGDIR}"/opt/lib

  mkdir -p "${PKGDIR}"/usr/share/licenses
  for pkg in gdb strace; do
    cp -r "${SRCBASE}"/${XPATH}/share/licenses/$pkg "${PKGDIR}"/usr/share/licenses
  done

  find "${PKGDIR}" -type d -exec chmod 755 {} \;
}

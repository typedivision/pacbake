PD = "Versatile (cross-)toolchain generator"
PV = "${PV_REV}"

HOMEPAGE = "http://crosstool-ng.org"
LICENSE = "GPL"

SRC_URI = " \
  git://github.com/crosstool-ng/crosstool-ng;protocol=https \
  file://crosstool-ng_${TARGET_ALIAS}.config \
  file://0001-no-license-deployment-for-ct-itself.patch \
"

SRCREV = "616870f619ab97c31466c71b37ca07978dc9ed65"

HOST_DEPENDS = " \
  wget ca-certificates xz unzip \
  autoconf automake gcc flex bison help2man \
"

PACKAGES = "sysroot sysdebug"

step_devshell() {
  step_build devshell
}

step_prepare() {
  cd "${SRCDIR}"/git

  # build crosstool-ng
  patch -Np1 -i "${SRCDIR}"/0001-no-license-deployment-for-ct-itself.patch

  export CC="${CC_BUILD}"
  ./bootstrap
  ./configure --prefix="${SRCDIR}"/local
  make install
}

step_build() {
  mkdir "${SRCDIR}"/build
  cd "${SRCDIR}"/build

  # build toolchain
  unset CC CXX AR RANLIB
  export CT_DL_DIR="${DL_DIR}"/ct-ng
  export CT_PREFIX="${SDK_PREFIX}"
  export PATH="${SRCDIR}"/local/bin:$PATH

  mkdir -p "$CT_DL_DIR"
  cat "${SRCDIR}"/crosstool-ng_${TARGET_ALIAS}.config > .config

  if [ "$1" = devshell ]; then
    exec bash
  fi

  ct-ng build
}

step_install() {
  install_devel
  install_sysroot
  install_sysdebug
}

install_devel() {
  cd ${SDK_PREFIX}
  install -d "${FILES_DEVEL}"/${SDK_PREFIX}
  cp -a bin lib libexec "${FILES_DEVEL}"/${SDK_PREFIX}

  cd ${SDK_PREFIX}/${TARGET_SYS}
  install -d "${FILES_DEVEL}"/${SDK_PREFIX}/${TARGET_SYS}
  cp -a bin sysroot "${FILES_DEVEL}"/${SDK_PREFIX}/${TARGET_SYS}
}

install_sysroot() {
  local pkgdir="${FILES_PKG}_sysbase"
  cd ${SDK_PREFIX}/${TARGET_SYS}/sysroot

  install -d "$pkgdir"/lib "$pkgdir"/usr/lib
  cp -a lib/. usr/lib/. "$pkgdir"/lib
  ln -s lib "$pkgdir"/lib64
  ln -s lib "$pkgdir"/usr/lib64

  for ext in a o map pc py spec; do
    find "$pkgdir" -name "*.$ext" -exec rm {} \;
  done
  rm -r "$pkgdir"/lib/{audit,gconv,pkgconfig}
  rm "$pkgdir"/lib/lib*san.*

  install -d "$pkgdir"/usr/bin
  cp sbin/ldconfig "$pkgdir"/usr/bin
  cp usr/bin/ldd "$pkgdir"/usr/bin

  install -d "$pkgdir"/usr/share/licenses
  for pn in expat gcc glibc linux ncurses; do
    cp -a ${SDK_PREFIX}/share/licenses/$pn "$pkgdir"/usr/share/licenses
  done

  find "$pkgdir" -type d -exec chmod 755 {} \;
}

install_sysdebug() {
  local pkgdir="${FILES_PKG}_sysdebug"
  cd ${SDK_PREFIX}/${TARGET_SYS}

  install -d "$pkgdir"/opt/debug
  cp -a debug-root/usr/bin "$pkgdir"/opt/debug

  install -d "$pkgdir"/opt/debug/lib
  cp -a sysroot/lib/lib*san.* "$pkgdir"/opt/debug/lib

  install -d "$pkgdir"/usr/share/licenses
  for pn in gdb strace; do
    cp -a ${SDK_PREFIX}/share/licenses/$pn "$pkgdir"/usr/share/licenses
  done

  find "$pkgdir" -type d -exec chmod 755 {} \;
}

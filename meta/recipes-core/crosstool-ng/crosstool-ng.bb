PD = "Versatile (cross-)toolchain generator"
PV = "${PV_REV}"

HOMEPAGE = "http://crosstool-ng.org"
LICENSE = "GPL"

SRC_URI = " \
  git://github.com/crosstool-ng/crosstool-ng;protocol=https \
  file://crosstool-ng_${TARGET_ALIAS}.config \
"

SRCREV = "b2151f1dba2b20c310adfe7198e461ec4469172b"

HOST_DEPENDS = " \
  wget ca-certificates xz unzip \
  autoconf automake gcc flex bison help2man \
"

PACKAGES = "sysbase sysdebug"

step_devshell() {
  step_build devshell
}

step_prepare() {
  cd "${SRCDIR}"/git

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
  install_sysbase
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

install_sysbase() {
  local pkgdir="${FILES_PKG}_sysbase"
  cd ${SDK_PREFIX}/${TARGET_SYS}/sysroot

  install -d "$pkgdir"/lib "$pkgdir"/usr/lib
  cp -a lib/. "$pkgdir"/lib
  cp -a usr/lib/. "$pkgdir"/usr/lib
  ln -s lib "$pkgdir"/lib64
  ln -s lib "$pkgdir"/usr/lib64

  for ext in a o map pc py spec; do
    find "$pkgdir" -name "*.$ext" -exec rm {} \;
  done
  rm -r "$pkgdir"/usr/lib/pkgconfig
  rm -r "$pkgdir"/usr/lib/terminfo

  install -d "$pkgdir"/usr/share/licenses
  # TODO add musl license
  for pn in expat gcc ncurses; do
    cp -a ${SDK_PREFIX}/share/licenses/$pn "$pkgdir"/usr/share/licenses
  done

  find "$pkgdir" -type d -exec chmod 755 {} \;
}

install_sysdebug() {
  local pkgdir="${FILES_PKG}_sysdebug"
  cd ${SDK_PREFIX}/${TARGET_SYS}

  install -d "$pkgdir"/opt/debug
  cp -a debug-root/usr/bin "$pkgdir"/opt/debug

  install -d "$pkgdir"/usr/share/licenses
  for pn in gdb strace; do
    cp -a ${SDK_PREFIX}/share/licenses/$pn "$pkgdir"/usr/share/licenses
  done

  find "$pkgdir" -type d -exec chmod 755 {} \;
}

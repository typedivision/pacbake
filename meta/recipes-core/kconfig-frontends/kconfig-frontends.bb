PD = "Out of the Linux source tree, packaging of the kconfig infrastructure, ready for use by third party projects"
PV = "4.11.0.1"

HOMEPAGE = "http://ymorin.is-a-geek.org/projects/kconfig-frontends"
LICENSE = "GPL"

SRC_URI = " \
  http://ymorin.is-a-geek.org/download/${P}/${S}.tar.xz;md5sum=ee0d3718b83b519f384ef5f7eae980c5 \
"

HOST_DEPENDS = "flex bison gperf"
HOST_DEPENDS_native = "${HOST_DEPENDS} gcc"

DEPENDS = "crosstool-ng"
DEPENDS_native = ""

step_build() {
  cd "${SRCDIR}"/${S}

  ./configure \
    --host=${TARGET_SYS} \
    --prefix=/ \
    --enable-frontends=conf,mconf

  rm libs/parser/hconf.c
  make
}

step_install() {
  cd "${SRCDIR}"/${S}

  if [ "${PN}" = "${P}-native" ]; then
    make DESTDIR="${FILES_DEV}" install
    return
  fi

  make DESTDIR="${FILES_PKG}" install
  rm -r "${FILES_PKG}"/{share,include}
  rm "${FILES_PKG}"/bin/kconfig-{gettext,diff,merge}
  rm "${FILES_PKG}"/lib/pkgconfig/*.pc

  install_license COPYING "${FILES_LICENSE}"
}

BBCLASSEXTEND = "native"

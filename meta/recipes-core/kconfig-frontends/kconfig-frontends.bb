PD = "Out of the Linux source tree, packaging of the kconfig infrastructure, ready for use by third party projects"
PV = "4.11.0.1"

HOMEPAGE = "http://ymorin.is-a-geek.org/projects/kconfig-frontends"
LICENSE = "GPL"

SRC_URI = " \
  http://ymorin.is-a-geek.org/download/${P}/${P}-${PV}.tar.xz;md5sum=ee0d3718b83b519f384ef5f7eae980c5 \
"

HOST_DEPENDS = "make flex bison gperf"
HOST_DEPENDS_native = "${HOST_DEPENDS} gcc"

DEPENDS = "crosstool-ng"
DEPENDS_native = ""

inherit pacman

step_build() {
  cd "${SRCDIR}"

  ./configure \
    --host=${TARGET_SYS} \
    --prefix=/ \
    --enable-frontends=conf,mconf

  rm libs/parser/hconf.c
  make

  if [ "${PN}" = "${P}-native" ]; then
    make DESTDIR="${FILES_SETUP}" install
  fi
}

step_package() {
  make -C "${SRCDIR}" DESTDIR="${PKGDIR}" install
  rm -r "${PKGDIR}"/{share,include}
  rm "${PKGDIR}"/bin/kconfig-{gettext,diff,merge}
  rm "${PKGDIR}"/lib/pkgconfig/kconfig-parser.pc

  install -Dm644 "${SRCDIR}"/COPYING -t "${LICDIR}"
}

BBCLASSEXTEND = "native"

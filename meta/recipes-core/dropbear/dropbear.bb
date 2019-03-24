PD = "A relatively small SSH server and client"
PV = "2014.66"

HOMEPAGE = "http://matt.ucc.asn.au/dropbear/"
LICENSE = "custom"

SRC_URI = " \
  ${HOMEPAGE}/releases/${S}.tar.bz2;md5sum=c21a01111aa5015db038c6efdb85717d \
"

HOST_DEPENDS = "dropbear"
DEPENDS = "crosstool-ng"
RDEPENDS = "zlib"

step_build() {
  cd "${SRCDIR}"/${S}

  ./configure \
    --host=${TARGET_SYS} \
    --prefix=/usr \
    --disable-wtmp \
    --disable-lastlog

  export PROGRAMS="dropbear dbclient scp"
  make
}

step_install() {
  cd "${SRCDIR}"/${S}
  make DESTDIR="${RESULT}"/pkg install

  cd "${RESULT}"/pkg
  install -D usr/sbin/dropbear -t "${FILES_PKG}"/bin
  install -D usr/bin/dbclient -t "${FILES_PKG}"/usr/bin

  install -d "${FILES_PKG}"/etc/dropbear
  dropbearkey -t rsa -f "${FILES_PKG}"/etc/dropbear/dropbear_rsa_host_key

  install -D /dev/null "${FILES_PKG}"/etc/minit/net.dropbear/run
  touch "${FILES_PKG}"/etc/minit/net.dropbear/respawn
  {
    echo "#!/bin/sh"
    echo "exec /bin/dropbear -F -B"
  } > "${FILES_PKG}"/etc/minit/net.dropbear/run

  install_license "${SRCDIR}"/${S}/LICENSE "${FILES_PKG}"
}

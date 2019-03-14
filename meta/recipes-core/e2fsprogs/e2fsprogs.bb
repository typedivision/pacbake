PD = "Ext2/3/4 filesystem utilities"
PV = "1.44.5"

HOMEPAGE = "http://e2fsprogs.sourceforge.net"
LICENSE = "GPL"

SRC_URI = " \
  http://downloads.sourceforge.net/sourceforge/${P}/${S}.tar.gz;md5sum=8d78b11d04d26c0b2dd149529441fa80 \
"

HOST_DEPENDS = "gcc"
DEPENDS = "crosstool-ng"

step_build() {
  cd "${SRCDIR}"/${S}

  ./configure \
    --host=${TARGET_SYS} \
    --prefix=/usr \
    --disable-elf-shlibs \
    --disable-profile \
    --disable-uuidd \
    --enable-libuuid \
    --enable-libblkid \
    --disable-resizer \
    --disable-defrag \
    --disable-imager \
    --disable-e2initrd-helper \
    --disable-nls \
    --disable-debugfs \
    --enable-symlink-install \
    --enable-fsck

  make
  make DESTDIR="${SRCDIR}"/dest install
}

step_install() {
  cd "${SRCDIR}"/dest
  cp -a --parent usr/sbin/{mke2fs,mkfs.ext4} "${FILES_PKG}"

  install_license "${SRCDIR}"/${S}/NOTICE "${FILES_PKG}"
}

PD = "The linux kernel"
PV_pi = "7f9c648"
PV_fw = "55e5912"
PV_nf = "b518de4"
PV = "4.9.18"

HOMEPAGE = "http://www.kernel.org"
LICENSE = "GPL"

SRC_URI = " \
  https://api.github.com/repos/raspberrypi/linux/tarball/${PV_pi};md5sum=ca9fb41c8b855704e2f6b98ca05fd569;downloadfilename=rpi-linux-${PV}.tar.gz \
  https://api.github.com/repos/raspberrypi/firmware/tarball/${PV_fw};md5sum=da8175f48eebee68ca6aa53c07325f14;downloadfilename=rpi-firmware-${PV_fw}.tar.gz \
  https://api.github.com/repos/RPi-Distro/firmware-nonfree/tarball/${PV_nf};md5sum=48b1e0e08f88b1571c8c0078b61297fa;downloadfilename=rpi-nonfree-${PV_nf}.tar.gz \
  file://linux-rpi.config \
  file://files \
"

HOST_DEPENDS = "gcc flex bc"
DEPENDS = "crosstool-ng"

step_devshell() {
  step_build devshell
}

step_build() {
  cd "${SRCDIR}"/raspberrypi-linux-${PV_pi}

  export CROSS_COMPILE="${TARGET_SYS}-"
  export ARCH=arm64
  cat "${SRCDIR}"/linux-rpi.config > .config

  if [ "$1" = devshell ]; then
    exec bash
  fi

  make
  make INSTALL_MOD_PATH="${SRCDIR}"/dest_mod modules_install
}

step_install() {
  cd "${SRCDIR}"/raspberrypi-linux-${PV_pi}

  install -D arch/arm64/boot/Image "${FILES_DEPLOY}"/linux/kernel8.img
  cp arch/arm64/boot/dts/broadcom/bcm2710-rpi-3-b.dtb "${FILES_DEPLOY}"/linux
  cp COPYING "${FILES_DEPLOY}"/linux/COPYING.linux

  install -D arch/arm64/boot/Image "${FILES_SHARE}"/kernel8.img
  cp arch/arm64/boot/dts/broadcom/bcm2710-rpi-3-b.dtb "${FILES_SHARE}"
  cp COPYING "${FILES_SHARE}"/COPYING.linux

  mkdir -p "${FILES_SHARE}"/overlays
  cp arch/arm64/boot/dts/overlays/*.dtb* "${FILES_SHARE}"/overlays
  cp arch/arm64/boot/dts/overlays/README "${FILES_SHARE}"/overlays

  mkdir -p "${FILES_SHARE}"/boot
  for file in bootcode.bin start.elf fixup.dat LICENCE.broadcom; do
    cp "${SRCDIR}"/raspberrypi-firmware-${PV_fw}/boot/$file "${FILES_SHARE}"/boot
  done
  cp "${SRCDIR}"/files/{config,cmdline}.txt "${FILES_SHARE}"/boot

  install_package
}

install_package() {
  install -d "${FILES_PKG}"
  mkdir -p "${PKGDIR}"/lib/firmware/brcm

  cp -r "${SRCDIR}"/dest_mod/lib/. "${PKGDIR}"/lib
  rm -rf "${PKGDIR}"/lib/modules/${PV}/{build,source}

  cd "${SRCDIR}"/RPi-Distro-firmware-nonfree-${PV_nf}
  cp brcm/brcmfmac43430-sdio.{bin,txt} "${PKGDIR}"/lib/firmware/brcm

  install_license LICENCE.broadcom_bcm43xx "${PKGDIR}"

  cd "${SRCDIR}"/raspberrypi-linux-${PV_pi}
  install_license COPYING "${PKGDIR}"
}

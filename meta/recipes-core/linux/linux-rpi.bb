PD = "The linux kernel"
PV_rpi = "7f9c648"
PV_fw = "55e5912"
PV = "4.9_${PV_rpi}"

HOMEPAGE = "http://www.kernel.org"
LICENSE = "GPL"

SRC_URI = " \
  https://api.github.com/repos/raspberrypi/linux/tarball/${PV_rpi};md5sum=ca9fb41c8b855704e2f6b98ca05fd569;downloadfilename=rpi-linux-${PV}.tar.gz \
  https://api.github.com/repos/raspberrypi/firmware/tarball/${PV_fw};md5sum=da8175f48eebee68ca6aa53c07325f14;downloadfilename=rpi-firmware-${PV_fw}.tar.gz \
  file://${P}_files \
  file://${P}.config \
"

HOST_DEPENDS = "make gcc flex bc"
DEPENDS = "crosstool-ng"

step_devshell() {
  step_build devshell
}

step_build() {
  cd "${SRCBASE}"/raspberrypi-linux-${PV_rpi}

  export CROSS_COMPILE="${TARGET_SYS}-"
  export ARCH=arm64
  cat "${SRCBASE}"/${P}.config > .config

  if [ "$1" = devshell ]; then
    exec bash
  fi

  make
  make INSTALL_MOD_PATH="${SRCBASE}"/setup_mod modules_install
}

step_install() {
  cd "${SRCBASE}"/raspberrypi-linux-${PV_rpi}

  install -D arch/arm64/boot/Image "${FILES_DEPLOY}"/linux/kernel8.img
  cp arch/arm64/boot/dts/broadcom/bcm2710-rpi-3-b.dtb "${FILES_DEPLOY}"/linux
  cp COPYING "${FILES_DEPLOY}"/linux/COPYING.linux

  install -D arch/arm64/boot/Image "${FILES_SHARE}"/linux/kernel8.img
  cp arch/arm64/boot/dts/broadcom/bcm2710-rpi-3-b.dtb "${FILES_SHARE}"/linux
  cp COPYING "${FILES_SHARE}"/linux/COPYING.linux

  mkdir -p "${FILES_SHARE}"/linux/overlays
  cp arch/arm64/boot/dts/overlays/*.dtb* "${FILES_SHARE}"/linux/overlays
  cp arch/arm64/boot/dts/overlays/README "${FILES_SHARE}"/linux/overlays

  mkdir -p "${FILES_SHARE}"/boot
  for file in bootcode.bin start.elf fixup.dat LICENCE.broadcom; do
    cp "${SRCBASE}"/raspberrypi-firmware-${PV_fw}/boot/$file "${FILES_SHARE}"/boot
  done
  cp "${SRCBASE}"/${P}_files/{config,cmdline}.txt "${FILES_SHARE}"/boot
}

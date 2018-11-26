PD = "Basic image with core packages"
PV = "1.0"

HOST_DEPENDS = "arch-install-scripts squashfs-tools dosfstools mtools"

DEPENDS = "linux-rpi sysroot sysdebug busybox pacman"

BUILD_AS_ROOT = "1"

step_build() {
  cd "${SRCBASE}"

  {
    echo '[options]'
    echo 'Architecture=${TARGET_ARCH}'
    echo '[target]'
    echo 'Server = file://${REPO}'
  } > pacman.conf

  mkdir rootfs
  pacstrap -GMC pacman.conf rootfs sysroot sysdebug busybox pacman

  rm -rf rootfs/var/cache
  {
    echo '#!/bin/sh'
    echo 'mount -t proc proc /proc -o nosuid,noexec,nodev'
    echo 'mount -t sysfs sys /sys -o nosuid,noexec,nodev'
    echo 'mount -t tmpfs tmp /tmp -o nosuid,nodev,size=10M'
    echo 'export PATH=/sbin:/bin:/usr/sbin:/usr/bin:/opt/bin'
    echo 'exec /bin/sh'
  } > rootfs/sbin/init
  chmod 755 rootfs/sbin/init

  mksquashfs rootfs rootfs.sqfs -comp xz
  mkdir -p "${FILES_DEPLOY}"/image
  cp rootfs.sqfs "${FILES_DEPLOY}"/image/rootfs-basic.sqfs

  local img=basic.img
  local img_size=32
  dd if=/dev/zero of=$img bs=1M count=$(expr $img_size + 2)
  {
    echo -e "n\np\n1\n\n+"$img_size"M\nt\nb"
    echo -e "p\nw"
  } | fdisk -c -u $img

  local block_size=$(fdisk -l $img | grep "$img"1 | tr -s ' ' | cut -d' ' -f4)
  mkfs.vfat -n BOOT -S 512 -C fat.img $(expr $block_size / 2)

  mcopy -i fat.img -s rootfs.sqfs ::rootfs.sqfs
  for file in "${SHARE}"/linux/{kernel8.img,bcm2710-rpi-3-b.dtb,COPYING.linux} "${SHARE}"/boot/*; do
    mcopy -i fat.img -s $file ::$(basename $file)
  done

  dd if=fat.img of=$img conv=notrunc seek=1 bs=1M
  cp $img "${FILES_DEPLOY}"/image/$img

  mkdir -p "${STAGE}"/${PN}
  tar -czf "${FILES_SHARE}"/basic.sdk.tar.gz -C "${SYSBASE}" ${SDK_PREFIX}
}

step_deploy() {
  mkdir -p "${SRCBASE}"/sdk-docker
  cd "${SRCBASE}"/sdk-docker

  cp "${SHARE}"/basic.sdk.tar.gz sdk.tar.gz
  {
    echo "FROM typedivision/arch-micro"
    echo "ADD sdk.tar.gz /"
  } > Dockerfile

  pacman -S --needed --noconfirm --cachedir="${PCACHE}" docker
  docker rmi sdk-basic-${TARGET_VENDOR} || true
  docker build -t sdk-basic-${TARGET_VENDOR} .
}

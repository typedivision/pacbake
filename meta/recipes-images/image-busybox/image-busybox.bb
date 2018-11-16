PD = "Minimal image with busybox"
PV = "1.0"

HOST_DEPENDS = "arch-install-scripts squashfs-tools dosfstools mtools"

DEPENDS = "linux-rpi"
RDEPENDS = "busybox"

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
  pacstrap -GMC pacman.conf rootfs busybox

  rm -rf rootfs/var/cache rootfs/var/lib/pacman
  {
    echo '#!/bin/sh'
    echo 'mount -t proc proc /proc -o nosuid,noexec,nodev'
    echo 'mount -t sysfs sys /sys -o nosuid,noexec,nodev'
    echo 'mount -t tmpfs tmp /tmp -o nosuid,nodev,size=10M'
    echo 'export PATH=/sbin:/bin:/usr/sbin:/usr/bin'
    echo 'exec /bin/sh'
  } > rootfs/sbin/init
  chmod 755 rootfs/sbin/init
  
  mksquashfs rootfs rootfs.sqfs -comp xz
  mkdir -p "${FILES_DEPLOY}"/image
  cp rootfs.sqfs "${FILES_DEPLOY}"/image/rootfs-busybox.sqfs

  local img=busybox.img
  local img_size=16
  dd if=/dev/zero of=$img bs=1M count=$(expr $img_size + 2)
  {
    echo -e "n\np\n1\n\n+"$img_size"M\nt\nb"
    echo -e "p\nw"
  } | fdisk -c -u $img

  local block_size=$(fdisk -l $img | grep "$img"1 | tr -s ' ' | cut -d' ' -f4)
  mkfs.vfat -n BOOT -S 512 -C fat.img $(expr $block_size / 2)

  mcopy -i fat.img -s rootfs.sqfs ::rootfs.sqfs
  for file in "${SHARED}"/linux/{kernel8.img,bcm2710-rpi-3-b.dtb,COPYING.linux} "${SHARED}"/boot/*; do
    mcopy -i fat.img -s $file ::$(basename $file)
  done

  dd if=fat.img of=$img conv=notrunc seek=1 bs=1M
  cp $img "${FILES_DEPLOY}"/image/$img
}

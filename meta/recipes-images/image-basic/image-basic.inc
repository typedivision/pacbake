PV = "0"

HOST_DEPENDS = "arch-install-scripts squashfs-tools dosfstools mtools"

IMAGE_PKGS = "sysbase sysdebug sysfiles linux-rpi pacman dropbear"
DEPENDS = "${IMAGE_PKGS}"

POST_IMAGE_INSTALL_CMD ?= ""

step_install() {
  mkdir "${RESULT}"/image
  cd "${RESULT}"/image

  {
    echo '[options]'
    echo 'Architecture=${TARGET_ARCH}'
    echo '[target]'
    echo 'Server = file://${REPO}'
  } > pacman.conf

  mkdir rootfs
  pacstrap -GMC pacman.conf rootfs ${IMAGE_PKGS}

  rm -rf rootfs/var/cache rootfs/run
  ln -s /var/run rootfs/run

  ${POST_IMAGE_INSTALL_CMD}

  mksquashfs rootfs rootfs.sqfs -comp xz
  mkdir -p "${FILES_DEPLOY}"/image
  cp rootfs.sqfs "${FILES_DEPLOY}"/image/rootfs-${P}.sqfs

  local img=${P}.img
  local img_size=32
  dd if=/dev/zero of=$img bs=1M count=$(expr $img_size + 1)
  {
    echo -e "n\np\n1\n\n\nt\nb"
    echo -e "p\nw"
  } | fdisk -c -u $img

  local block_size=$(fdisk -l $img | grep "$img"1 | tr -s ' ' | cut -d' ' -f4)
  mkfs.vfat -n BOOT -S 512 -C fat.img $(expr $block_size / 2)

  mcopy -i fat.img -s rootfs.sqfs ::rootfs.sqfs
  for file in "${SDK_SHARED}"/linux-rpi/{kernel8.img,bcm2710-rpi-3-b.dtb,COPYING.linux} \
              "${SDK_SHARED}"/linux-rpi/boot/*; do
    mcopy -i fat.img -s $file ::$(basename $file)
  done

  dd if=fat.img of=$img conv=notrunc seek=1 bs=1M
  cp $img "${FILES_DEPLOY}"/image/$img
}

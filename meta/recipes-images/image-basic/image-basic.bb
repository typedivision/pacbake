PD = "Basic image with core packages"
PV = "0"

HOST_DEPENDS = "arch-install-scripts squashfs-tools dosfstools mtools"

IMAGE_PKGS = "sysroot sysdebug sysfiles linux-rpi pacman"
DEPENDS = "${IMAGE_PKGS}"

step_install() {
  cd "${SRCDIR}"

  {
    echo '[options]'
    echo 'Architecture=${TARGET_ARCH}'
    echo '[target]'
    echo 'Server = file://${REPO}'
  } > pacman.conf

  mkdir rootfs
  pacstrap -GMC pacman.conf rootfs ${IMAGE_PKGS}

  rm -rf rootfs/var/cache
  ln -sf /var/run rootfs/run

  mksquashfs rootfs rootfs.sqfs -comp xz
  mkdir -p "${FILES_DEPLOY}"/image
  cp rootfs.sqfs "${FILES_DEPLOY}"/image/rootfs-basic.sqfs

  local img=basic.img
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

  tar -czf "${FILES_DEPLOY}"/basic.sdk.tar.gz -C "${DEVROOT}" ${SDK_PREFIX}
}

step_deploy() {
  if ! [ "${DOCKER_SDK}" ]; then
    return
  fi

  mkdir -p "${SRCDIR}"/sdk-docker
  cd "${SRCDIR}"/sdk-docker

  cp "${DEPLOY}"/basic.sdk.tar.gz sdk.tar.gz
  {
    echo "FROM typedivision/arch-micro"
    echo "ADD sdk.tar.gz /"
  } > Dockerfile

  local sdkimage=pactools-${TARGET_ALIAS}:basic
  pacman -S --needed --noconfirm --cachedir="${PKGCACHE}" docker
  docker rmi $sdkimage || true
  docker build -t $sdkimage .
}

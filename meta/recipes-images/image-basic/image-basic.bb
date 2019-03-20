PD = "Basic image with core packages"

require image-basic.inc

step_install_append() {
  tar -czf "${FILES_DEPLOY}"/sdk-basic.tar.gz -C "${DEVROOT}" ${SDK_PREFIX}
}

step_deploy() {
  if ! [ "${DOCKER_SDK}" ]; then
    return
  fi

  mkdir -p "${TARGET}"/sdk-basic-docker
  cd "${TARGET}"/sdk-basic-docker

  cp "${DEPLOY}"/sdk-basic.tar.gz sdk.tar.gz
  {
    echo "FROM typedivision/arch-micro"
    echo "ADD sdk.tar.gz /"
  } > Dockerfile

  local sdkimage=sdk-${TARGET_ALIAS}:basic
  pacman -S --needed --noconfirm --cachedir="${PKGCACHE}" docker
  docker rmi $sdkimage || true
  docker build -t $sdkimage .
}

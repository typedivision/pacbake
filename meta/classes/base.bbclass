BB_DEFAULT_TASK = "all"
T = "${WORKDIR}/temp"

inherit logging sstate pacman

FILESPATH = "${FILE_DIRNAME}"
PKGCACHE ?= "${DL_DIR}/pkgcache"

HOST_DEPENDS_BASE = " \
  shadow coreutils findutils diffutils bash sed grep gawk \
  which file tar gzip patch make pkgconfig \
"

DEPENDS_append = " ${RDEPENDS}"
RDEPENDS = ""

WRAP_DEVROOT = 'bwrap \
  --bind "${DEVROOT}" / \
  --proc /proc \
  --dev /dev \
  --tmpfs /tmp \
  --ro-bind /etc/resolv.conf /etc/resolv.conf \
  --bind "${WORKDIR}" "${WORKDIR}" \
  --bind "${DL_DIR}" "${DL_DIR}" \
  --bind "${REPO}" "${REPO}" \
  --setenv DEVROOT 1 \
'

WRAP_DEVROOT_USER = '${WRAP_DEVROOT} \
  --unshare-user --uid 1000 \
'

addtask clean
do_clean[nostamp] = "1"

python do_clean() {
    bb.note("Cleaning workdir, stamps and stage files for package " + d.getVar("PN"))
    workdir = d.expand("${W}")
    stamps = d.expand("${STAMP}.*")
    stage = d.expand("${STAGE}/${PN}")
    for path in [workdir, stamps, stage]:
        bb.utils.remove(path, True)
}

addtask fetch before do_build
do_fetch[file-checksums] = "${@bb.fetch2.get_checksum_file_list(d)}"
do_fetch[dirs] = "${DL_DIR}"

python do_fetch() {
    src_uri = (d.getVar("SRC_URI") or "").split()
    if len(src_uri) == 0:
        return
    try:
        fetcher = bb.fetch2.Fetch(src_uri, d)
        fetcher.download()
        if d.getVar("UNPACK"):
            fetcher.unpack(d.getVar("SRCDIR"))
    except bb.fetch2.BBFetchException as e:
        bb.fatal(str(e))
}

addtask setup before do_build
do_setup[deptask] = "do_stage"
do_setup[cleandirs] = "${DEVROOT}"
do_setup[dirs] = "${PKGCACHE} ${COMMON}"
do_setup[vardepsexclude] += "BB_TASKDEPDATA"
do_setup[vardeps] += "DEPENDS"

python do_setup() {
    taskdepdata = d.getVar("BB_TASKDEPDATA", False)
    depends = []
    for depid in taskdepdata:
        depname = taskdepdata[depid][0]
        if depname != d.getVar("PN") and depname not in depends:
            depends.append(depname)

    localdata = bb.data.createCopy(d)
    localdata.setVar("BUILD_DEPENDS", " ".join(depends))
    bb.build.exec_func("setup_devroot", localdata)
}

setup_devroot() {
  local devroot_base="${COMMON}"/devroot_base
  if ! [ -d "$devroot_base" ]; then
    (
      flock 100
      [ -d "$devroot_base" ] && exit
      rm -rf "$devroot_base.tmp"
      mkdir -p "$devroot_base.tmp"
      pacstrap "$devroot_base.tmp" ${HOST_DEPENDS_BASE} --cachedir="${PKGCACHE}"
      echo "en_US.UTF-8 UTF-8" > "$devroot_base.tmp"/etc/locale.gen
      bwrap --bind "$devroot_base.tmp" / sh -c "locale-gen; useradd -u 1000 user"
      mv "$devroot_base.tmp" "$devroot_base"
    ) 100>"$devroot_base.lock"
  fi
  cp -a "$devroot_base"/. "${DEVROOT}"
  if [ "${HOST_DEPENDS}" ]; then
    (
      flock 100
      pacman -r "${DEVROOT}" --cachedir="${PKGCACHE}" -S --noconfirm --needed ${HOST_DEPENDS}
    ) 100>"${PKGCACHE}/pkgcache.lock"
  fi
  for dep in ${BUILD_DEPENDS}; do
    if [ -e "${STAGE}"/$dep/$dep.devel.tar.gz ]; then
      bbmsg INFO "setup $dep"
      tar -h -xf "${STAGE}"/$dep/$dep.devel.tar.gz -C "${DEVROOT}"
    fi
    if [ -e "${STAGE}"/$dep/$dep.share.tar.gz ]; then
      bbmsg INFO "setup shared files of $dep"
      mkdir -p "${DEVROOT}"/${SDK_SHARED}/$dep
      tar -h -xf "${STAGE}"/$dep/$dep.share.tar.gz -C "${DEVROOT}"/${SDK_SHARED}/$dep
    fi
  done
}

unpack[cleandirs] = "${SRCDIR}"

python unpack() {
    localdata = bb.data.createCopy(d)
    localdata.setVar("UNPACK", True)
    bb.build.exec_func("do_fetch", localdata)
}

addtask build after do_setup
do_build[deptask] = "do_deploy"
do_build[prefuncs] = "unpack"
do_build[cleandirs] = "${RESULT}"
do_build[dirs] = "${REPO} ${WORKDIR}"

do_build() {
  if ! [ "$DEVROOT" ]; then
    exec ${WRAP_DEVROOT_USER} "$0"
  fi
  bbmsg NOTE "Start build at $(date +'%T %Z')"
  cd "${SRCDIR}"
  step_prepare
  cd "${SRCDIR}"
  step_build
}

base_step_prepare() {
  :
}

base_step_build() {
  :
}

addtask install after do_build
do_install[cleandirs] = "${RESULT} ${TARGET}"
do_install[dirs] = "${WORKDIR}"

do_install() {
  if ! [ "$DEVROOT" ]; then
    exec ${WRAP_DEVROOT} "$0"
  fi
  mkdir -p "${FILES_DEVEL}" "${FILES_SHARE}" "${FILES_DEPLOY}"
  if [ $(echo ${PACKAGES} | wc -w) -eq 1 ]; then
      install -d "${FILES_PKG}"
  else
    for p in ${PACKAGES}; do
      install -d "${FILES_PKG}_$p"
    done
  fi
  cd "${SRCDIR}"
  step_install
}

base_step_install() {
  :
}

addtask devshell after do_setup
do_devshell[prefuncs] = "unpack"
do_devshell[nostamp] = "1"
do_devshell[dirs] = "${WORKDIR}"

do_devshell() {
  if ! [ "$DEVROOT" ]; then
    if ! tmux info &> /dev/null; then
      bbmsg WARNING "tmux is not running"
      return 1
    fi
    tmux split-window "
      ${WRAP_DEVROOT_USER} \"$0\" || sleep 20
      tmux wait-for -S done
    " \; wait-for done
  else
    step_prepare
    step_devshell
  fi
}

base_step_devshell() {
  cd "${SRCDIR}"
  exec bash
}

addtask stage after do_install
do_stage[cleandirs] = "${STAGE}/${PN}"
do_stage[postfuncs] = "teardown"

do_stage() {
  if [ "$(ls ${FILES_DEVEL})" ]; then
    tar -czf "${STAGE}/${PN}/${PN}".devel.tar.gz -C "${FILES_DEVEL}" .
  fi
  if [ "$(ls ${FILES_SHARE})" ]; then
    tar -czf "${STAGE}/${PN}/${PN}".share.tar.gz -C "${FILES_SHARE}" .
  fi
  if [ "$(ls ${FILES_DEPLOY})" ]; then
    tar -czf "${STAGE}/${PN}/${PN}".deploy.tar.gz -C "${FILES_DEPLOY}" .
  fi
  if [ -d "${PKGDIR}" ]; then
    find "${PKGDIR}" -name "*.pkg.tar.*" -exec cp {} "${STAGE}/${PN}" \;
  fi
}

python teardown() {
    bb.build.del_stamp("do_setup", d)
    devroot = d.expand("${DEVROOT}")
    bb.utils.remove(devroot, True)
}

addtask deploy after do_stage
do_deploy[deptask] = "do_deploy"
do_deploy[dirs] = "${DEPLOY}"

do_deploy() {
  if [ -e "${STAGE}"/${PN}/${PN}.deploy.tar.gz ]; then
    tar -xhf "${STAGE}"/${PN}/${PN}.deploy.tar.gz -C "${DEPLOY}"
  fi
  step_deploy
}

base_step_deploy() {
  :
}

addtask all after do_deploy
do_all[noexec] = "1"

do_all() {
  :
}

EXPORT_FUNCTIONS step_prepare step_build step_install step_devshell step_deploy

BB_DEFAULT_TASK ?= "all"

inherit logging sstate

FILESPATH = "${FILE_DIRNAME}"
PKGCACHE ?= "${DL_DIR}/pkgcache"

HOST_DEPENDS_BASE = "shadow coreutils findutils diffutils bash tar gzip sed grep gawk which file patch"

DEPENDS_append = " ${RDEPENDS}"
RDEPENDS = ""

WRAP_SYSBASE = 'bwrap \
  --bind "${SYSBASE}" / \
  --proc /proc \
  --dev /dev \
  --tmpfs /tmp \
  --ro-bind /etc/resolv.conf /etc/resolv.conf \
  --bind "${WORKDIR}" "${WORKDIR}" \
  --bind "${DL_DIR}" "${DL_DIR}" \
  --bind "${REPO}" "${REPO}" \
  --bind "${SHARE}" "${SHARE}" \
  --setenv WRAP 1 \
'

WRAP_SYSBASE_USER = '${WRAP_SYSBASE} \
  --unshare-user --uid 1000 \
'

addtask clean
do_clean[nostamp] = "1"

python do_clean() {
    bb.note("Cleaning workdir, stamps and stage files for package " + d.getVar("P"))
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
            fetcher.unpack(d.getVar("SRCBASE"))
    except bb.fetch2.BBFetchException as e:
        bb.fatal(str(e))
}

addtask setup before do_build
do_setup[deptask] = "do_stage"
do_setup[cleandirs] = "${SYSBASE}"
do_setup[dirs] = "${LOCAL}"
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
    bb.build.exec_func("setup_system", localdata)
}

setup_system() {
  local sysbase_setup="${LOCAL}"/sysbase_setup
  if ! [ -d "$sysbase_setup" ]; then
    (
      flock 200
      [ -d "$sysbase_setup" ] && exit
      rm -rf "$sysbase_setup.tmp"
      mkdir -p "$sysbase_setup.tmp"
      pacstrap "$sysbase_setup.tmp" ${HOST_DEPENDS_BASE} --cachedir="${PKGCACHE}"
      echo "en_US.UTF-8 UTF-8" > "$sysbase_setup.tmp"/etc/locale.gen
      bwrap --bind "$sysbase_setup.tmp" / sh -c "locale-gen; useradd -u 1000 user"
      mv "$sysbase_setup.tmp" "$sysbase_setup"
    ) 200>"$sysbase_setup.lock"
  fi
  cp -a "$sysbase_setup"/. "${SYSBASE}"
  pacman -r "${SYSBASE}" --cachedir="${PKGCACHE}" -S --noconfirm --needed ${HOST_DEPENDS}
  for dep in ${BUILD_DEPENDS}; do
    if [ -e "${STAGE}"/$dep/$dep.setup.tar.gz ]; then
      bbmsg INFO "install $dep"
      tar -xf "${STAGE}"/$dep/$dep.setup.tar.gz -C "${SYSBASE}"
    fi
  done
}

unpack[cleandirs] = "${SRCBASE}"

python unpack() {
    localdata = bb.data.createCopy(d)
    localdata.setVar("UNPACK", True)
    bb.build.exec_func("do_fetch", localdata)
}

addtask build after do_setup
do_build[deptask] = "do_deploy"
do_build[prefuncs] = "unpack"
do_build[cleandirs] = "${FILES_SETUP} ${FILES_SHARE} ${FILES_DEPLOY}"
do_build[dirs] = "${REPO} ${SHARE}"

do_build() {
  if ! [ "$WRAP" ]; then
    bbmsg NOTE "Executing Build Task"
    if [ "${BUILD_AS_ROOT}" ]; then
      exec ${WRAP_SYSBASE} "$0"
    else
      exec ${WRAP_SYSBASE_USER} "$0"
    fi
  else
    step_prepare
    step_build
    step_install
  fi
}

base_step_prepare() {
  :
}

base_step_build() {
  :
}

base_step_install() {
  :
}

addtask devshell after do_setup
do_devshell[prefuncs] = "unpack"
do_devshell[nostamp] = "1"

do_devshell() {
  if ! [ "$WRAP" ]; then
    if ! tmux info &> /dev/null; then
      bbmsg WARNING "tmux is not running"
      return 1
    fi
    tmux split-window "
      ${WRAP_SYSBASE_USER} \"$0\"
      tmux wait-for -S done
    " \; wait-for done
  else
    step_prepare
    step_devshell
  fi
}

base_step_devshell() {
  cd "${SRCBASE}"
  exec bash
}

addtask stage after do_build
do_stage[cleandirs] = "${STAGE}/${PN}"
do_stage[postfuncs] = "teardown"

do_stage() {
  if [ "$(ls ${FILES_SETUP})" ]; then
    tar -czf "${STAGE}/${PN}/${PN}".setup.tar.gz -C "${FILES_SETUP}" .
  fi
  if [ "$(ls ${FILES_SHARE})" ]; then
    tar -czf "${STAGE}/${PN}/${PN}".share.tar.gz -C "${FILES_SHARE}" .
  fi
  if [ "$(ls ${FILES_DEPLOY})" ]; then
    tar -czf "${STAGE}/${PN}/${PN}".deploy.tar.gz -C "${FILES_DEPLOY}" .
  fi
  if [ -d "${PKGBASE}" ]; then
    find "${PKGBASE}" -name "*.pkg.tar.*" -exec cp {} "${STAGE}/${PN}" \;
  fi
}

python teardown() {
    bb.build.del_stamp("do_setup", d)
    sysbase = d.expand("${SYSBASE}")
    bb.utils.remove(sysbase, True)
}

addtask deploy after do_stage
do_deploy[deptask] = "do_deploy"
do_deploy[dirs] = "${SHARE} ${DEPLOY}"

do_deploy() {
  if [ -e "${STAGE}"/${PN}/${PN}.deploy.tar.gz ]; then
    tar -xhf "${STAGE}"/${PN}/${PN}.deploy.tar.gz -C "${DEPLOY}"
  fi
  if [ -e "${STAGE}"/${PN}/${PN}.share.tar.gz ]; then
    tar -xhf "${STAGE}"/${PN}/${PN}.share.tar.gz -C "${SHARE}"
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

PKGDIR = "${WORKDIR}/pkgdir"

HOST_DEPENDS_BASE_append = " pacman fakeroot"

PACKAGES = "${PN}"

install_license() {
  install -Dm644 "$1" -t "$2"/usr/share/licenses/${PN}
}

addtask package after do_install before do_stage
do_package[cleandirs] = "${PKGDIR}"

python() {
    pkgname = (d.getVar("PACKAGES") or "").split()
    if len(pkgname) == 0 or d.getVar("PN").endswith("-native"):
        d.setVar("PACKAGES", "")
        d.setVarFlag("do_package", "noexec", "1")
    elif len(pkgname) > 1:
        for p in pkgname:
            d.appendVar("PROVIDES", " " + p)
}

python do_package() {
    pkgname = d.getVar("PACKAGES").split()

    pkgdir = d.getVar("PKGDIR")
    pkgbuild = open(os.path.join(pkgdir, 'PKGBUILD'), 'w')

    pkgbuild.write('pkgname=(%s)\n' % " ".join(pkgname))
    pkgbuild.write('pkgver=%s\n' % d.getVar("PV"))
    pkgbuild.write('pkgrel=%s\n' % d.getVar("PR"))
    pkgbuild.write('pkgdesc="%s"\n' % d.getVar("PD"))
    pkgbuild.write('arch=(%s)\n' % d.getVar("TARGET_ARCH"))
    pkgbuild.write('url=%s\n' % d.getVar("HOMEPAGE"))
    pkgbuild.write('license=(%s)\n' % d.getVar("LICENSE"))

    if d.getVar("RDEPENDS") is not None:
        pkgbuild.write('depends=(%s)\n' % d.getVar("RDEPENDS"))

    if len(pkgname) == 1:
        pkgbuild.write('package() { cp -r "%s"/. "$pkgdir"; }\n' % d.getVar("FILES_PKG"))
    else:
        for p in pkgname:
            pkgbuild.write(
                'package_%s() { cp -r "%s_%s"/. "$pkgdir"; }\n' % (p, d.getVar("FILES_PKG"), p)
            )

    pkgbuild.close()
    bb.build.exec_func("package_pkgbuild", d)
}

package_pkgbuild() {
  cd "${PKGDIR}"
  {
    echo 'CARCH=${TARGET_ARCH}'
    echo 'CHOST=${TARGET_SYS}'
    echo 'OPTIONS=(strip !libtool !staticlibs emptydirs purge)'
    echo 'PURGE_TARGETS=(usr/{,share}/{doc,man,info})'
    echo 'PKGEXT=".pkg.tar.xz"'
    echo 'strip() { ${TARGET_SYS}-strip "$@"; }'
    echo 'objcopy() { ${TARGET_SYS}-objcopy "$@"; }'
  } > makepkg.conf

  ${WRAP_DEVROOT_USER} makepkg -Rdfc --config makepkg.conf
}

addtask repo_add after do_stage before do_deploy
do_repo_add[dirs] = "${REPO}"

do_repo_add() {
  for pkg in "${STAGE}/${PN}"/*.pkg.tar.xz; do
    if [ -e "$pkg" ]; then
      local pkgname=$(basename "$pkg")
      cp "$pkg" "${REPO}"
      (
        flock 200
        repo-remove "${REPO}"/target.db.tar.gz ${pkgname%-*-*-*} 2>/dev/null || true
        repo-add "${REPO}"/target.db.tar.gz "${REPO}"/$pkgname
      ) 200>"${REPO}"/.lock
    fi
  done
}

PKGBASE = "${WORKDIR}/pkgbase"
PKGDIR  = "$pkgdir"
LICDIR  = "${PKGDIR}/usr/share/licenses/${P}"

HOST_DEPENDS += "pacman fakeroot"

addtask package after do_build before do_stage
do_package[cleandirs] = "${PKGBASE}"

python() {
    pkgname = (d.getVar("PACKAGES") or "").split()
    if len(pkgname) > 1:
        for p in pkgname:
            d.appendVarFlag("do_package", "vardeps", " step_package_" + p)
            d.appendVar("PROVIDES", " " + p)
}

python do_package() {
    packagedir = d.getVar("PKGBASE")
    pkgbuild = open(os.path.join(packagedir, 'PKGBUILD'), 'w')

    pkgname = (d.getVar("PACKAGES") or "").split()
    if len(pkgname) == 0:
        pkgname = [d.getVar("PN")];

    pkgbuild.write("pkgname=(%s)\n" % " ".join(pkgname))
    pkgbuild.write("pkgver=%s\n" % d.getVar("PV"))
    pkgbuild.write("pkgrel=%s\n" % d.getVar("PR"))
    pkgbuild.write("pkgdesc=\"%s\"\n" % d.getVar("PD"))
    pkgbuild.write("arch=(%s)\n" % d.getVar("TARGET_ARCH"))
    pkgbuild.write("url=%s\n" % d.getVar("HOMEPAGE"))
    pkgbuild.write("license=(%s)\n" % d.getVar("LICENSE"))
    if d.getVar("RDEPENDS") is not None:
        pkgbuild.write("depends=(%s)\n" % d.getVar("RDEPENDS"))
    if len(pkgname) <= 1:
        pkgbuild.write("package() {\n%s}\n" % d.getVar("step_package"))
    else:
        for p in pkgname:
            pkgbuild.write("package_%s() {\n%s}\n" % (p, d.getVar("step_package_" + p)))

    pkgbuild.close()
    bb.build.exec_func("pkgbuild", d)
}

pkgbuild() {
  cd "${PKGBASE}"

  printf > makepkg.conf '%s\n' \
    'CARCH=${TARGET_ARCH}' \
    'CHOST=${TARGET_SYS}' \
    'OPTIONS=(strip !libtool !staticlibs emptydirs purge)' \
    'PURGE_TARGETS=(usr/{,share}/{doc,man,info})' \
    'PKGEXT=".pkg.tar.xz"' \
    'strip() { ${TARGET_SYS}-strip "$@"; }' \
    'objcopy() { ${TARGET_SYS}-objcopy "$@"; }'

  ${WRAP_SYSBASE_USER} makepkg -Rdfc --config makepkg.conf
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

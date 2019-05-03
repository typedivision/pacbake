EXCLUDE_FROM_WORLD = "1"

deltask do_package
deltask do_repo_add

python() {
    pn = d.getVar("PN")

    host_depends = d.getVar("HOST_DEPENDS_native")
    if host_depends is not None:
        d.setVar("HOST_DEPENDS", host_depends)

    depends = d.getVar("DEPENDS_native")
    if depends is None:
        depends = d.getVar("DEPENDS")
    if not depends:
        return
    nativedeps = []
    for dep in bb.utils.explode_deps(depends):
        if dep == pn:
            continue
        elif not dep.endswith("-native"):
            nativedeps.append(dep + "-native")
        else:
            nativedeps.append(dep)
    d.setVar("DEPENDS", " ".join(nativedeps))
}

TARGET_ARCH = "${BUILD_ARCH}"
TARGET_OS = "${BUILD_OS}"
TARGET_VENDOR = "${BUILD_VENDOR}"

CC = "${CC_BUILD}"
CXX = "${CXX_BUILD}"
AR = "${AR_BUILD}"
RANLIB = "${RANLIB_BUILD}"

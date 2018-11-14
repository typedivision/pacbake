python() {
    pn = d.getVar("PN")
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
CXX = "$CXX_BUILD}"
AR = "${AR_BUILD}"
RANLIB = "${RANLIB_BUILD}"

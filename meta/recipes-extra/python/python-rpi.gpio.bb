PD = "Python module to control Raspberry Pi GPIO channels"
PV = "${PV_REV}"

HOMEPAGE = "http://sourceforge.net/projects/raspberry-gpio-python/"
LICENSE = "MIT"

SRC_URI = " \
  hg://hg.code.sf.net/p/raspberry-gpio-python;module=code \
"

SRCREV = "03be41"

HOST_DEPENDS = "python3"
DEPENDS = "crosstool-ng"
RDEPENDS = "python"

step_install() {
  cd "${SRCDIR}"/code

  export PYTHONPATH="${SDK_SYSROOT}"/usr/lib/python3.7
  export _PYTHON_SYSCONFIGDATA_NAME=_sysconfigdata_m_linux_aarch64-linux-gnu
  export PYTHONDONTWRITEBYTECODE=1

  python setup.py install --root="${FILES_PKG}"

  install_license LICENCE.txt "${FILES_PKG}"
}

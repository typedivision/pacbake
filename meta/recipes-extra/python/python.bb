PD = "Next generation of the python high-level scripting language"
PV = "3.7.2"

HOMEPAGE = "http://www.python.org/"
LICENSE = "custom"

SRC_URI = " \
  https://www.python.org/ftp/${P}/${PV}/Python-${PV}.tar.xz;md5sum=df6ec36011808205beda239c72f947cb \
"

HOST_DEPENDS = "python3"
DEPENDS = "crosstool-ng"

step_build() {
  cd "${SRCDIR}"/Python-${PV}

  {
    echo "*disabled*"
    echo "_asyncio _bz2 _codecs_cn _codecs_hk _codecs_iso2022 _codecs_jp _codecs_kr"
    echo "_codecs_tw _contextvars _crypt _csv _ctypes _ctypes_test _curses _curses_panel"
    echo "_dbm _decimal _gdbm _json _lsprof _lzma _multibytecodec _multiprocessing _queue"
    echo "_sha256 _sqlite3 _ssl _testbuffer _testcapi _testimportmultiple _testmultiphase"
    echo "_tkinter _uuid _xxtestfuzz audioop mmap nis ossaudiodev parser pwd readline"
    echo "resource termios xxlimited"

    echo "_abc _bisect _blake2 _datetime _elementtree _hashlib _heapq _md5 _opcode _pickle"
    echo "_posixsubprocess _random _sha1 _sha3 _sha512 _struct _socket array atexit binascii"
    echo "cmath fcntl grp math pyexpat pwd select spwd syslog time unicodedata zlib"
  } > Modules/Setup.local

  ./configure \
    --build=${BUILD_SYS} \
    --host=${TARGET_SYS} \
    --prefix=/usr \
    --enable-shared \
    --with-computed-gotos \
    --enable-optimizations \
    --with-lto \
    --without-ensurepip \
    --without-cxx-main \
    --disable-ipv6 \
    ac_cv_file__dev_ptmx=no \
    ac_cv_file__dev_ptc=no

  make
}

step_install() {
  cd "${SRCDIR}"/Python-${PV}
  make DESTDIR="${FILES_DEVEL}"/${SDK_SYSROOT} install

  cd "${FILES_DEVEL}"/${SDK_SYSROOT}
  install -D usr/lib/*.so* -t "${FILES_PKG}"/usr/lib
  install -D usr/bin/python3.7 -t "${FILES_PKG}"/usr/bin
  ln -s python3.7 "${FILES_PKG}"/usr/bin/python3
  ln -s python3.7 "${FILES_PKG}"/usr/bin/python

  cd usr/lib/python3.7
  install -d "${FILES_PKG}"/usr/lib/python3.7/lib-dynload
  install -d "${FILES_PKG}"/usr/lib/python3.7/encodings

  install \
    encodings/__init__.py \
    encodings/aliases.py \
    encodings/utf_8.py \
    encodings/latin_1.py \
    "${FILES_PKG}"/usr/lib/python3.7/encodings

  install \
    codecs.py \
    io.py \
    abc.py \
    site.py \
    os.py \
    stat.py \
    posixpath.py \
    genericpath.py \
    _collections_abc.py \
    _sitebuiltins.py \
    "${FILES_PKG}"/usr/lib/python3.7

  install_license "${SRCDIR}"/Python-${PV}/LICENSE "${FILES_PKG}"
}

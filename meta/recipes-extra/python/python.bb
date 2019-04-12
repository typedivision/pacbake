PD = "Next generation of the python high-level scripting language"
PV = "3.7.2"

HOMEPAGE = "http://www.python.org/"
LICENSE = "custom"

SRC_URI = " \
  https://www.python.org/ftp/${P}/${PV}/Python-${PV}.tar.xz;md5sum=df6ec36011808205beda239c72f947cb \
"

PACKAGES = "python python-server"

HOST_DEPENDS = "python3"
DEPENDS = "crosstool-ng"

step_build() {
  cd "${SRCDIR}"/Python-${PV}
  {
    echo "*disabled*"
    echo "_abc _elementtree _hashlib _pickle"
    echo "array atexit cmath fcntl grp pwd spwd syslog time unicodedata zlib"

    echo "_asyncio _bz2 _codecs_cn _codecs_hk _codecs_iso2022 _codecs_jp _codecs_kr"
    echo "_codecs_tw _crypt _csv _ctypes _ctypes_test _curses _curses_panel"
    echo "_dbm _gdbm _json _lsprof _lzma _multibytecodec _multiprocessing _queue"
    echo "_sqlite3 _ssl _testbuffer _testcapi _testimportmultiple _testmultiphase"
    echo "_tkinter _uuid _xxtestfuzz audioop mmap nis ossaudiodev parser readline"
    echo "resource termios xxlimited"
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
  install_python
  install_python_server
}

install_python() {
  local pkgdir="${FILES_PKG}_python"
  cd "${FILES_DEVEL}"/${SDK_SYSROOT}

  install -D usr/lib/*.so* -t "$pkgdir"/usr/lib
  install -D usr/bin/python3.7 -t "$pkgdir"/usr/bin
  ln -s python3.7 "$pkgdir"/usr/bin/python3
  ln -s python3.7 "$pkgdir"/usr/bin/python

  cd usr/lib/python3.7
  install -d "$pkgdir"/usr/lib/python3.7/lib-dynload
  install -d "$pkgdir"/usr/lib/python3.7/encodings

  install \
    encodings/__init__.py \
    encodings/aliases.py \
    encodings/utf_8.py \
    encodings/latin_1.py \
    "$pkgdir"/usr/lib/python3.7/encodings

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
    "$pkgdir"/usr/lib/python3.7

  install_license "${SRCDIR}"/Python-${PV}/LICENSE "$pkgdir"
}

install_python_server() {
  local pkgdir="${FILES_PKG}_python-server"
  cd "${FILES_DEVEL}"/${SDK_SYSROOT}/usr/lib/python3.7

  install -d "$pkgdir"/usr/lib/python3.7/lib-dynload
  install \
    lib-dynload/math.cpython* \
    lib-dynload/_datetime.cpython* \
    lib-dynload/_heapq.cpython* \
    lib-dynload/_random.cpython* \
    lib-dynload/_bisect.cpython* \
    lib-dynload/_md5.cpython* \
    lib-dynload/_sha1.cpython* \
    lib-dynload/_sha3.cpython* \
    lib-dynload/_sha256.cpython* \
    lib-dynload/_sha512.cpython* \
    lib-dynload/_blake2.cpython* \
    lib-dynload/_socket.cpython* \
    lib-dynload/select.cpython* \
    lib-dynload/_struct.cpython* \
    lib-dynload/binascii.cpython* \
    lib-dynload/_contextvars.cpython* \
    lib-dynload/pyexpat.cpython* \
    lib-dynload/_opcode.cpython* \
    lib-dynload/_posixsubprocess.cpython* \
    "$pkgdir"/usr/lib/python3.7/lib-dynload
 
  install \
    copy.py types.py weakref.py _weakrefset.py copyreg.py \
    datetime.py enum.py re.py sre_*.py functools.py operator.py \
    keyword.py heapq.py reprlib.py random.py warnings.py \
    hashlib.py traceback.py linecache.py tokenize.py token.py \
    string.py threading.py bisect.py socket.py selectors.py \
    calendar.py locale.py base64.py struct.py quopri.py uu.py \
    mimetypes.py shutil.py fnmatch.py socketserver.py decimal.py \
    _pydecimal.py numbers.py contextvars.py inspect.py dis.py \
    opcode.py pydoc.py contextlib.py pkgutil.py platform.py \
    subprocess.py signal.py \
    "$pkgdir"/usr/lib/python3.7

  for libdir in http email collections logging urllib html xml xmlrpc importlib; do
    find $libdir -name "*.py" -exec cp --parent {} "$pkgdir"/usr/lib/python3.7 \;
  done

  install -Dm644 "${SRCDIR}"/Python-${PV}/LICENSE -t "$pkgdir"/usr/share/licenses/python-server
}

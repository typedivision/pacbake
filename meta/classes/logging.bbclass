LOGFIFO = "${T}/fifo.${@os.getpid()}"
ENABLE_SH_LOGGING = "1"

bbmsg() {
  {
    if ! [ "${ENABLE_SH_LOGGING}" = 1 ]; then
      return
    fi
    local log_level=$1
    shift

    local log_cmd
    case $log_level in
      ERROR)   log_cmd="bberror";;
      WARNING) log_cmd="bbwarn";;
      NOTE)    log_cmd="bbverbnote";;
      INFO)    log_cmd="bbnote";;
      DEBUG)   log_cmd="bbdebug 1";;
      *)       return;;
    esac

    if [ -p "${LOGFIFO}" ] ; then
      printf "%b\0" "$log_cmd $*" > "${LOGFIFO}"
    else
      echo "$log_level: $*"
    fi
  } 2>/dev/null
}

def generate_sstatefn(p, hash, d):
    if not hash:
        hash = "INVALID"
    return p + "_" + hash

PS = "${@generate_sstatefn(d.getVar('PF'), d.getVar('BB_TASKHASH'), d)}"

SSTATETASKS = "do_stage"

addtask stage_setscene

do_stage_setscene() {
  sstate_restore
}

python () {
    for task in (d.getVar('SSTATETASKS', True) or "").split():
        d.appendVarFlag(task, 'postfuncs', " sstate_save")
}

sstate_save[dirs] = "${SSTATE}"

sstate_save() {
  cp -a "${STAGE}/${PN}" "${SSTATE}/${PS}"
}

sstate_restore[cleandirs] = "${STAGE}/${PN}"

sstate_restore() {
  cp -a "${SSTATE}/${PS}/." "${STAGE}/${PN}"
}

BB_HASHFILENAME = "${PF}"
BB_HASHCHECK_FUNCTION = "sstate_checkhashes"

def sstate_checkhashes(sq_fn, sq_task, sq_hash, sq_hashfn, d, siginfo=False):
    hashes = []
    for pid in range(len(sq_fn)):
        ps = generate_sstatefn(sq_hashfn[pid], sq_hash[pid], d)
        sstate = d.expand("${SSTATE}/" + ps)
        if os.path.exists(sstate) or os.path.exists(sstate + ".tar.gz"):
            bb.debug(1, "Found valid sstate %s" % ps)
            hashes.append(pid)
        else:
            bb.debug(1, "Unavailable sstate %s" % ps)

    return hashes

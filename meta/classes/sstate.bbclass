def generate_sstatefn(p, hash, d):
    if not hash:
        hash = "INVALID"
    return p + "_" + hash

PS = "${@generate_sstatefn(d.getVar('PF'), d.getVar('BB_TASKHASH'), d)}"

SSTATETASKS = "do_stage"

python() {
    d.appendVarFlag("do_stage", "postfuncs", " stage_savescene")
}

stage_savescene[cleandirs] = " ${SSTATE}/${PS}"

stage_savescene() {
  cp -a "${STAGE}/${PN}/." "${SSTATE}/${PS}"
}

addtask stage_setscene
do_stage_setscene[cleandirs] = "${STAGE}/${PN}"

do_stage_setscene() {
  cp -a "${SSTATE}/${PS}/." "${STAGE}/${PN}"
}

BB_HASHFILENAME = "${PF}"
BB_HASHCHECK_FUNCTION = "sstate_checkhashes"

def sstate_checkhashes(sq_fn, sq_task, sq_hash, sq_hashfn, d, siginfo=False):
    hashes = []
    for pid in range(len(sq_fn)):
        ps = generate_sstatefn(sq_hashfn[pid], sq_hash[pid], d)
        sstate = d.expand("${SSTATE}/" + ps)
        if os.path.exists(sstate):
            bb.debug(1, "Found valid sstate %s" % ps)
            hashes.append(pid)
        else:
            bb.debug(1, "Unavailable sstate %s" % ps)

    return hashes

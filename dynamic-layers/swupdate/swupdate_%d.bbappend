FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI += "\
	file://01_lc_type.conf \
	file://10_platf_env.conf \
	file://80_reboot.conf \
	file://background.jpg \
	file://favicon.png \
	file://logo.png \
	file://swupdate.cfg \
"

do_cfg_process() {
	echo "${MACHINE} 1.0" > ${WORKDIR}/hwrevision
}

do_install:append () {
	install -m 644 ${WORKDIR}/background.jpg ${D}/www/images/
	install -m 644 ${WORKDIR}/favicon.png ${D}/www/images/
	install -m 644 ${WORKDIR}/logo.png ${D}/www/images/
	install -m 644 ${WORKDIR}/swupdate.cfg ${D}${sysconfdir}/
	install -d ${D}${sysconfdir}/swupdate/
	install -d ${D}${sysconfdir}/swupdate/conf.d/
	install -m 644 ${WORKDIR}/01_lc_type.conf ${D}${sysconfdir}/swupdate/conf.d/
	install -m 644 ${WORKDIR}/10_platf_env.conf ${D}${sysconfdir}/swupdate/conf.d/
	install -m 644 ${WORKDIR}/80_reboot.conf ${D}${sysconfdir}/swupdate/conf.d/
	install -m 644 ${WORKDIR}/hwrevision ${D}${sysconfdir}/
}

addtask cfg_process after do_patch before do_install

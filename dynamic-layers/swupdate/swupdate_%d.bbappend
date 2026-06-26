FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SYSTEMD_SRC_URI = "${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'file://systemd.cfg', '', d)}"

SRC_URI += "\
	file://01_lc_type.conf \
	file://10_platf_env.conf \
	file://80_reboot.conf \
	file://background.jpg \
	file://favicon.png \
	file://logo.png \
	file://swupdate.cfg.template \
	${SYSTEMD_SRC_URI} \
"

do_cfg_process() {
	sed -e 's/@@MACHINE_NAME@@/${MACHINE}/' ${UNPACKDIR}/swupdate.cfg.template > ${WORKDIR}/swupdate.cfg
	echo "${MACHINE} 1.0" > ${WORKDIR}/hwrevision
}

do_install:append () {
	install -m 644 ${UNPACKDIR}/background.jpg ${D}/www/images/
	install -m 644 ${UNPACKDIR}/favicon.png ${D}/www/images/
	install -m 644 ${UNPACKDIR}/logo.png ${D}/www/images/
	install -m 644 ${WORKDIR}/swupdate.cfg ${D}${sysconfdir}/
	install -d ${D}${sysconfdir}/swupdate/
	install -d ${D}${sysconfdir}/swupdate/conf.d/
	install -m 644 ${UNPACKDIR}/01_lc_type.conf ${D}${sysconfdir}/swupdate/conf.d/
	install -m 644 ${UNPACKDIR}/10_platf_env.conf ${D}${sysconfdir}/swupdate/conf.d/
	install -m 644 ${UNPACKDIR}/80_reboot.conf ${D}${sysconfdir}/swupdate/conf.d/
	install -m 644 ${WORKDIR}/hwrevision ${D}${sysconfdir}/
}

addtask cfg_process after do_patch before do_install

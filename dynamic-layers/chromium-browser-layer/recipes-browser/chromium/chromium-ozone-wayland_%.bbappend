FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI:append = " \
    ${@bb.utils.contains('DISTRO_FEATURES', 'x11', '', 'file://0001-Remove-DRI-dependency.patch' , d)}"

# Add helper to launch chromium as user weston from weston.ini
SRC_URI:append = " \
    file://chromium-weston \
"

do_install:append() {
    install -d ${D}/${bindir}
    install -m 0755 ${WORKDIR}/chromium-weston ${D}/${bindir}/chromium-weston
}

FILES:${PN} += " \
    ${bindir}/chromium-weston \
"

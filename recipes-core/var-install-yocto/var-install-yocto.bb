SUMMARY = "Variscite target Yocto install for recovery images."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

# For historical reasons, scripts are located outside ${PN} directory
FILESEXTRAPATHS:prepend := "${THISDIR}/../../scripts/var_mk_yocto_sdcard/variscite_scripts:"

INSTALL_SCRIPT ?= "mx8_install_yocto.sh"
INSTALL_SCRIPT_NAME ?= "install_yocto.sh"

SRC_URI = "\
    file://${INSTALL_SCRIPT} \
    file://echos.sh \
"

do_install() {
    install -Dm 0755 ${WORKDIR}/${INSTALL_SCRIPT} ${D}${bindir}/${INSTALL_SCRIPT_NAME}
    install -Dm 0755 ${WORKDIR}/echos.sh ${D}${bindir}/echos.sh
}

FILES:${PN} = "\
    ${bindir} \
"

RDEPENDS:${PN} = "\
    bash \
    zstd \
"

COMPATIBLE_MACHINE = "(mx8-nxp-bsp|mx9-nxp-bsp)"

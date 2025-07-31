SUMMARY = "Variscite target Yocto install for recovery images."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

# For historical reasons, scripts are located outside ${PN} directory
FILESEXTRAPATHS:prepend := "${THISDIR}/../../scripts/var_mk_yocto_sdcard/variscite_scripts:"

INSTALL_SCRIPT:imx6ul-var-dart ?= "mx6ul_mx7_install_yocto.sh"
INSTALL_SCRIPT:var-som-mx6 ?= "mx6_install_yocto.sh"
INSTALL_SCRIPT:mx7-nxp-bsp ?= "mx6ul_mx7_install_yocto.sh"
INSTALL_SCRIPT:mx8-nxp-bsp ?= "mx8_install_yocto.sh"
INSTALL_SCRIPT:mx9-nxp-bsp ?= "mx8_install_yocto.sh"

INSTALL_SCRIPT_NAME ?= "install_yocto.sh"

SRC_URI = "\
    file://${INSTALL_SCRIPT} \
    file://echos.sh \
    file://mx6_install_yocto_emmc.sh \
    file://imx6ul_install_emmc_smart.sh \
    file://imx6ul-var-dart_yocto_nand_wifi.desktop \
    file://imx6ul-var-dart_yocto_emmc.desktop \
    file://imx6ul-var-dart_yocto_nand_sd.desktop \
    file://imx7-var-som_yocto.desktop \
"

do_install() {
    install -Dm 0755 ${UNPACKDIR}/${INSTALL_SCRIPT} ${D}${bindir}/${INSTALL_SCRIPT_NAME}
    install -Dm 0755 ${UNPACKDIR}/echos.sh ${D}${bindir}/echos.sh
}

do_install:append:var-som-mx6() {
    install -Dm 0755 ${UNPACKDIR}/mx6_install_yocto_emmc.sh ${D}${bindir}/install_yocto_emmc.sh
}

# Desktop files are just available for old legacy platforms
do_install:append:mx7-nxp-bsp() {
    install -d ${D}${datadir}/applications
    install -Dm 0644 ${UNPACKDIR}/imx7-var-som_yocto.desktop ${D}${datadir}/applications/
}

do_install:append:imx6ul-var-dart() {
    install -d ${D}${datadir}/applications
    install -Dm 0755 ${UNPACKDIR}/imx6ul_install_emmc_smart.sh ${D}${bindir}/
    install -Dm 0644 ${UNPACKDIR}/imx6ul-var-dart_yocto_emmc.desktop ${D}${datadir}/applications/
    if ${@bb.utils.contains('MACHINE_FEATURES', 'nand-flash', 'true', 'false', d)}; then
        install -Dm 0644 ${UNPACKDIR}/imx6ul-var-dart_yocto_nand_sd.desktop ${D}${datadir}/applications/
        install -Dm 0644 ${UNPACKDIR}/imx6ul-var-dart_yocto_nand_wifi.desktop ${D}${datadir}/applications/
    fi
}

FILES:${PN} = "\
    ${bindir} \
    ${datadir} \
"

IMX6_7_EXTRA_RDEPENDS = "\
    imx-kobs \
    dosfstools \
    i2c-tools \
    libubootenv \
    mtd-utils \
    mtd-utils-ubifs \
"

RDEPENDS:${PN} = "\
    bash \
    e2fsprogs-e2fsck \
    e2fsprogs-mke2fs \
    tar \
    util-linux-fdisk \
    zstd \
"

RDEPENDS:${PN}:append:mx6-nxp-bsp = "${IMX6_7_EXTRA_RDEPENDS}"
RDEPENDS:${PN}:append:mx7-nxp-bsp = "${IMX6_7_EXTRA_RDEPENDS}"

COMPATIBLE_MACHINE = "(mx6-nxp-bsp|mx7-nxp-bsp|mx8-nxp-bsp|mx9-nxp-bsp)"

DESCRIPTION = "Variscite recipe to package installer artifacts into a UUU demo \
image, and a var-recovery-image"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

DEPENDS = "\
    tar-native \
    zstd-native \
"

VAR_UUU_INSTALL = "false"
VAR_UUU_INSTALL:mx8-nxp-bsp = "true"
VAR_UUU_INSTALL:mx9-nxp-bsp = "true"

# Default value if the variable is not set
VAR_RECOVERY_SD_NAME ?= "var-recovery-image-${MACHINE}"
VAR_RECOVERY_TARGET_ROOTFS ?= "fsl-image-gui"

PACKAGE_RELEASE_NAME = "${VAR_RECOVERY_SD_NAME}"

do_configure[noexec] = "1"
do_compile[noexec] = "1"

do_install[depends] += " \
    var-image-swupdate:do_image_complete \
    var-image-swu:do_swuimage \
    var-recovery-image:do_image_complete \
    ${@bb.utils.contains('VAR_UUU_INSTALL', 'true', ' var-uuu-installer:do_install', '', d)} \
    var-image-swupdate:do_create_image_sbom_spdx \
    ${VAR_RECOVERY_TARGET_ROOTFS}:do_create_image_sbom_spdx \
"

do_install () {
    if ${VAR_UUU_INSTALL}; then
        install -Dm 0644 ${DEPLOY_DIR_IMAGE}/var-image-swupdate-${MACHINE}.rootfs.wic.bmap ${S}/${PN}/yocto/var-image-swupdate-${MACHINE}.rootfs.wic.bmap
    fi
    install -Dm 0644 ${DEPLOY_DIR_IMAGE}/var-image-swupdate-${MACHINE}.rootfs.wic.zst ${S}/${PN}/yocto/var-image-swupdate-${MACHINE}.rootfs.wic.zst
    install -Dm 0644 ${DEPLOY_DIR_IMAGE}/var-image-swupdate-${MACHINE}.rootfs.spdx.json ${S}/${PN}/yocto/var-image-swupdate-${MACHINE}.rootfs.spdx.json
    install -Dm 0644 ${DEPLOY_DIR_IMAGE}/var-image-swupdate-${MACHINE}.rootfs.vex.json ${S}/${PN}/yocto/var-image-swupdate-${MACHINE}.rootfs.vex.json
    install -Dm 0644 ${DEPLOY_DIR_IMAGE}/var-image-swu-${MACHINE}.rootfs.swu ${S}/${PN}/yocto/var-image-swu-${MACHINE}.rootfs.swu
    if ${VAR_UUU_INSTALL}; then
        install -Dm 0644 ${DEPLOY_DIR_IMAGE}/${VAR_RECOVERY_SD_NAME}.wic.bmap ${S}/${PN}/${VAR_RECOVERY_SD_NAME}.wic.bmap
    fi
    install -Dm 0644 ${DEPLOY_DIR_IMAGE}/${VAR_RECOVERY_SD_NAME}.wic.zst ${S}/${PN}/${VAR_RECOVERY_SD_NAME}.wic.zst
    install -Dm 0644 ${DEPLOY_DIR_IMAGE}/${VAR_RECOVERY_TARGET_ROOTFS}-${MACHINE}.rootfs.spdx.json ${S}/${PN}/yocto/${VAR_RECOVERY_TARGET_ROOTFS}-${MACHINE}.rootfs.spdx.json
    install -Dm 0644 ${DEPLOY_DIR_IMAGE}/${VAR_RECOVERY_TARGET_ROOTFS}-${MACHINE}.rootfs.vex.json ${S}/${PN}/yocto/${VAR_RECOVERY_TARGET_ROOTFS}-${MACHINE}.rootfs.vex.json

    if [ -f ${DEPLOY_DIR_IMAGE}/var-uuu-installer.tar.zst ]; then
        install -Dm 0644 ${DEPLOY_DIR_IMAGE}/var-uuu-installer.tar.zst ${S}/var-uuu-installer.tar.zst
        tar --zstd -xf var-uuu-installer.tar.zst -C ${PN}/yocto/
    fi

    tar -cf - ${PN} | zstd -19 -T0 -f -o ${PACKAGE_RELEASE_NAME}.tar.zst

    install -Dm 0644 ${PACKAGE_RELEASE_NAME}.tar.zst ${DEPLOY_DIR_IMAGE}/${PACKAGE_RELEASE_NAME}.tar.zst
    cd ${DEPLOY_DIR_IMAGE}
    ln -sf ${PACKAGE_RELEASE_NAME}.tar.zst ${DEPLOY_DIR_IMAGE}/${PACKAGE_RELEASE_NAME}-${MACHINE}.tar.zst
}

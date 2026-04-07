DESCRIPTION = "Variscite recipe to package installer artifacts into a UUU demo \
image, and a var-recovery-image"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

DEPENDS = "\
    tar-native \
    zstd-native \
    var-uuu-installer \
"

# Default value if the variable is not set
VAR_RECOVERY_SD_NAME ?= "var-recovery-image-${MACHINE}"

PACKAGE_RELEASE_NAME = "${VAR_RECOVERY_SD_NAME}"

do_configure[noexec] = "1"
do_compile[noexec] = "1"

do_install[depends] += " \
    var-image-swupdate:do_image_complete \
    var-image-swu:do_swuimage \
    var-recovery-image:do_image_complete \
    var-uuu-installer:do_install \
"

do_install () {
    install -Dm 0644 ${DEPLOY_DIR_IMAGE}/var-uuu-installer.tar.zst ${S}/var-uuu-installer.tar.zst
    install -Dm 0644 ${DEPLOY_DIR_IMAGE}/var-image-swupdate-${MACHINE}.rootfs.wic.bmap ${S}/${PN}/yocto/var-image-swupdate-${MACHINE}.rootfs.wic.bmap
    install -Dm 0644 ${DEPLOY_DIR_IMAGE}/var-image-swupdate-${MACHINE}.rootfs.wic.zst ${S}/${PN}/yocto/var-image-swupdate-${MACHINE}.rootfs.wic.zst
    install -Dm 0644 ${DEPLOY_DIR_IMAGE}/var-image-swupdate-${MACHINE}.rootfs.spdx.tar.zst ${S}/${PN}/yocto/var-image-swupdate-${MACHINE}.rootfs.spdx.tar.zst
    install -Dm 0644 ${DEPLOY_DIR_IMAGE}/var-image-swupdate-${MACHINE}.rootfs.cve ${S}/${PN}/yocto/var-image-swupdate-${MACHINE}.cve
    install -Dm 0644 ${DEPLOY_DIR_IMAGE}/var-image-swu-${MACHINE}.rootfs.swu ${S}/${PN}/yocto/var-image-swu-${MACHINE}.rootfs.swu
    install -Dm 0644 ${DEPLOY_DIR_IMAGE}/${VAR_RECOVERY_SD_NAME}.wic.bmap ${S}/${PN}/${VAR_RECOVERY_SD_NAME}.wic.bmap
    install -Dm 0644 ${DEPLOY_DIR_IMAGE}/${VAR_RECOVERY_SD_NAME}.wic.zst ${S}/${PN}/${VAR_RECOVERY_SD_NAME}.wic.zst
    install -Dm 0644 ${DEPLOY_DIR_IMAGE}/${VAR_RECOVERY_SD_NAME}.spdx.tar.zst ${S}/${PN}/yocto/${VAR_RECOVERY_SD_NAME}.spdx.tar.zst
    install -Dm 0644 ${DEPLOY_DIR_IMAGE}/${VAR_RECOVERY_SD_NAME}.cve ${S}/${PN}/yocto/${VAR_RECOVERY_SD_NAME}.cve

    tar --zstd -xf var-uuu-installer.tar.zst  -C ${PN}/yocto/

    tar -cf - ${PN} | zstd -19 -T0 -f -o ${PACKAGE_RELEASE_NAME}.tar.zst

    install -Dm 0644 ${PACKAGE_RELEASE_NAME}.tar.zst ${DEPLOY_DIR_IMAGE}/${PACKAGE_RELEASE_NAME}.tar.zst
    cd ${DEPLOY_DIR_IMAGE}
    ln -sf ${PACKAGE_RELEASE_NAME}.tar.zst ${DEPLOY_DIR_IMAGE}/${PACKAGE_RELEASE_NAME}-${MACHINE}.tar.zst
}

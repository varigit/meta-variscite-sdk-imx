# This recipe builds a UUU (NXP mfgtools) installer bundle.
#
# What it does:
# - Generates a uuu_script that boots the board via ROM (SDP/SDPS/SDPV) and
#   flashes the bootloader + rootfs to eMMC using NXP mfgtools.
# - Collects the deploy artifacts (IMAGE_BOOTLOADER and <image>-<machine>.rootfs.wic/.bmap),
#   packs them into a single ${PN}.tar.zst, and deploys it to DEPLOY_DIR_IMAGE.
#
# Required variable:
# - VAR_FLASH_IMAGE_NAME: base image name used to pick the rootfs artifacts from deploy
#   (<VAR_FLASH_IMAGE_NAME>-<MACHINE>.rootfs.wic and .wic.bmap).
#   If VAR_RECOVERY_TARGET_ROOTFS is set, it overrides VAR_FLASH_IMAGE_NAME.
#   If neither is set, VAR_FLASH_IMAGE_NAME defaults to "fsl-image-gui-chromium".

DESCRIPTION = "Variscite class to build an installation package containing the \
bootloader/rootfs images and a UUU script, ready to be flashed via NXP mfgtools \
(UUU)."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

DEPENDS += "\
    tar-native \
    zstd-native \
"

do_configure[noexec] = "1"
do_compile[noexec] = "1"

uuu_script() {
    cat > ${B}/uuu_script <<EOF
uuu_version 1.4.149
# This command will be run when i.MX6/7 i.MX8MM, i.MX8MQ
SDP: boot -f ${IMAGE_BOOTLOADER} -scanlimited 0x800000

# This command will be run when ROM support stream mode
# i.MX8QXP, i.MX8QM
SDPS: boot -scanterm -f ${IMAGE_BOOTLOADER} -scanlimited 0x800000

SDPV: delay 1000
SDPV: write -f ${IMAGE_BOOTLOADER} -skipspl -scanterm -scanlimited 0x800000
SDPV: jump -scanlimited 0x800000

FB: ucmd setenv fastboot_dev mmc
FB: ucmd setenv mmcdev \${emmc_dev}
FB: ucmd mmc dev \${emmc_dev}
FB: flash -raw2sparse -bmap ${VAR_FLASH_IMAGE_NAME}-${MACHINE}.rootfs.wic.bmap all ${VAR_FLASH_IMAGE_NAME}-${MACHINE}.rootfs.wic
FB: flash -scanterm -scanlimited 0x800000 bootloader ${IMAGE_BOOTLOADER}
FB: done
EOF
}

python handle_uuu_image_variables() {
    if d.getVar('VAR_RECOVERY_TARGET_ROOTFS'):
        d.setVar('VAR_FLASH_IMAGE_NAME', d.getVar('VAR_RECOVERY_TARGET_ROOTFS'))
    elif not d.getVar('VAR_FLASH_IMAGE_NAME'):
        bb.warn("VAR_FLASH_IMAGE_NAME is empty. Setting variable to use fsl-image-gui-chromium as default.")
        d.setVar('VAR_FLASH_IMAGE_NAME', "fsl-image-gui-chromium")
    else:
        bb.warn("Using %s as default image." % d.getVar('VAR_FLASH_IMAGE_NAME'))
}

addhandler handle_uuu_image_variables
handle_uuu_image_variables[eventmask] = "bb.event.RecipePreFinalise"

do_generate_uuu_script() {
        uuu_script
}

do_generate_uuu_script[dirs] = "${B}"

addtask generate_uuu_script after do_unpack before do_install

do_install[depends] += "${VAR_FLASH_IMAGE_NAME}:do_image_complete"

do_install() {
    install -Dm 0755 ${B}/uuu_script ${B}/uuu-installer/uuu_script
    install -Dm 0644 ${DEPLOY_DIR_IMAGE}/${IMAGE_BOOTLOADER} ${B}/uuu-installer/${IMAGE_BOOTLOADER}
    install -Dm 0644 ${DEPLOY_DIR_IMAGE}/${VAR_FLASH_IMAGE_NAME}-${MACHINE}.rootfs.wic ${B}/uuu-installer/${VAR_FLASH_IMAGE_NAME}-${MACHINE}.rootfs.wic
    install -Dm 0644 ${DEPLOY_DIR_IMAGE}/${VAR_FLASH_IMAGE_NAME}-${MACHINE}.rootfs.wic.bmap ${B}/uuu-installer/${VAR_FLASH_IMAGE_NAME}-${MACHINE}.rootfs.wic.bmap

    tar -cf - uuu-installer | zstd -19 -T0 -f -o ${PN}.tar.zst

    install -Dm 0644 ${PN}.tar.zst ${DEPLOY_DIR_IMAGE}/${PN}.tar.zst
    cd ${DEPLOY_DIR_IMAGE}
    ln -sf ${PN}.tar.zst ${DEPLOY_DIR_IMAGE}/${PN}-${MACHINE}.tar.zst
}

ALLOW_EMPTY:${PN} = "1"

PACKAGE_ARCH = "${MACHINE_ARCH}"

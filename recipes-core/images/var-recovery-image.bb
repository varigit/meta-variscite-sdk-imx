# Copyright (C) 2023 Variscite Ltd
# Released under the MIT license (see COPYING.MIT for the terms)

DESCRIPTION = "Variscite bootable recovery SD card image used for installing \
    various images on the eMMC or NAND."
LICENSE = "MIT"

# The base of the running SD recovery image defaults to fsl-image-gui.
# VAR_RECOVERY_SD_IMAGE can be overridden in local.conf or other conf files.
VAR_RECOVERY_SD_IMAGE ?= "recipes-fsl/images/fsl-image-gui.bb"

# The target rootfs that is programmed to the eMMC also defaults to fsl-image-gui.
# VAR_RECOVERY_TARGET_ROOTFS can be overridden via an environment variable or in
# local.conf or other conf files.
VAR_RECOVERY_TARGET_ROOTFS ?= "fsl-image-gui"


VAR_RECOVERY_DEPENDS_IMX6_7 = "u-boot"
VAR_RECOVERY_DEPENDS_IMX8_9 = "imx-boot"

VAR_RECOVERY_DEPENDS:mx6-nxp-bsp = "${VAR_RECOVERY_DEPENDS_IMX6_7}"
VAR_RECOVERY_DEPENDS:mx7-nxp-bsp = "${VAR_RECOVERY_DEPENDS_IMX6_7}"
VAR_RECOVERY_DEPENDS:mx8-nxp-bsp = "${VAR_RECOVERY_DEPENDS_IMX8_9}"
VAR_RECOVERY_DEPENDS:mx9-nxp-bsp = "${VAR_RECOVERY_DEPENDS_IMX8_9}"

VAR_RECOVERY_IMAGES_IMX6_7 = " \
    ${IMAGE_BOOT_FILES} \
    SPL-nand \
    SPL-sd \
    u-boot.img-sd \
    u-boot.img-nand \
"

VAR_RECOVERY_IMAGES_IMX8_9 = "imx-boot"

VAR_RECOVERY_IMAGES:mx6-nxp-bsp = "${VAR_RECOVERY_IMAGES_IMX6_7}"
VAR_RECOVERY_IMAGES:mx7-nxp-bsp = "${VAR_RECOVERY_IMAGES_IMX6_7}"

VAR_RECOVERY_IMAGES:mx8-nxp-bsp = "${VAR_RECOVERY_DEPENDS_IMX8_9}"
VAR_RECOVERY_IMAGES:mx9-nxp-bsp = "${VAR_RECOVERY_DEPENDS_IMX8_9}"

# The file must then be renamed to follow the install_yocto.sh standard name.
VAR_RECOVERY_IMAGE_RENAME[imx-boot] = "imx-boot-sd.bin"

# Use the var-recovery bbclass
inherit var-recovery

# Install Android files for mx8 machines
IMAGE_INSTALL:append:mx8-nxp-bsp = " \
    ${@bb.utils.contains('DISTRO', 'fsl-imx-xwayland', 'var-install-android', '', d)} \
"

# Install Android files for var-som-mx6
IMAGE_INSTALL:append:var-som-mx6 = " \
    ${@bb.utils.contains('DISTRO', 'fsl-imx-xwayland', 'var-install-android', '', d)} \
"

# var-recovery can install to NAND, but it is too big to be installed on NAND
IMAGE_FSTYPES:remove = "multiubi"

COMPATIBLE_MACHINE = "(mx6-nxp-bsp|mx7-nxp-bsp|mx8-nxp-bsp|mx9-nxp-bsp)"

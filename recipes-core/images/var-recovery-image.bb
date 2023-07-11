# Copyright (C) 2023 Variscite Ltd
# Released under the MIT license (see COPYING.MIT for the terms)

DESCRIPTION = "Variscite bootable recovery SD card image used for installing \
    various images on the eMMC."
LICENSE = "MIT"

# The base of the running SD recovery image defaults to fsl-image-gui.
# VAR_RECOVERY_SD_IMAGE can be overridden in local.conf or other conf files.
VAR_RECOVERY_SD_IMAGE ?= "recipes-fsl/images/fsl-image-gui.bb"

# The target rootfs that is programmed to the eMMC also defaults to fsl-image-gui.
# VAR_RECOVERY_TARGET_ROOTFS can be overridden via an environment variable or in
# local.conf or other conf files.
VAR_RECOVERY_TARGET_ROOTFS ?= "fsl-image-gui"


# The recovery SD image is dependent on imx-boot components
VAR_RECOVERY_DEPENDS = "\
   imx-boot \
"

# Package the imx-boot generated file in the SD image
VAR_RECOVERY_IMAGES:mx9-nxp-bsp = "\
    imx-boot-imx93-var-som-sd.bin-flash_singleboot \
"
# The file must then be renamed to follow the install_yocto.sh standard name.
VAR_RECOVERY_IMAGE_RENAME[imx-boot-imx93-var-som-sd.bin-flash_singleboot] = "imx-boot-sd.bin"

# Use the var-recovery bbclass
inherit var-recovery

COMPATIBLE_MACHINE = "(mx9-nxp-bsp)"

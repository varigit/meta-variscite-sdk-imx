# Copyright (C) 2015 Freescale Semiconductor
# Released under the MIT license (see COPYING.MIT for the terms)

DESCRIPTION = "Freescale Image to validate i.MX machines. \
This image contains everything used to test i.MX machines including GUI, \
demos and lots of applications. This creates a very large image, not \
suitable for production."
LICENSE = "MIT"

inherit core-image

### WARNING: This image is NOT suitable for production use and is intended
###          to provide a way for users to reproduce the image used during
###          the validation process of i.MX BSP releases.

# Since Yocto 5.2 (walnascar) the image feature debug-tweaks as has been deprecated.
# The following new image features are recommended as a replacement.
# See: https://docs.yoctoproject.org/migration-guides/migration-5.2.html
DEBUG_TWEAKS = " \
    allow-empty-password \
    allow-root-login \
    empty-root-password \
    post-install-logging \
"

IMAGE_FEATURES += " \
    splash \
    ssh-server-openssh \
    hwcodecs \
    ${DEBUG_TWEAKS} \
    nfs-client \
    tools-debug \
    tools-testapps \
    ${@bb.utils.contains('DISTRO_FEATURES', 'wayland', 'weston', \
       bb.utils.contains('DISTRO_FEATURES',     'x11', 'x11-base x11-sato', \
                                                       '', d), d)} \
"

CORE_IMAGE_EXTRA_INSTALL += " \
	packagegroup-core-full-cmdline \
	packagegroup-tools-bluetooth \
	packagegroup-imx-tools-audio \
	packagegroup-fsl-tools-gpu-external \
	packagegroup-fsl-tools-testapps \
	packagegroup-fsl-tools-benchmark \
	packagegroup-fsl-gstreamer1.0 \
	packagegroup-fsl-gstreamer1.0-full \
	packagegroup-imx-isp \
	packagegroup-variscite-imx-ml \
	packagegroup-variscite-imx-security \
	${@bb.utils.contains('DISTRO_FEATURES', 'virtualization', 'packagegroup-variscite-imx-docker', '', d)} \
	packagegroup-variscite-devel \
	${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'systemd-analyze', '', d)} \
	${@bb.utils.contains('DISTRO_FEATURES', 'x11', 'xterm', '', d)} \
	${@bb.utils.contains('DISTRO_FEATURES', 'x11 wayland', 'weston-xwayland', '', d)} \
	${@bb.utils.contains('DISTRO_FEATURES', 'wayland', 'weston-init', \
	   bb.utils.contains('DISTRO_FEATURES',     'x11', 'packagegroup-core-x11-sato-games', \
							 '', d), d)} \
"

CORE_IMAGE_EXTRA_INSTALL:append:mx8-nxp-bsp = "\
    packagegroup-fsl-tools-gpu \
    packagegroup-variscite-swupdate \
"

CORE_IMAGE_EXTRA_INSTALL:append:mx95-nxp-bsp = "\
    packagegroup-fsl-tools-gpu \
"

CORE_IMAGE_EXTRA_INSTALL:append:mx9-nxp-bsp = "\
    packagegroup-variscite-swupdate \
"

systemd_disable_vt () {
    rm ${IMAGE_ROOTFS}${sysconfdir}/systemd/system/getty.target.wants/getty@tty*.service
}

IMAGE_PREPROCESS_COMMAND:append = " ${@ 'systemd_disable_vt;' if bb.utils.contains('DISTRO_FEATURES', 'systemd', True, False, d) and bb.utils.contains('USE_VT', '0', True, False, d) else ''} "

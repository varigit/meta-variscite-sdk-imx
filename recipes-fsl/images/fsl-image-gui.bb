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

IMAGE_FEATURES += " \
    splash \
    ssh-server-dropbear \
    hwcodecs \
    debug-tweaks \
    nfs-server \
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
	packagegroup-fsl-isp \
	packagegroup-variscite-imx-ml \
	packagegroup-variscite-imx-security \
	${@bb.utils.contains('DISTRO_FEATURES', 'virtualization', 'packagegroup-variscite-imx-docker', '', d)} \
	${@bb.utils.contains('DISTRO_FEATURES', 'x11', 'xterm', '', d)} \
	${@bb.utils.contains('DISTRO_FEATURES', 'x11 wayland', 'weston-xwayland', '', d)} \
	${@bb.utils.contains('DISTRO_FEATURES', 'wayland', 'weston-init', \
	   bb.utils.contains('DISTRO_FEATURES',     'x11', 'packagegroup-core-x11-sato-games', \
							 '', d), d)} \
	nodejs \
	screen \
	tcf-agent \
	openssh-sftp-server \
	libgpiod \
	libgpiod-tools \
"

# only for Android enabled machines
IMAGE_INSTALL:append:imxgpu3d = " \
	android-tools \
	android-tools-adbd \
	android-tools-fstools \
	f2fs-tools \
"

# only for DRM enabled machines
IMAGE_INSTALL:append:imxdrm = " \
	libdrm-tests \
"

CORE_IMAGE_EXTRA_INSTALL:append:mx8-nxp-bsp = "\
    packagegroup-fsl-tools-gpu \
"

systemd_disable_vt () {
    rm ${IMAGE_ROOTFS}${sysconfdir}/systemd/system/getty.target.wants/getty@tty*.service
}

IMAGE_PREPROCESS_COMMAND:append = " ${@ 'systemd_disable_vt;' if bb.utils.contains('DISTRO_FEATURES', 'systemd', True, False, d) and bb.utils.contains('USE_VT', '0', True, False, d) else ''} "

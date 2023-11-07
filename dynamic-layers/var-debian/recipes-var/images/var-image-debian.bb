# A desktop image with an Desktop rootfs
#
# Note that we have a tight dependency to debian-base
# and that we cannot just install arbitrary Yocto packages to avoid
# rootfs pollution or destruction.
PV = "${@d.getVar('PREFERRED_VERSION_debian-base', True) or '1.0'}"

require var-image-common.inc

REQUIRED_DISTRO_FEATURES = "wayland"

ML_NNSTREAMER_PKGS = " \
	nnstreamer \
	nnstreamer-tensorflow-lite \
	nnstreamer-python3 \
	nnstreamer-protobuf \
"

SWUPDATE_PKGS = " \
	swupdate \
	swupdate-www \
	kernel-image \
	kernel-devicetree \
"

# This must be added first as it provides the foundation for
# subsequent modifications to the rootfs
IMAGE_INSTALL += "\
	debian-base \
	debian-base-dev \
	debian-base-dbg \
	debian-base-doc \
"

APTGET_EXTRA_PACKAGES += "\
	ntpdate patchelf \
	libpixman-1-0 libpango-1.0-0 libpangocairo-1.0-0 \
	squashfs-tools \
	bluez \
	bluez-tools \
	docker-compose \
	docker.io \
	gpiod \
	hdparm \
	hostapd \
	i2c-tools \
	iw \
	kmod \
	pm-utils \
	rng-tools \
	u-boot-tools \
	wireless-regdb \
	wireless-tools \
	xterm \
	zstd \
	util-linux \
	fdisk \
"

##############################################################################
# NOTE: We cannot install arbitrary Yocto packages as they will
# conflict with the content of the prebuilt Desktop rootfs and pull
# in dependencies that may break the rootfs.
# Any package addition needs to be carefully evaluated with respect
# to the final image that we build.
##############################################################################

IMAGE_INSTALL += " \
	packagegroup-fsl-gstreamer1.0 \
	packagegroup-fsl-gstreamer1.0-full \
"

# # GPU driver
G2D_SAMPLES                 = ""
G2D_SAMPLES:imxgpu2d        = "imx-g2d-samples"
G2D_SAMPLES:imxdpu          = "imx-g2d-samples"

IMAGE_FEATURES += " \
    ${@bb.utils.contains('DISTRO_FEATURES', 'wayland', 'weston', \
       bb.utils.contains('DISTRO_FEATURES',     'x11', 'x11-base x11-sato', \
                                                       '', d), d)} \
"

IMAGE_INSTALL += " \
	weston \
	weston-xwayland \
"

IMAGE_INSTALL += " \
	wayland-protocols \
	libgles1-imx libgles1-imx-dev \
	libgles2-imx libgles2-imx-dev \
	libgles3-imx-dev \
	libglslc-imx \
	libopencl-imx \
	libegl-imx libegl-imx-dev \
	libgal-imx libgal-imx-dev \
	libvsc-imx \
	libgbm-imx libgbm-imx-dev \
	libvulkan-imx \
	libopenvx-imx libopenvx-imx-dev \
	libnn-imx \
	libdrm-vivante \
	imx-gpu-viv-tools \
	libgpuperfcnt \
	${G2D_SAMPLES} \
	apitrace \
	gputop \
	imx-gpu-sdk \
	weston-examples \
	u-boot-default-env \
	u-boot-fw-utils \
	${SWUPDATE_PKGS} \
"

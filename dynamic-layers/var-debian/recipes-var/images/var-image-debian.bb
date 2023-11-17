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

APTGET_ML_PKGS = " \
	python3-torch \
	python3-torchvision \
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
	iperf3 \
	curl \
	lvm2 \
	can-utils \
	${APTGET_ML_PKGS} \
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

BRCM_YOCTO_PACKAGES = " \
	bcm43xx-utils \
	brcm-patchram-plus \
	linux-firmware-bcm4339 \
	linux-firmware-bcm43430 \
"

# TODO: Merge this with variscite.inc MACHINE_EXTRA_RDEPENDS
# For now, MACHINE_EXTRA_RDEPENDS pulls in too many dependencies
# that don't make sense to add to the debian-base image. To avoid
# conflicts, install them here instead
MACHINE_EXTRA_RDEPENDS_YOCTO = " \
	var-mii \
	u-boot-fw-utils \
	u-boot-splash \
	u-boot-default-env \
	${@bb.utils.contains('MACHINE_FEATURES', 'nxpiw612-sdio', 'iw612-utils', '', d)} \
	${BRCM_YOCTO_PACKAGES} \
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
	${MACHINE_EXTRA_RDEPENDS_YOCTO} \
	${SWUPDATE_PKGS} \
	pm-utils-variscite \
	keyctl-caam \
	spidev-test \
	udev udev-extraconf \
	packagegroup-var-ml \
"

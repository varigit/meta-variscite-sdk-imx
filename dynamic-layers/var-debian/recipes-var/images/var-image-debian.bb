# Variscite Debian Image
#
# This image supports Variscite i.MX SoCs and includes hardware-specific packages
# like imx-atf, linux, gstreamer, and wayland, built using Yocto recipes.
# These packages are installed via IMAGE_INSTALL. All dependencies required by
# recipes in IMAGE_INSTALL must be provided by the debian-base recipe. Failure to
# do so will result in build errors due to package conflicts.
#
# Generic packages not dependent on any IMAGE_INSTALL packages (e.g., vim, docker)
# should be installed using APT.
#
DESCRIPTION = "This Variscite Debian image is tailored for testing Variscite i.MX \
machines. It includes a GUI, various demos, and a wide range of applications, \
making it ideal for development and testing. Based on Debian [version], this image \
is comprehensive but large, and may not be optimized for production use."
LICENSE = "MIT"

require var-image-common.inc

REQUIRED_DISTRO_FEATURES = "wayland"

IMAGE_FEATURES += " \
	tools-sdk \
	debug-tweaks \
	tools-profile \
	package-management \
	splash \
	nfs-server \
	tools-debug \
	ssh-server-dropbear \
	tools-testapps \
	hwcodecs \
	${@bb.utils.contains('DISTRO_FEATURES', 'wayland', 'weston', \
	   bb.utils.contains('DISTRO_FEATURES',	 'x11', 'x11-base x11-sato', \
	   '', d), d)} \
"

APTGET_ML_PKGS = " \
	python3-torch \
	python3-torchvision \
"

APTGET_BT_PKGS = " \
	libasound2-plugin-bluez \
	libc6 \
	expect \
"

# Packages to be installed by Debian
APTGET_EXTRA_PACKAGES += "\
	apt \
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
	v4l-utils \
	${APTGET_ML_PKGS} \
	${APTGET_BT_PKGS} \
	console-setup locales \
	vim \
	ethtool wget ftp iputils-ping \
	net-tools \
	nfs-common \
	openssh-server \
	libtool autoconf pkg-config \
	python-is-python3 \
	netplan.io \
	network-manager \
"

##############################################################################
# NOTE: We cannot install arbitrary Yocto packages as they will
# conflict with the content of the prebuilt Desktop rootfs and pull
# in dependencies that may break the rootfs.
# Any package addition needs to be carefully evaluated with respect
# to the final image that we build.
##############################################################################

# Debian base root filesystem, provides all dependencies for other packages
# installed yocto, e.g. IMAGE_INSTALL
BB_DEBIAN_BASE = "\
	debian-base \
	debian-base-dev \
	debian-base-dbg \
	debian-base-doc \
"

# Kernel dev packages
BB_KERNEL_DEV_PKGS = " \
	kernel-dev \
	kernel-devsrc \
	kernel-modules \
	linux-imx-headers-dev \
"

# swupdate
BB_SWUPDATE_PKGS = " \
	swupdate \
	swupdate-www \
	kernel-image \
	kernel-devicetree \
"

# ml
BB_ML_PKGS = " \
	packagegroup-var-ml \
"

# camera
BB_CAMERA_PKGS = " \
	packagegroup-fsl-isp \
"

# gstreamer
BB_GSTREAMER_PKGS = " \
	packagegroup-fsl-gstreamer1.0 \
	packagegroup-fsl-gstreamer1.0-full \
"

# GPU driver
BB_G2D_SAMPLES                 = ""
BB_G2D_SAMPLES:imxgpu2d        = "imx-g2d-samples"
BB_G2D_SAMPLES:imxdpu          = "imx-g2d-samples"

# bcm4339 and bcm4339
BB_BRCM_PKGS = " \
	bcm43xx-utils \
	brcm-patchram-plus \
	linux-firmware-bcm4339 \
	linux-firmware-bcm43430 \
"

# TODO: Merge this with variscite.inc MACHINE_EXTRA_RDEPENDS
# For now, MACHINE_EXTRA_RDEPENDS pulls in too many dependencies
# that don't make sense to add to the debian-base image. To avoid
# conflicts, install them here instead
BB_MACHINE_EXTRA_RDEPENDS = " \
	var-mii \
	var-wallpapers \
	u-boot-fw-utils \
	u-boot-splash \
	u-boot-default-env \
	${@bb.utils.contains('MACHINE_FEATURES', 'nxpiw612-sdio', 'iw612-utils', '', d)} \
	${BB_BRCM_PKGS} \
	${BB_KERNEL_DEV_PKGS} \
"

BB_WESTON_PKGS = " \
	weston \
	weston-examples \
	weston-xwayland \
	wayland-protocols \
"

# Packages to be installed by Yocto
IMAGE_INSTALL += " \
	${BB_DEBIAN_BASE} \
	${MACHINE_EXTRA_RRECOMMENDS} \
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
	${BB_G2D_SAMPLES} \
	apitrace \
	gputop \
	imx-gpu-sdk \
	${BB_MACHINE_EXTRA_RDEPENDS} \
	${BB_SWUPDATE_PKGS} \
	pm-utils-variscite \
	keyctl-caam \
	spidev-test \
	udev udev-extraconf \
	alsa-state \
	bluealsa \
	${BB_ML_PKGS} \
	${BB_CAMERA_PKGS} \
	${BB_GSTREAMER_PKGS} \
	${BB_WESTON_PKGS} \
	perf \
"

# Remove GPU packags not supported by imx8mm
IMAGE_INSTALL:remove:mx8mm-nxp-bsp = " \
	libgles3-imx-dev \
	libclc-imx libclc-imx-dev \
	libopencl-imx \
	libvulkan-imx \
	libopenvx-imx libopenvx-imx-dev \
"

# Remove GPU packags not supported by imx93
IMAGE_INSTALL:remove:mx93-nxp-bsp = " \
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
	gputop \
	imx-gpu-sdk \
"

install_chromium() {
	# Install icon to the launch bar
	printf "\n[launcher]\nicon=/usr/share/icons/hicolor/24x24/apps/chromium.png\npath=/usr/sbin/runuser -l weston -c chromium" >> ${IMAGE_ROOTFS}${sysconfdir}/xdg/weston/weston.ini
}

install_obex_service() {
	local layer_dir="${TOPDIR}/../sources"

	# Copy obex.service to the systemd system directory
	install -m 0644 "${layer_dir}/meta-variscite-bsp-common/recipes-connectivity/bluez5/files/obex.service" "${IMAGE_ROOTFS}${libdir}/systemd/system/obex.service"

 	# Copy obexd.conf to the D-Bus system configuration directory
	install -m 0644 "${layer_dir}/meta-variscite-bsp-common/recipes-connectivity/bluez5/files/obexd.conf" "${IMAGE_ROOTFS}${sysconfdir}/dbus-1/system.d/obexd.conf"

	# Enable obex.service
	ln -sf "${libdir}/systemd/system/obex.service" "${IMAGE_ROOTFS}${sysconfdir}/systemd/system/multi-user.target.wants/obex.service"
}

install_wallpaper() {
	# Install default wallpaper for weston
	sed -i -e "/^\[shell\]/a background-type=scale-crop" ${IMAGE_ROOTFS}${sysconfdir}/xdg/weston/weston.ini
	sed -i -e "/^\[shell\]/a background-image=${datadir}/images/desktop-base/default" ${IMAGE_ROOTFS}${sysconfdir}/xdg/weston/weston.ini
}

ROOTFS_POSTPROCESS_COMMAND:prepend = " \
	install_wallpaper; \
	install_obex_service; \
	install_basefilesissue; \
	${@bb.utils.contains('IMAGE_INSTALL', 'chromium-ozone-wayland', 'install_chromium; ', '', d)}; \
"

IMAGE_INSTALL:append:mx8mp-nxp-bsp = " chromium-ozone-wayland"
IMAGE_INSTALL:append:mx8mm-nxp-bsp = " chromium-ozone-wayland"

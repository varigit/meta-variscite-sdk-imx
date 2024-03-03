# A desktop image with an Desktop rootfs
#
# and that we cannot just install arbitrary Yocto packages to avoid
# rootfs pollution or destruction.

IMAGE_LINGUAS = ""
IMAGE_INSTALL = ""
inherit core-image image nativeaptinstall features_check
export PACKAGE_INSTALL = "${IMAGE_INSTALL}"

APTGET_CHROOT_DIR = "${IMAGE_ROOTFS}"
APTGET_SKIP_UPGRADE = "1"

ROOTFS_POSTPROCESS_COMMAND:prepend = "compare_rootfs_with_debian_base;"
ROOTFS_POSTPROCESS_COMMAND:append = "do_fix_ldconfig; do_save_graphics; do_aptget_update; do_update_host; do_config_netplan;"
IMAGE_PREPROCESS_COMMAND:append = " do_config_systemd_resolved; do_enable_graphics; do_cleanup_rootfs; "


IMAGE_INSTALL += "\
	kernel-devicetree \
	kernel-image \
	${MACHINE_EXTRA_RRECOMMENDS} \
	perf \
"

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
	${@bb.utils.contains('DISTRO_FEATURES', 'wayland', '', \
	   bb.utils.contains('DISTRO_FEATURES',	 'x11', 'x11-base x11-sato', \
													   '', d), d)} \
"

APTGET_EXTRA_PACKAGES += "\
	console-setup locales \
	apt vim \
	ethtool wget ftp iputils-ping \
	net-tools \
	nfs-common \
	openssh-server \
	libtool autoconf pkg-config \
	bluez \
	python-is-python3 \
	netplan.io \
	network-manager \
"

APTGET_EXTRA_SOURCE_PACKAGES += "\
"

# Add user with password user and default shell bash
USER_SHELL_BASH ?= "/bin/bash"
# Default password: user
USER_PASSWD_USER ?= "zHipsIr46vtZk"
APTGET_ADD_USERS ?= "user:${USER_PASSWD_USER}:${USER_SHELL_BASH}"

HOST_NAME = "${MACHINE}"

fakeroot do_update_host() {
	set -x
	echo >"${APTGET_CHROOT_DIR}/etc/hostname" "${HOST_NAME}"

	echo  >"${APTGET_CHROOT_DIR}/etc/hosts" "127.0.0.1 localhost"
	echo >>"${APTGET_CHROOT_DIR}/etc/hosts" "127.0.1.1 ${HOST_NAME}"
	echo >>"${APTGET_CHROOT_DIR}/etc/hosts" ""
	echo >>"${APTGET_CHROOT_DIR}/etc/hosts" "# The following lines are desirable for IPv6 capable hosts"
	echo >>"${APTGET_CHROOT_DIR}/etc/hosts" "::1 ip6-localhost ip6-loopback"
	echo >>"${APTGET_CHROOT_DIR}/etc/hosts" "fe00::0 ip6-localnet"
	echo >>"${APTGET_CHROOT_DIR}/etc/hosts" "ff00::0 ip6-mcastprefix"
	echo >>"${APTGET_CHROOT_DIR}/etc/hosts" "ff02::1 ip6-allnodes"
	echo >>"${APTGET_CHROOT_DIR}/etc/hosts" "ff02::2 ip6-allrouters"
	echo >>"${APTGET_CHROOT_DIR}/etc/hosts" "ff02::3 ip6-allhosts"
	set +x
}

do_config_netplan() {
	set -x

	echo >>"${APTGET_CHROOT_DIR}/etc/netplan/network-manage-all.yaml" "network:"
	echo >>"${APTGET_CHROOT_DIR}/etc/netplan/network-manage-all.yaml" "  version: 2"
	echo >>"${APTGET_CHROOT_DIR}/etc/netplan/network-manage-all.yaml" "  renderer: NetworkManager"

	set +x
}

fakeroot do_save_graphics() {
	set -x

	if [ -e "${IMAGE_ROOTFS}/usr/bin/Xwayland" ]; then
		# backup graphics components
		mv ${IMAGE_ROOTFS}/usr/bin/Xwayland ${IMAGE_ROOTFS}/usr/bin/Xwayland_imx
	fi

	set +x
}

do_enable_graphics() {
	set -x

	if [ -e "${IMAGE_ROOTFS}/usr/bin/Xwayland" ]; then
	# set egl/gles2 for gnome/mutter compositor
#	echo >>"${IMAGE_ROOTFS}/etc/environment" "COGL_RENDERER=egl_wayland"
		echo >>"${IMAGE_ROOTFS}/etc/environment" "COGL_DRIVER=gles2"
#	echo >>"${IMAGE_ROOTFS}/etc/environment" "CLUTTER_BACKEND=wayland"
		echo >>"${IMAGE_ROOTFS}/etc/environment" "CLUTTER_DRIVER=gles2"
		# Warning: Ignoring XDG_SESSION_TYPE=wayland on Gnome. Use QT_QPA_PLATFORM=waylandd
		# to run on Wayland anyway.
		echo >>"${IMAGE_ROOTFS}/etc/environment" "QT_QPA_PLATFORM=wayland"

		# video playback use gtkglsink as default sink, need chose GDK_GL = gles
		# to use gles backend in gtk
		echo >>"${IMAGE_ROOTFS}/etc/environment" "GDK_GL=gles"

		# disable xsession startup
		rm -f ${IMAGE_ROOTFS}/usr/share/xsessions/*

		cp -f ${IMAGE_ROOTFS}/usr/bin/Xwayland_imx ${IMAGE_ROOTFS}/usr/bin/Xwayland

		rm -f ${IMAGE_ROOTFS}/usr/lib/systemd/system/default.target
		ln graphical.target -s ${IMAGE_ROOTFS}/usr/lib/systemd/system/default.target

		rm -f ${IMAGE_ROOTFS}/etc/systemd/system/default.target
		ln /usr/lib/systemd/system/graphical.target -s ${IMAGE_ROOTFS}/etc/systemd/system/default.target
	fi

	set +x
}

do_cleanup_rootfs() {
	set -x
	# remove apt-get source list, apt-get update can download them
	rm -rf ${IMAGE_ROOTFS}/var/lib/apt/lists/*
	# remove apt-get package cache, apt-get update can create them
	rm -rf ${IMAGE_ROOTFS}/var/cache/apt/*.bin
	set +x
}

# We need to add Yocto libraries to LD path and remove conflicting libraries
fakeroot do_fix_ldconfig() {
	# Ld config misses /usr/lib path
	set -x
	echo >>"${APTGET_CHROOT_DIR}/etc/ld.so.conf.d/01-yocto.conf" "/usr/lib"
	set +x
}

fakeroot do_config_systemd_resolved() {
	set -x
	rm -f ${IMAGE_ROOTFS}/etc/resolv.conf
	ln /usr/lib/systemd/resolv.conf -s ${IMAGE_ROOTFS}/etc/resolv.conf
	set +x
}

python do_rootfs:prepend() {
    d.setVar('IMAGE_LOG_CHECK_EXCLUDES', 'Failed')
}

compare_rootfs_with_debian_base() {
	DEBIAN_BASE_DIR="${DEPLOY_DIR_IMAGE}/debian-base"

	IMG_NAME="${PN}-${PV}-${PR}-${MACHINE}"
	OUTPUT_DIR="${DEPLOY_DIR_IMAGE}/${IMG_NAME}"

	if [ -d "${DEBIAN_BASE_DIR}" ]; then
		# Ensure the output directory exists
		install -d ${OUTPUT_DIR}

		# List files in the current rootfs and the debian-base
		find ${IMAGE_ROOTFS} -type f | sort | sed "s|${IMAGE_ROOTFS}||" > ${OUTPUT_DIR}/${IMG_NAME}_file_list.txt
		find ${DEBIAN_BASE_DIR} -type f | sort | sed "s|${DEBIAN_BASE_DIR}||" > ${OUTPUT_DIR}/debian_base_file_list.txt

		# Compare the lists and output the files unique to the current rootfs
		comm -23 ${OUTPUT_DIR}/${IMG_NAME}_file_list.txt ${OUTPUT_DIR}/debian_base_file_list.txt > ${OUTPUT_DIR}/${IMG_NAME}_unique_files.txt

		# Compare the lists and output the files unique to the debian-base
		comm -13 ${OUTPUT_DIR}/${IMG_NAME}_file_list.txt ${OUTPUT_DIR}/debian_base_file_list.txt > ${OUTPUT_DIR}/debian_base_unique_files.txt
	fi
}

do_rootfs[network] = "1"

COMPATIBLE_MACHINE = "(.*debian)"

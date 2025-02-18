#!/bin/sh

if [ $# -lt 1 ]; then
	exit 0;
fi

get_current_root_device()
{
	for i in $(cat /proc/cmdline); do
		case "$i" in
			root=*) CURRENT_ROOT="${i#root=}" ;;
		esac
	done
}

get_update_part()
{
	CURRENT_PART=$(printf "%s" "$CURRENT_ROOT" | tail -c 1)
	if [ $CURRENT_PART = "1" ]; then
		UPDATE_PART="2";
	else
		UPDATE_PART="1";
	fi
}

get_update_device()
{
	UPDATE_ROOT=${CURRENT_ROOT%?}${UPDATE_PART}
}

format_update_device()
{
	umount $UPDATE_ROOT
	mkfs.ext4 $UPDATE_ROOT -F -L rootfs${UPDATE_PART} -q
}

if [ "$1" = "preinst" ]; then
	# get the current root device
	get_current_root_device

	# get the device to be updated
	get_update_part
	get_update_device

	# format the device to be updated
	format_update_device

	# create a symlink for the update process
	ln -sf $UPDATE_ROOT /dev/update
fi

if [ "$1" = "postinst" ]; then
	get_current_root_device

	# set TMPDIR to /tmp if it is not already set
	TMPDIR=${TMPDIR:=/tmp}

	# Create the mount point if it doesn't exist
	mkdir -p ${TMPDIR}/datadst

	if [ ! -d "/sys/kernel/debug/gpmi-nand" ]; then
		# Adjust u-boot-fw-utils for eMMC on the installed rootfs
		mount -t ext4 /dev/update ${TMPDIR}/datadst
		rm ${TMPDIR}/datadst/etc/u-boot-initial-env-*nand*
		ln -sf u-boot-initial-env-sd ${TMPDIR}/datadst/etc/u-boot-initial-env
		sed -i "/mtd/ s/^#*/#/" ${TMPDIR}/datadst/etc/fw_env.config
		CURRENT_BLK_DEV="${CURRENT_ROOT%p?}"
		sed -i "s/^#*\/dev\/mmcblk./$(echo ${CURRENT_BLK_DEV} | sed 's_/_\\/_g')/" ${TMPDIR}/datadst/etc/fw_env.config
		umount /dev/update
	fi

	get_update_part

	fw_setenv mmcbootpart $UPDATE_PART
	fw_setenv mmcrootpart $UPDATE_PART
fi

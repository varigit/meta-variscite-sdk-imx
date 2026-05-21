#!/bin/bash
TGT_FILENAME=/opt/images/Yocto/rootfs.tar.zst
TGT_ROOTFS=$(readlink "$TGT_FILENAME")
if [ $? -ne 0 ]; then
	echo "WARNING: $TGT_FILENAME was expected to be a symlink, but it is not. Using single partitioning for installation"
	EXTRA_PARAM=""
else
	if [[ "$TGT_ROOTFS" == var-image-swupdate* ]]; then
		echo "Target file system has swupdate. Using dual partitioning"
		EXTRA_PARAM="-u"
	else
		echo "Using single partitioning for"
		EXTRA_PARAM=""
	fi
fi

/usr/sbin/install_yocto.sh "$EXTRA_PARAM" -r emmc

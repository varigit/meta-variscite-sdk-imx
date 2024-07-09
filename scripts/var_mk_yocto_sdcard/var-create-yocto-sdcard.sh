#!/bin/bash
set -e

#### Script version ####
SCRIPT_NAME=${0##*/}
readonly SCRIPT_VERSION="0.7"

#### Exports Variables ####
#### global variables ####
readonly ABSOLUTE_FILENAME=`readlink -e "$0"`
readonly ABSOLUTE_DIRECTORY=`dirname ${ABSOLUTE_FILENAME}`
readonly SCRIPT_POINT=${ABSOLUTE_DIRECTORY}

readonly YOCTO_ROOT="${SCRIPT_POINT}/../../../.."

readonly TARBALL_BASENAME="rootfs"

# Support zst format
readonly TARBALL_FMT="tar.zst"
readonly TAR_FLAGS="--zstd -xf"

# Verify MACHINE environment variable is set
if [[ -z "${MACHINE}" ]]; then
	MACHINE='${MACHINE}'
fi

if [[ -e ${YOCTO_ROOT}/sources/meta-boot2qt ]] ; then
	readonly BSP_TYPE="B2QT"
	readonly YOCTO_BUILD=${YOCTO_ROOT}/build-${MACHINE}
	readonly YOCTO_DEFAULT_IMAGE=b2qt-embedded-qt6-image
else
	readonly BSP_TYPE="YOCTO"
	if [[ $MACHINE = "imx6ul-var-dart" || $MACHINE = "imx7-var-som" ]]; then
		readonly YOCTO_BUILD=${YOCTO_ROOT}/build_x11
	elif [[ -d "${YOCTO_ROOT}/build_xwayland" ]]; then
		readonly YOCTO_BUILD=${YOCTO_ROOT}/build_xwayland
	elif [[ -d "${YOCTO_ROOT}/build_wayland" ]]; then
		readonly YOCTO_BUILD=${YOCTO_ROOT}/build_wayland
	elif [[ -d "${YOCTO_ROOT}/build-${MACHINE}" ]]; then
		readonly YOCTO_BUILD=${YOCTO_ROOT}/build-${MACHINE}
	else
		echo "Unable to find directory to set YOCTO_BUILD to, exiting"
		exit 1
	fi
	readonly YOCTO_DEFAULT_IMAGE=fsl-image-gui
fi
echo "BSP type: ${BSP_TYPE}"

readonly YOCTO_SCRIPTS_PATH=${SCRIPT_POINT}/variscite_scripts
readonly YOCTO_IMGS_PATH=${YOCTO_BUILD}/tmp/deploy/images/${MACHINE}

# Sizes are in MiB
BOOTLOAD_RESERVE_SIZE=8
BOOT_ROM_SIZE=0
DEFAULT_ROOTFS_SIZE=7400
BOOTLOADER_OFFSET=33
HAS_UBI_IMAGES=0
HAS_DESKTOP_ICONS=0

AUTO_FILL_SD=0
SPARE_SIZE=4
LOOP_MAJOR=7

# This function performs sanity check to verify  that the target device is removable devise of proper size
function check_device()
{
	# Check that parameter is a valid block device
	if [ ! -b "$1" ]; then
		echo "$1 is not a valid block device, exiting"
		exit 1
	fi

	local dev=$(basename $1)

	# Check that /sys/block/$dev exists
	if [ ! -d /sys/block/$dev ]; then
		echo "Directory /sys/block/${dev} missing, exiting"
		exit 1
	fi

	# Get device parameters
	local removable=$(cat /sys/block/${dev}/removable)
	local size_bytes=$((512*$(cat /sys/class/block/${dev}/size)))
	local size_gb=$((size_bytes/1000000000))

	# Non-removable SD card readers require additional check
	if [ "${removable}" != "1" ]; then
		local drive=$(udisksctl info -b /dev/${dev}|grep "Drive:"|cut -d"'" -f 2)
		local mediaremovable=$(gdbus call --system --dest org.freedesktop.UDisks2 \
				--object-path ${drive} --method org.freedesktop.DBus.Properties.Get \
				org.freedesktop.UDisks2.Drive MediaRemovable)
		if [[ "${mediaremovable}" = *"true"* ]]; then
			removable=1
		fi
	fi

	# Check that device is either removable or loop
	if [ "$removable" != "1" -a $(stat -c '%t' /dev/$dev) != ${LOOP_MAJOR} ]; then
		echo "$1 is not a removable device, exiting"
		exit 1
	fi

	# Check that device is attached
	if [ ${size_bytes} -eq 0 ]; then
		echo "$1 is not attached, exiting"
		exit 1
	fi

	# Check that device has a valid size
	echo "Detected removable device $1, size=${size_gb}GB"
}

# Default SD card image
YOCTO_DEFAULT_IMAGE_PATH=${YOCTO_IMGS_PATH}
YOCTO_DEFAULT_IMAGE_BASE_IN_NAME=${YOCTO_DEFAULT_IMAGE}-${MACHINE}.rootfs

# Default eMMC image
YOCTO_RECOVERY_ROOTFS_PATH=${YOCTO_IMGS_PATH}
YOCTO_RECOVERY_ROOTFS_BASE_IN_NAME=${YOCTO_DEFAULT_IMAGE}-${MACHINE}

echo "================================================"
echo "=  Variscite recovery SD card creation script  ="
echo "================================================"

help() {
	bn=`basename $0`
	echo " Usage: MACHINE=<var-som-mx6|imx6ul-var-dart|imx7-var-som|imx8mq-var-dart|imx8mm-var-dart|imx8qxp-var-som|imx8qxpb0-var-som|imx8qm-var-som|imx8mn-var-som|imx8mp-var-dart|imx93-var-som|imx95-var-dart> $bn <options> device_node"
	echo
	echo " options:"
	echo " -h                display this Help message"
	echo " -s                only Show partition sizes to be written, without actually write them"
	echo " -a                Automatically set the rootfs partition size to fill the SD card (leaving spare ${SPARE_SIZE}MiB)"
	echo " -d DEFAULT_IMAGE  select an alternative Rootfs for the SD card"
	echo "                    - Default: \"${YOCTO_DEFAULT_IMAGE_PATH}/${YOCTO_DEFAULT_IMAGE}-${MACHINE}\""
	echo "                    - WARNING: DEFAULT_IMAGE must include all dependencies for install_yocto.sh"
	echo "                               It is recommended to use the default value, fsl-image-gui, which"
	echo "                               is tested by Variscite"
	echo " -r ROOTFS_NAME    select an alternative Rootfs for recovery images"
	echo "                    - Default: If unset, the value of DEFAULT_IMAGE is used."
	echo "                    - <ROOTFS_NAME>.${TARBALL_FMT} is added to SDCARD_IMAGE/opt/images/Yocto/${TARBALL_BASENAME}.${TARBALL_FMT}"
	echo "                      and can be installed to eMMC using install_yocto.sh"
	echo
}

function handle_file_missing()
{
	echo "Error: \"${1}\" not found"
	help
	exit 1
}

if [[ $EUID -ne 0 ]] ; then
	echo "This script must be run with super-user privileges"
	exit 1
fi

case $MACHINE in
	"imx8mm-var-dart" | \
	"imx8mn-var-som" | \
	"imx8mq-var-dart" | \
	"imx8mp-var-dart" | \
	"imx8qm-var-som" | \
	"imx8qxp-var-som" | \
	"imx8qxpb0-var-som" | \
	"imx93-var-som" | \
	"imx95-var-dart")
		IMXBOOT_TARGET=imx-boot
		;;
	"imx6ul-var-dart")
		FAT_VOLNAME=BOOT-VAR6UL
		SD_BLOCK_DEV=mmcblk0
		;;
	"imx7-var-som")
		FAT_VOLNAME=BOOT-VARMX7
		SD_BLOCK_DEV=mmcblk0
		;;
	"var-som-mx6")
		FAT_VOLNAME=BOOT-VARMX6
		SD_BLOCK_DEV=mmcblk1
		;;
	*)
		help
		exit 1
esac

if [[ $MACHINE = "var-som-mx6" || $MACHINE = "imx6ul-var-dart" || $MACHINE = "imx7-var-som" ]]; then
	BOOTLOAD_RESERVE_SIZE=4
	BOOT_ROM_SIZE=12
	HAS_UBI_IMAGES=1
fi

if [[ $MACHINE = "imx6ul-var-dart" || $MACHINE = "imx7-var-som" ]]; then
	HAS_DESKTOP_ICONS=1
fi

if [[ $MACHINE = "imx8qxp-var-som" || $MACHINE = "imx8qxpb0-var-som" || \
	  $MACHINE = "imx8qm-var-som" || $MACHINE = "imx8mn-var-som" || \
	  $MACHINE = "imx8mp-var-dart" ]] || [[ $MACHINE = "imx93-var-som" || \
	  $MACHINE = "imx95-var-dart" ]]; then

	BOOTLOADER_OFFSET=32
fi

TEMP_DIR=./var_tmp
P1_MOUNT_DIR=${TEMP_DIR}/${FAT_VOLNAME}
P2_MOUNT_DIR=${TEMP_DIR}/rootfs


# Parse command line
moreoptions=1
node="na"
cal_only=0

while [ "$moreoptions" = 1 -a $# -gt 0 ]; do
	case $1 in
		-h) help; exit 3 ;;
		-s) cal_only=1 ;;
		-a) AUTO_FILL_SD=1 ;;
		-d) shift;
			YOCTO_DEFAULT_IMAGE_MASK_PATH=`readlink -e "${1}.${TARBALL_FMT}"` || handle_file_missing "${1}.${TARBALL_FMT}";
			YOCTO_DEFAULT_IMAGE_PATH=`dirname ${YOCTO_DEFAULT_IMAGE_MASK_PATH}`
			YOCTO_DEFAULT_IMAGE_BASE_IN_NAME=`basename ${1}`
			# If YOCTO_RECOVERY_ROOTFS_MASK_PATH unset, copy the
			# default image path and name to recovery image path and name
			if [[ -z "${YOCTO_RECOVERY_ROOTFS_MASK_PATH}" ]]; then
				YOCTO_RECOVERY_ROOTFS_PATH=${YOCTO_DEFAULT_IMAGE_PATH}
				YOCTO_RECOVERY_ROOTFS_BASE_IN_NAME=${YOCTO_DEFAULT_IMAGE_BASE_IN_NAME}
			fi
		;;
		-r) shift;
			YOCTO_RECOVERY_ROOTFS_MASK_PATH=`readlink -e "${1}.${TARBALL_FMT}"` || handle_file_missing "${1}.${TARBALL_FMT}";
			YOCTO_RECOVERY_ROOTFS_PATH=`dirname ${YOCTO_RECOVERY_ROOTFS_MASK_PATH}`
			YOCTO_RECOVERY_ROOTFS_BASE_IN_NAME=`basename ${1}`
		;;
		*)  moreoptions=0; node=$1 ;;
	esac
	[ "$moreoptions" = 0 ] && [ $# -gt 1 ] && help && exit 1
	[ "$moreoptions" = 1 ] && shift
done
part=""
if [[ $node == *mmcblk* ]] || [[ $node == *loop* ]] ; then
	part="p"
fi

# allow only removable/loopback devices, to protect host PC
echo "MACHINE=${MACHINE}"
echo "SD card rootfs:  ${YOCTO_DEFAULT_IMAGE_BASE_IN_NAME}"
echo "Recovery rootfs: ${YOCTO_RECOVERY_ROOTFS_BASE_IN_NAME}"
echo "================================================"
check_device $node
echo "================================================"
read -p "Press Enter to continue"

# Call sfdisk to get total card size
if [ "${AUTO_FILL_SD}" -eq "1" ]; then
	TOTAL_SIZE=`sfdisk -s ${node}`
	TOTAL_SIZE=`expr ${TOTAL_SIZE} / 1024`
	ROOTFS_SIZE=`expr ${TOTAL_SIZE} - ${BOOTLOAD_RESERVE_SIZE} - ${BOOT_ROM_SIZE} - ${SPARE_SIZE}`
else
	ROOTFS_SIZE=${DEFAULT_ROOTFS_SIZE}
fi

if [ "${cal_only}" -eq "1" ]; then
cat << EOF
BOOTLOADER (No Partition) : ${BOOTLOAD_RESERVE_SIZE}MiB
BOOT                      : ${BOOT_ROM_SIZE}MiB
ROOT-FS                   : ${ROOTFS_SIZE}MiB
EOF
exit 3
fi


function delete_device
{
	echo
	echo "Deleting current partitions"
	for ((i=0; i<=10; i++))
	do
		if [[ -e ${node}${part}${i} ]] ; then
			dd if=/dev/zero of=${node}${part}${i} bs=512 count=1024 2> /dev/null || true
		fi
	done
	sync

	((echo d; echo 1; echo d; echo 2; echo d; echo 3; echo d; echo w) | fdisk $node &> /dev/null) || true
	sync

	dd if=/dev/zero of=$node bs=1M count=${BOOTLOAD_RESERVE_SIZE}
	sync; sleep 1
}

function ceildiv
{
	local num=$1
	local div=$2
	echo $(( (num + div - 1) / div ))
}

function create_parts
{
	echo
	echo "Creating new partitions"
	BLOCK=`echo ${node} | cut -d "/" -f 3`
	SECT_SIZE_BYTES=`cat /sys/block/${BLOCK}/queue/physical_block_size`

	BOOTLOAD_RESERVE_SIZE_BYTES=$((BOOTLOAD_RESERVE_SIZE * 1024 * 1024))
	ROOTFS_SIZE_BYTES=$((ROOTFS_SIZE * 1024 * 1024))

	PART1_START=`ceildiv ${BOOTLOAD_RESERVE_SIZE_BYTES} ${SECT_SIZE_BYTES}`

	if [ ${BOOT_ROM_SIZE} = 0 ]; then
		PART1_SIZE=`ceildiv ${ROOTFS_SIZE_BYTES} ${SECT_SIZE_BYTES}`

sfdisk --force -uS ${node} &> /dev/null << EOF
${PART1_START},${PART1_SIZE},83
EOF

	else
		BOOT_ROM_SIZE_BYTES=$((BOOT_ROM_SIZE * 1024 * 1024))
		PART1_SIZE=`ceildiv ${BOOT_ROM_SIZE_BYTES} ${SECT_SIZE_BYTES}`
		PART2_START=$((PART1_START + PART1_SIZE))
		PART2_SIZE=$((ROOTFS_SIZE_BYTES / SECT_SIZE_BYTES))

sfdisk --force -uS ${node} &> /dev/null << EOF
${PART1_START},${PART1_SIZE},c
${PART2_START},${PART2_SIZE},83
EOF

	fi

	sync; sleep 1
	fdisk -l $node
}

function format_parts
{
	echo
	echo "Formatting partitions"
	if [ ${BOOT_ROM_SIZE} = 0 ]; then
		mkfs.ext4 ${node}${part}1 -L rootfs
	else
		mkfs.vfat ${node}${part}1 -n ${FAT_VOLNAME}
		mkfs.ext4 ${node}${part}2 -L rootfs
	fi
	sync; sleep 1
}

function install_bootloader
{
	echo
	echo "Installing U-Boot"
	if [ ${BOOT_ROM_SIZE} = 0 ]; then
		dd if=${YOCTO_IMGS_PATH}/${IMXBOOT_TARGET} of=${node} bs=1K seek=${BOOTLOADER_OFFSET}; sync
	else
		dd if=${YOCTO_IMGS_PATH}/SPL-sd of=${node} bs=1K seek=1; sync
		dd if=${YOCTO_IMGS_PATH}/u-boot.img-sd of=${node} bs=1K seek=69; sync
	fi
}

function mount_parts
{
	mkdir -p ${P1_MOUNT_DIR}
	mkdir -p ${P2_MOUNT_DIR}
	sync
	if [ ${BOOT_ROM_SIZE} = 0 ]; then
		mount ${node}${part}1  ${P2_MOUNT_DIR}
	else
		mount ${node}${part}1  ${P1_MOUNT_DIR}
		mount ${node}${part}2  ${P2_MOUNT_DIR}
	fi
}

function unmount_parts
{
	if [ ${BOOT_ROM_SIZE} != 0 ]; then
		umount ${P1_MOUNT_DIR}
	fi
	umount ${P2_MOUNT_DIR}
	rm -rf ${TEMP_DIR}
}

function install_yocto
{
	if [ ${BOOT_ROM_SIZE} != 0 ]; then
		echo
		echo "Installing Yocto Boot partition"
		for f in ${YOCTO_IMGS_PATH}/*.dtb; do
			if [[ -L $f ]] && [[ $f != *${MACHINE}.dtb ]]; then
				cp $f	${P1_MOUNT_DIR}/
			fi
		done

		pv ${YOCTO_IMGS_PATH}/?Image >	${P1_MOUNT_DIR}/`cd ${YOCTO_IMGS_PATH}; ls ?Image`
		sync
	fi

	echo
	echo "Installing Yocto Root File System"
	pv ${YOCTO_DEFAULT_IMAGE_PATH}/${YOCTO_DEFAULT_IMAGE_BASE_IN_NAME}.${TARBALL_FMT} | tar ${TAR_FLAGS} - -C ${P2_MOUNT_DIR}/
	sync
}

function set_fw_env_config_to_sd
{
	sed -i "/mtd/ s/^#*/#/" ${P2_MOUNT_DIR}/etc/fw_env.config
	sed -i "s/#*\/dev\/mmcblk./\/dev\/${SD_BLOCK_DEV}/" ${P2_MOUNT_DIR}/etc/fw_env.config
}

function set_fw_utils_to_sd_on_sd_card
{
	# Adjust u-boot-fw-utils for SD on the SD card
	if [[ `readlink ${P2_MOUNT_DIR}/etc/u-boot-initial-env` != "u-boot-initial-env-sd" ]]; then
		ln -sf u-boot-initial-env-sd ${P2_MOUNT_DIR}/etc/u-boot-initial-env
	fi

	if [[ -f ${P2_MOUNT_DIR}/etc/fw_env.config ]]; then
		set_fw_env_config_to_sd
	fi
}

function copy_images
{
	echo
	echo "Copying Yocto images to /opt/images/"
	mkdir -p ${P2_MOUNT_DIR}/opt/images/Yocto

	if [ ${BOOT_ROM_SIZE} != 0 ]; then
		for f in ${YOCTO_RECOVERY_ROOTFS_PATH}/*.dtb; do
			if [[ -L $f ]] && [[ $f != *${MACHINE}.dtb ]]; then
				cp $f	${P2_MOUNT_DIR}/opt/images/Yocto/
			fi
		done

		cp ${YOCTO_RECOVERY_ROOTFS_PATH}/?Image		${P2_MOUNT_DIR}/opt/images/Yocto/
	fi

	# Copy image for eMMC
	if [ -f ${YOCTO_RECOVERY_ROOTFS_PATH}/${YOCTO_RECOVERY_ROOTFS_BASE_IN_NAME}.${TARBALL_FMT} ]; then
		pv ${YOCTO_RECOVERY_ROOTFS_PATH}/${YOCTO_RECOVERY_ROOTFS_BASE_IN_NAME}.${TARBALL_FMT} > ${P2_MOUNT_DIR}/opt/images/Yocto/${TARBALL_BASENAME}.${TARBALL_FMT}
	else
		echo "${TARBALL_BASENAME}.${TARBALL_FMT} file is not present. Installation on \"eMMC\" will not be supported."
	fi

	if [ ${HAS_UBI_IMAGES} = 1 ]; then
		# Configure uboot-fw-utils for SD
		set_fw_utils_to_sd_on_sd_card
		# Copy images for NAND flash
		for f in ${YOCTO_RECOVERY_ROOTFS_PATH}/${YOCTO_RECOVERY_ROOTFS_BASE_IN_NAME}_*.ubi; do
			if [ -f "$f" ]; then
				pv $f > ${P2_MOUNT_DIR}/opt/images/Yocto/`basename $f`
			fi
		done
		if ls ${P2_MOUNT_DIR}/opt/images/Yocto/*.ubi &> /dev/null; then
			STR=$YOCTO_RECOVERY_ROOTFS_BASE_IN_NAME rename 's/\Q$ENV{STR}\E/rootfs/' ${P2_MOUNT_DIR}/opt/images/Yocto/*.ubi
		else
			echo "UBI rootfs images are not present. Installation on \"NAND flash\" will not be supported."
		fi
	fi

	if [ ${BOOT_ROM_SIZE} = 0 ]; then
		if [ ${MACHINE} = "imx8mq-var-dart" ]; then
			cp ${YOCTO_RECOVERY_ROOTFS_PATH}/imx-boot-${MACHINE}-sd.bin-* ${P2_MOUNT_DIR}/opt/images/Yocto
			(cd ${P2_MOUNT_DIR}/opt/images/Yocto; ln -fs imx-boot-${MACHINE}-sd.bin-flash_evk imx-boot-sd.bin)
		else
			cp ${YOCTO_RECOVERY_ROOTFS_PATH}/${IMXBOOT_TARGET} ${P2_MOUNT_DIR}/opt/images/Yocto/imx-boot-sd.bin
		fi
	else
		cp ${YOCTO_RECOVERY_ROOTFS_PATH}/SPL-nand		${P2_MOUNT_DIR}/opt/images/Yocto/
		cp ${YOCTO_RECOVERY_ROOTFS_PATH}/SPL-sd			${P2_MOUNT_DIR}/opt/images/Yocto/
		cp ${YOCTO_RECOVERY_ROOTFS_PATH}/u-boot.img-nand	${P2_MOUNT_DIR}/opt/images/Yocto/
		cp ${YOCTO_RECOVERY_ROOTFS_PATH}/u-boot.img-sd		${P2_MOUNT_DIR}/opt/images/Yocto/
	fi
}

function copy_scripts
{
	echo
	echo "Copying scripts"

	cp ${YOCTO_SCRIPTS_PATH}/echos.sh 		${P2_MOUNT_DIR}/usr/bin/

	if [[ $MACHINE = "imx6ul-var-dart" || $MACHINE = "imx7-var-som" ]]; then
		cp ${YOCTO_SCRIPTS_PATH}/mx6ul_mx7_install_yocto.sh	${P2_MOUNT_DIR}/usr/bin/install_yocto.sh
	elif [[ $MACHINE = "var-som-mx6" ]]; then
		cp ${YOCTO_SCRIPTS_PATH}/mx6_install_yocto.sh		${P2_MOUNT_DIR}/usr/bin/install_yocto.sh
		cp ${YOCTO_SCRIPTS_PATH}/mx6_install_yocto_emmc.sh	${P2_MOUNT_DIR}/usr/bin/install_yocto_emmc.sh
	else
		cp ${YOCTO_SCRIPTS_PATH}/mx8_install_yocto.sh	${P2_MOUNT_DIR}/usr/bin/install_yocto.sh
	fi

	if [ ${HAS_DESKTOP_ICONS} = 1 ]; then
		echo
		echo "Copying desktop icons"
		if [ -d ${P2_MOUNT_DIR}/usr/share/applications ]; then
			cp ${YOCTO_SCRIPTS_PATH}/${MACHINE}*.desktop	${P2_MOUNT_DIR}/usr/share/applications/

			# Remove inactive icons
			if [ ! -f ${P2_MOUNT_DIR}/opt/images/Yocto/${TARBALL_BASENAME}.${TARBALL_FMT} ]; then
				rm -rf ${P2_MOUNT_DIR}/usr/share/applications/${MACHINE}*yocto*emmc*.desktop
			fi

			if ! ls ${P2_MOUNT_DIR}/opt/images/Yocto/rootfs*.ubi &> /dev/null; then
				rm -rf ${P2_MOUNT_DIR}/usr/share/applications/${MACHINE}*yocto*nand*.desktop
			fi

			if [[ ${YOCTO_RECOVERY_ROOTFS_BASE_IN_NAME} == var-image-swupdate* ]]; then
				sed -i 's/install_yocto.sh/& -u/' ${P2_MOUNT_DIR}/usr/share/applications/${MACHINE}*yocto*.desktop
			fi
		fi
	fi
}

umount ${node}${part}*  2> /dev/null || true

delete_device
create_parts
format_parts

mount_parts
install_yocto
copy_images
copy_scripts

echo
echo "Syncing"
sync | pv -t

unmount_parts

install_bootloader

echo
echo "Done"

exit 0

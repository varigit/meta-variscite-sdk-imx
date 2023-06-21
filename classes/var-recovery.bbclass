# Copyright (C) 2023 Variscite
#
# Variscite Recovery Class
#
# This class provides an interface to create a standard Variscite recovery
# image. A recovery image is a bootable SD Linux image that is then used to
# program a target image to eMMC using Variscite platform specific scripts
# provided by var-install-yocto.bb.
#
# Usage
# ======
# Since this class contains a require statement on VAR_RECOVERY_SD_IMAGE, then
# its inheritance should occur following setting this variable (see variable
# descriptions below):
#
# i.e.
# VAR_RECOVERY_SD_IMAGE = "..."
# inherit var-recovery
#
# Mandatory Variables
# ====================
# VAR_RECOVERY_SD_IMAGE: Set to base <image>.bb path which represents the
#     image/rootfs used to create the running recovery SD card image.
#
# VAR_RECOVERY_TARGET_ROOTFS: Set to <image-name> to set the target image which
#     will be installed within the rootfs of the recovery SD card image (under
#     VAR_RECOVERY_IMAGES_TARGET_PATH) and ultimately be programmed to the
#     eMMC. This variable can also be overridden by passing as an environmental
#     variable with the same name from the original environment.
#
# Optional Variables & Flags
# ===========================
# VAR_RECOVERY_DEPENDS: Space separated list of packages on which the recovery
#     image creation depends. Internally, VAR_RECOVERY_TARGET_ROOTFS is always
#     on this list.
#
# VAR_RECOVERY_IMAGES: Space separated list of deployed image files that should
#     be installed in the recovery SD card image (files produced by
#     VAR_RECOVERY_DEPENDS recipes). Internally, the VAR_RECOVERY_TARGET_ROOTFS
#     image file is always on this list and a sym link
#     rootfs.<VAR_RECOVERY_TARGET_ROOTFS_TYPE> pointing to this image is
#     installed as well.
#
# VAR_RECOVERY_IMAGE_RENAME[<VAR_RECOVERY_IMAGE>]: Flag which allows for
#     install renaming of a specific VAR_RECOVERY_IMAGES file.
#
# VAR_RECOVERY_IMAGE_SUBDIR[<VAR_RECOVERY_IMAGE>]: Flag which allows for
#     subdirectory install of a specific VAR_RECOVERY_IMAGES file from the
#     VAR_RECOVERY_IMAGES_TARGET_PATH base.
#
# VAR_RECOVERY_IMAGES_TARGET_PATH: Default path in the rootfs of the recovery
#     SD card image where target image artifacts are installed. This path will
#     be dependent on the platform install_yocto.sh script, but if unset
#     defaults to "/opt/images/Yocto."
#
# VAR_RECOVERY_IMAGE_ROOTFS_MAXSIZE: Size in KB for the approximate maximum
#      size of the recovery image rootfs. If unset, this defaults to 7,598,080
#      (this default value was chosen to target SD cards of size 8 GB).
#
# VAR_RECOVERY_TARGET_ROOTFS_TYPE: Target compressed rootfs tarball format
#     which should match at least one of the formats specified in IMAGE_FSTYPES
#     of the VAR_RECOVERY_TARGET_ROOTFS image.
#

require ${VAR_RECOVERY_SD_IMAGE}

VAR_RECOVERY_IMAGES_TARGET_PATH ?= "/opt/images/Yocto/"

VAR_RECOVERY_TARGET_ROOTFS_TYPE ?= "tar.zst"

ROOTFS_WORKDIR = "${WORKDIR}/rootfs/${VAR_RECOVERY_IMAGES_TARGET_PATH}/"

# Recovery image filename - defaults to Yocto standard of image class.
VAR_RECOVERY_SD_NAME ?= "${IMAGE_BASENAME}-${MACHINE}"
IMAGE_LINK_NAME = "${VAR_RECOVERY_SD_NAME}"

# The actual image will be slightly smaller than VAR_RECOVERY_IMAGE_ROOTFS_MAXSIZE
# after adjustment by wks DEFAULT_EXTRA_SPACE and DEFAULT_OVERHEAD_FACTOR variables.
VAR_RECOVERY_IMAGE_ROOTFS_MAXSIZE ?= "${@eval('7420*1024')}"
IMAGE_ROOTFS_MAXSIZE = "${VAR_RECOVERY_IMAGE_ROOTFS_MAXSIZE}"
IMAGE_OVERHEAD_FACTOR = "1"

# Subtract out IMAGE_ROOTFS_EXTRA_SPACE and wks DEFAULT_EXTRA_SPACE and reduce by factor of wks DEFAULT_OVERHEAD_FACTOR
IMAGE_ROOTFS_SIZE_RAW = "${@eval('int((${IMAGE_ROOTFS_MAXSIZE} / 1.3) - (${IMAGE_ROOTFS_EXTRA_SPACE}) - (1024*10))')}"

# Align the image size to 10MB so when scaled by wks DEFAULT_OVERHEAD_FACTOR
# it will remain aligned to 1MB.
IMAGE_ROOTFS_SIZE = "${@eval('${IMAGE_ROOTFS_SIZE_RAW} - (${IMAGE_ROOTFS_SIZE_RAW} % (1024*10))')}"

# Install Yocto board scripts
IMAGE_INSTALL:append = "var-install-yocto"

# Do not cache parsing ops for any recipe inheriting this class. Otherwise, Bitbake will not
# re-run our anonymous python function meaning it cannot properly read VAR_RECOVERY_TARGET_ROOTFS
# if passed as an environmental variable
BB_DONT_CACHE = "1"

python () {
    # Check if an environment variable VAR_RECOVERY_TARGET_ROOTFS from the user exists. Otherwise
    # get the Bitbake defined variable.
    var_recovery_target_rootfs = (d.getVar('BB_ORIGENV', False).getVar('VAR_RECOVERY_TARGET_ROOTFS') or
        d.getVar('VAR_RECOVERY_TARGET_ROOTFS'))

    if var_recovery_target_rootfs is None:
        bb.fatal("Please set VAR_RECOVERY_TARGET_ROOTFS")

    # Set an internal variable _RECOVERY_TARGET_ROOTFS for retrieval at task time.
    d.setVar('_RECOVERY_TARGET_ROOTFS', var_recovery_target_rootfs)

    images = (d.getVar('VAR_RECOVERY_DEPENDS', True) or "").split()

    # Target is dependent on VAR_RECOVERY_DEPENDS do_build
    depstr = ""
    for image in images:
        depstr += f" {image}:do_build"

    # and VAR_RECOVERY_TARGET_ROOTFS do_build
    depstr += f" {var_recovery_target_rootfs}:do_build"

    d.appendVarFlag('do_install_image_artifacts', 'depends', depstr)
}

do_install_image_artifacts[cleandirs] = "${ROOTFS_WORKDIR}"
do_install_image_artifacts[doc] = "Install artifacts to be installed by Yocto recovery image"
do_install_image_artifacts[vardeps] = "VAR_RECOVERY_TARGET_ROOTFS VAR_RECOVERY_TARGET_ROOTFS_TYPE \
    VAR_RECOVERY_IMAGES_TARGET_PATH VAR_RECOVERY_IMAGE_RENAME VAR_RECOVERY_IMAGE_SUBDIR"
addtask do_install_image_artifacts after do_rootfs before do_image

python do_install_image_artifacts() {
    def install_dir(dir):
        os.system(f"install -d {dir}")

    def install_image(src, dest):
        os.system(f"install -m 0644 {src} {dest}")

    def symlink(src, link):
        os.system(f"ln -s {src} {link}")

    rootfs_workdir = d.getVar("ROOTFS_WORKDIR")
    deploy_dir_image = d.getVar("DEPLOY_DIR_IMAGE")
    target_fs_type = d.getVar("VAR_RECOVERY_TARGET_ROOTFS_TYPE")

    var_recovery_target_rootfs = f'{d.getVar("_RECOVERY_TARGET_ROOTFS")}-' \
        f'{d.getVar("MACHINE")}.{target_fs_type}'

    # VAR_RECOVERY_TARGET_ROOTFS is always present in VAR_RECOVERY_IMAGES
    # TODO: Provide a way to rename in weird cases
    d.appendVar("VAR_RECOVERY_IMAGES", var_recovery_target_rootfs)

    var_recovery_images = d.getVar('VAR_RECOVERY_IMAGES').split()

    install_dir(rootfs_workdir)

    for artifact in var_recovery_images:
        rename_flag = d.getVarFlag("VAR_RECOVERY_IMAGE_RENAME", artifact)
        if rename_flag:
            dest_name = rename_flag
        else:
            dest_name = artifact

        subdir_flag = d.getVarFlag("VAR_RECOVERY_IMAGE_SUBDIR", artifact)
        if subdir_flag:
            install_dir(os.path.join(rootfs_workdir, subdir_flag))
            dest = os.path.join(rootfs_workdir, subdir_flag, dest_name)
        else:
            dest = os.path.join(rootfs_workdir, dest_name)

        src = os.path.join(deploy_dir_image, artifact)
        install_image(src, dest)

    symlink(var_recovery_target_rootfs, os.path.join(rootfs_workdir, f"rootfs.{target_fs_type}"))
}

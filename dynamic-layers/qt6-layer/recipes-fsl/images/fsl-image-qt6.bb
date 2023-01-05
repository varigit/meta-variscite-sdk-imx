DESCRIPTION = "Freescale Image - Adds Qt6"
LICENSE = "MIT"

require recipes-fsl/images/fsl-image-gui.bb

inherit populate_sdk_qt6_base

TOOLCHAIN_HOST_TASK:append = " nativesdk-packagegroup-qt6-toolchain-host"
TOOLCHAIN_TARGET_TASK:append = " packagegroup-qt6-modules"

CONFLICT_DISTRO_FEATURES = "directfb"

IMAGE_INSTALL += " \
    packagegroup-qt6-imx \
    tzdata \
    ${IMAGE_INSTALL_OPENCV} \
"

IMAGE_INSTALL_OPENCV              = ""
IMAGE_INSTALL_OPENCV:imxgpu       = "${IMAGE_INSTALL_OPENCV_PKGS}"
IMAGE_INSTALL_OPENCV:mx93-nxp-bsp = "${IMAGE_INSTALL_OPENCV_PKGS}"
IMAGE_INSTALL_OPENCV_PKGS = " \
    opencv-apps \
    opencv-samples \
    python3-opencv \
"

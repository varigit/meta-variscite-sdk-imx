DESCRIPTION = "Add packages for AI/ML build"

PACKAGE_ARCH = "${TUNE_PKGARCH}"

inherit packagegroup

ML_PKGS ?= ""
ML_PKGS:mx8-nxp-bsp = " \
    pytorch \
    tensorflow-lite \
    tensorflow-lite-vx-delegate \
    torchvision \
"
ML_PKGS:append:mx8mq-nxp-bsp = " \
    deepview-rt \
    tvm \
"
ML_PKGS:append:mx8mp-nxp-bsp = " \
    deepview-rt \
    tvm \
"
ML_PKGS:remove:mx8mm-nxp-bsp = "tensorflow-lite-vx-delegate"
ML_EXTRA_PKGS ?= ""
ML_EXTRA_PKGS:mx8-nxp-bsp = " \
    python3-opencv \
    python3-pip \
    python3-requests \
"
RDEPENDS:${PN} = " \
    ${ML_PKGS} \
    ${ML_EXTRA_PKGS} \
"

DESCRIPTION = "Add packages for AI/ML build"

PACKAGE_ARCH = "${TUNE_PKGARCH}"

inherit packagegroup

ML_PKGS ?= ""

# MX8
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

# MX9
ML_PKGS:mx9-nxp-bsp = " \
    deepview-rt-examples \
    onnxruntime-tests \
    tensorflow-lite \
    torchvision \
"
# ARM ethos-u package
ETHOS_U_PKGS = ""
ETHOS_U_PKGS:mx93-nxp-bsp = " \
    ethos-u-vela \
    ethos-u-driver-stack \
"

RDEPENDS:${PN} = " \
    ${ML_PKGS} \
    ${ML_EXTRA_PKGS} \
    ${ETHOS_U_PKGS} \
"

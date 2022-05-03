DESCRIPTION = "Add packages for AI/ML build"

inherit packagegroup

ML_PKGS ?= ""
ML_PKGS:mx8 = " \
    armnn \
    armnn-swig \
    pytorch \
    tensorflow-lite \
    torchvision \
"
ML_PKGS:append:mx8mq = " \
    deepview-rt \
    tvm \
"
ML_PKGS:append:mx8mp = " \
    deepview-rt \
    tvm \
    eiq-apps \
"
ML_PKGS:append:mx8mm = " \
    eiq-apps \
"
ML_EXTRA_PKGS ?= ""
ML_EXTRA_PKGS:mx8 = " \
    python3-opencv \
    python3-pip \
    python3-requests \
"
RDEPENDS:${PN} = " \
    ${ML_PKGS} \
    ${ML_EXTRA_PKGS} \
"

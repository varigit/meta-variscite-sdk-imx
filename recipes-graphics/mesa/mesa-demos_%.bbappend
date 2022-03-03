FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI:append:imxgpu3d = " file://do-not-build-xeglgears.patch"

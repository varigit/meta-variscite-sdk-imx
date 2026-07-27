# This BitBake append file is for the i.MX fork of libcamera,
# adds the fix to enable video streaming with resolution above 1920x1080.

FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"
SRC_URI:append = " \
	file://0001-pipeline-imx8-isi-do-not-limit-resolution-to-1920x10.patch \
"

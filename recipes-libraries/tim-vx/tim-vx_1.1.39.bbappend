# tim-vx compile fails if the opencl headers are already installed on host
EXTRA_OECMAKE += " \
	-DCMAKE_SYSROOT=${PKG_CONFIG_SYSROOT_DIR} \
"

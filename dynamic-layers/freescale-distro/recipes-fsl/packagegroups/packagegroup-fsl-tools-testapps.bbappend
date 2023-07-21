SOC_TOOLS_IMX_TESTAPPS = " \
	vlan \
	cryptodev-module \
	cryptodev-tests \
"

SOC_TOOLS_TESTAPPS = ""
SOC_TOOLS_TESTAPPS:imx-nxp-bsp = "${SOC_TOOLS_IMX_TESTAPPS}"

# Overwrite the original setting in meta-freescale-distro layer
# imx-test should be installed on all i.MX SoC
SOC_TOOLS_TEST = ""
SOC_TOOLS_TEST:imx-nxp-bsp = "imx-test"
SOC_TOOLS_TEST:imxgpu  = "imx-test imx-gpu-viv-demos"

RDEPENDS:${PN} += " \
	${SOC_TOOLS_TESTAPPS} \
	procps \
	ptpd \
	linuxptp \
	spidev-test \
	iw \
	can-utils \
	cpufrequtils \
	nano \
	ntp \
	minicom \
	coreutils \
	mmc-utils \
	udev-extraconf \
	e2fsprogs-resize2fs \
	openssl-bin \
	${@bb.utils.contains('DISTRO_FEATURES', 'x11', 'tk', '', d)} \
	${@bb.utils.contains('DISTRO_FEATURES', 'wayland', 'weston-examples', '', d)} \
	${@bb.utils.contains('DISTRO_FEATURES', 'wifi', 'hostapd', '', d)} \
"

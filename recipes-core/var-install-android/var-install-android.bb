SUMMARY = "Variscite target Android install for recovery images."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

DEPENDS = "parted-native"
RDEPENDS:${PN} = "bash zstd"

ANDROID_IMAGE_FILENAME:imx8mp-var-dart = "mx8mp__yocto-mickledore-6.1.36_2.1.0-v1.2__android-14.0.0_1.0.0-v1.0.wic"
ANDROID_IMAGE_FOLDER:imx8mp-var-dart = "DART-MX8M-PLUS"
ANDROID_IMAGE_CKSUM:imx8mp-var-dart = "4d8ac44031f09dd62f75ad2af17f0b3ad94ded5fc81404d87c3c8715a2dbcae6"

ANDROID_IMAGE_FILENAME:imx8mm-var-dart = "mx8mm__yocto-mickledore-6.1.36_2.1.0-v1.2__android-14.0.0_1.0.0-v1.0.wic"
ANDROID_IMAGE_FOLDER:imx8mm-var-dart = "DART-MX8M-MINI"
ANDROID_IMAGE_CKSUM:imx8mm-var-dart = "b8b666a61ebb0f563c96866932854f0ace96e74ba655e2bcbcca0b53f86e5d75"

ANDROID_IMAGE_FILENAME:imx8mn-var-som = "mx8mn__yocto-mickledore-6.1.36_2.1.0-v1.0__android-14.0.0_1.0.0-v1.0.wic"
ANDROID_IMAGE_FOLDER:imx8mn-var-som = "VAR-SOM-MX8M-NANO"
ANDROID_IMAGE_CKSUM:imx8mn-var-som = "f280fbf537461cb08da8eacd91bfe5a25585409757871190a95f60d0a90ad177"

ANDROID_IMAGE_FILENAME:imx8mq-var-dart = "mx8m__yocto-kirkstone-5.15-2.0.x-v1.5__android-13.0.0_1.2.0-v1.2.img"
ANDROID_IMAGE_FOLDER:imx8mq-var-dart = "DART-MX8M"
ANDROID_IMAGE_CKSUM:imx8mq-var-dart = "bee93380e2c9da64fda0be78702f77e444bed9091bd56e6c1cefa24321986968"

ANDROID_IMAGE_FILENAME:imx8qxp-var-som = "mx8x__yocto-mickledore-6.1.36_2.1.0-v1.3__android-13.0.0_1.2.0-v1.0.wic"
ANDROID_IMAGE_FOLDER:imx8qxp-var-som = "VAR-SOM-MX8X"
ANDROID_IMAGE_CKSUM:imx8qxp-var-som = "9b2ac41f1a7595378cadb312bc5decc4a3b9ebb37085f35f41493b930663a6dc"

ANDROID_IMAGE_FILENAME:imx8qm-var-som = "mx8__yocto-mickledore-6.1.22-2.0.0-v1.0__android-13.0.0_1.2.0-v1.0.wic"
ANDROID_IMAGE_FOLDER:imx8qm-var-som = "VAR-SOM-MX8"
ANDROID_IMAGE_CKSUM:imx8qm-var-som = "22b0b98a7d4fdd1cca9d5a8a6ad674b90dd6e40df9526cdc29c82643ca1ea540"

SRC_URI = "https://variscite-public.nyc3.cdn.digitaloceanspaces.com/${ANDROID_IMAGE_FOLDER}/Software/${ANDROID_IMAGE_FILENAME}.zst;sha256sum=${ANDROID_IMAGE_CKSUM}"
# Machines still with .gz image
SRC_URI:imx8mq-var-dart = "https://variscite-public.nyc3.cdn.digitaloceanspaces.com/${ANDROID_IMAGE_FOLDER}/Software/${ANDROID_IMAGE_FILENAME}.gz;sha256sum=${ANDROID_IMAGE_CKSUM}"

do_install() {
	install -d ${D}${bindir}
	install -d ${D}/opt/images
	wic cp  ${WORKDIR}/${ANDROID_IMAGE_FILENAME}:1${bindir}/install_android.sh ${D}${bindir}/install_android.sh
	chmod 755 ${D}${bindir}/install_android.sh
	chown root:root ${D}${bindir}/install_android.sh
	wic cp  ${WORKDIR}/${ANDROID_IMAGE_FILENAME}:1/opt/images/Android ${D}/opt/images/
	chown -R root:root ${D}/opt/images/Android
}

FILES:${PN} = "\
	${bindir}/install_android.sh \
	/opt/images/Android/* \
"

INSANE_SKIP:${PN} += "arch"

COMPATIBLE_MACHINE = "mx8-nxp-bsp"

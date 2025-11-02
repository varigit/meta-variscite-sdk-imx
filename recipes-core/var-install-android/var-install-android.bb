SUMMARY = "Variscite target Android install for recovery images."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

DEPENDS = "parted-native"

ANDROID_IMAGE_FILENAME:var-som-mx6 = "mx6__yocto-kirkstone-5.15.71_2.2.0-v1.0__android-8.0.0_1.0.0-v1.0.wic"
ANDROID_IMAGE_FOLDER:var-som-mx6 = "VAR-SOM-MX6"
ANDROID_IMAGE_CKSUM:var-som-mx6 = "e798776110c9b58be5c07d9d0ce0154f2329a9631bbe5537010de88390aa72e8"

ANDROID_IMAGE_FILENAME:imx8mp-var-dart = "mx8mp__yocto-scarthgap-6.6.23_2.0.0-v1.1__android-14.0.0_1.0.0-v1.1.wic"
ANDROID_IMAGE_FOLDER:imx8mp-var-dart = "DART-MX8M-PLUS"
ANDROID_IMAGE_CKSUM:imx8mp-var-dart = "1c16fede6494d722774df1359c8cca3b8db2abe8a98d5bf93e8b9d8e1c4fee1b"

ANDROID_IMAGE_FILENAME:imx8mm-var-dart = "mx8mm__yocto-scarthgap-6.6.52_2.2.0-v1.0__android-14.0.0_1.0.0-v1.3.wic"
ANDROID_IMAGE_FOLDER:imx8mm-var-dart = "DART-MX8M-MINI"
ANDROID_IMAGE_CKSUM:imx8mm-var-dart = "ca9f9b778286331f4c763d51cabb2e3884a87b595d92a98fab0eec1aaef34da3"

ANDROID_IMAGE_FILENAME:imx8mn-var-som = "mx8mn__yocto-scarthgap-6.6.52_2.2.0-v1.2__android-14.0.0_1.0.0-v1.1.wic"
ANDROID_IMAGE_FOLDER:imx8mn-var-som = "VAR-SOM-MX8M-NANO"
ANDROID_IMAGE_CKSUM:imx8mn-var-som = "b9951417d93e89fbb9c94fff49543ac7c186664efffdc10f5e52832d863aa7bf"

ANDROID_IMAGE_FILENAME:imx8mq-var-dart = "mx8m__yocto-mickledore-6.1.36_2.1.0-v1.0__android-13.0.0_1.2.0-v1.2.wic"
ANDROID_IMAGE_FOLDER:imx8mq-var-dart = "DART-MX8M"
ANDROID_IMAGE_CKSUM:imx8mq-var-dart = "cfd6b87e8fe5c585f3da6d45258609354d9f972e68f4350becbc6bd32bdf12b4"

ANDROID_IMAGE_FILENAME:imx8qxp-var-som = "mx8x__yocto-mickledore-6.1.36-2.1.0-v1.0__android-13.0.0_1.2.0-v1.0.wic"
ANDROID_IMAGE_FOLDER:imx8qxp-var-som = "VAR-SOM-MX8X"
ANDROID_IMAGE_CKSUM:imx8qxp-var-som = "ee91ac2c0c31822ea15352d2f79d1a768710f9c48d77dced40d58be013d0533f"

ANDROID_IMAGE_FILENAME:imx8qm-var-som = "mx8__yocto-mickledore-6.1.22-2.0.0-v1.0__android-13.0.0_1.2.0-v1.0.wic"
ANDROID_IMAGE_FOLDER:imx8qm-var-som = "VAR-SOM-MX8"
ANDROID_IMAGE_CKSUM:imx8qm-var-som = "22b0b98a7d4fdd1cca9d5a8a6ad674b90dd6e40df9526cdc29c82643ca1ea540"

SRC_URI = "https://variscite-public.nyc3.cdn.digitaloceanspaces.com/${ANDROID_IMAGE_FOLDER}/Software/${ANDROID_IMAGE_FILENAME}.zst;sha256sum=${ANDROID_IMAGE_CKSUM}"
# Machines still with .gz image
SRC_URI:var-som-mx6 = "https://variscite-public.nyc3.cdn.digitaloceanspaces.com/${ANDROID_IMAGE_FOLDER}/Software/${ANDROID_IMAGE_FILENAME}.gz;sha256sum=${ANDROID_IMAGE_CKSUM}"

FS_PART = "1"
FS_PART:var-som-mx6 = "2"

do_install() {
    install -d ${D}${bindir}
    install -d ${D}/opt/images
    wic cp  ${WORKDIR}/${ANDROID_IMAGE_FILENAME}:${FS_PART}${bindir}/install_android.sh ${D}${bindir}/install_android.sh
    chmod 755 ${D}${bindir}/install_android.sh
    chown root:root ${D}${bindir}/install_android.sh
    wic cp  ${WORKDIR}/${ANDROID_IMAGE_FILENAME}:${FS_PART}/opt/images/Android ${D}/opt/images/
    chown -R root:root ${D}/opt/images/Android
}

do_install:append:var-som-mx6() {
    wic cp  ${WORKDIR}/${ANDROID_IMAGE_FILENAME}:${FS_PART}${bindir}/install_android_emmc.sh ${D}${bindir}/install_android_emmc.sh
    chmod 755 ${D}${bindir}/install_android_emmc.sh
    chown root:root ${D}${bindir}/install_android_emmc.sh
}

FILES:${PN} = "\
    ${bindir}/install_android.sh \
    /opt/images/Android/* \
"

FILES:${PN}:append:var-som-mx6 = "\
    ${bindir}/install_android_emmc.sh \
"

INSANE_SKIP:${PN} += "arch"

RDEPENDS:${PN} = "\
    android-tools \
    android-tools-adbd \
    android-tools-fstools \
    bash \
    e2fsprogs-mke2fs \
    e2fsprogs-e2fsck \
    f2fs-tools \
    zstd \
"

COMPATIBLE_MACHINE = "(mx8-nxp-bsp|var-som-mx6)"

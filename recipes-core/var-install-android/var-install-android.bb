SUMMARY = "Variscite target Android install for recovery images."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

DEPENDS = "\
    tar-native \
    zstd-native \
"

SRC_URI = "https://variscite-public.nyc3.cdn.digitaloceanspaces.com/${ANDROID_IMAGE_FOLDER}/Software/android/${ANDROID_IMAGE_FILENAME}.tar.zst;sha256sum=${ANDROID_IMAGE_CKSUM}"

S = "${WORKDIR}/android-artifacts"

ANDROID_IMAGE_FILENAME:var-som-mx6 = "mx6-android-8.0.0_1.0.0-v1.0"
ANDROID_IMAGE_FOLDER:var-som-mx6 = "VAR-SOM-MX6"
ANDROID_IMAGE_CKSUM:var-som-mx6 = "c1ac5bec9465834dfc8114a8f65c2f95e7632f0fbb53dc6d4fca3f8f409193c0"

ANDROID_IMAGE_FILENAME:imx8mp-var-dart = "mx8mp-android-15.0.0_1.2.0-v1.1"
ANDROID_IMAGE_FOLDER:imx8mp-var-dart = "DART-MX8M-PLUS"
ANDROID_IMAGE_CKSUM:imx8mp-var-dart = "f35d597bf318cb452462975be5a96259be3ce9e12729410bf80133581e42fa84"

ANDROID_IMAGE_FILENAME:imx8mm-var-dart = "mx8mm-android-15.0.0_1.2.0-v1.0"
ANDROID_IMAGE_FOLDER:imx8mm-var-dart = "DART-MX8M-MINI"
ANDROID_IMAGE_CKSUM:imx8mm-var-dart = "388975f1b89762952c94bd375f6711ef7bbce7e8e8604571e862f5a0a275255a"

ANDROID_IMAGE_FILENAME:imx8mn-var-som = "mx8mn-android-14.0.0_1.0.0-v1.1"
ANDROID_IMAGE_FOLDER:imx8mn-var-som = "VAR-SOM-MX8M-NANO"
ANDROID_IMAGE_CKSUM:imx8mn-var-som = "50c2a30a405c858743e04edc1c0c14650c9ddc3977380fae2c7cef4daa5669bf"

ANDROID_IMAGE_FILENAME:imx8mq-var-dart = "mx8m-android-13.0.0_1.2.0-v1.3"
ANDROID_IMAGE_FOLDER:imx8mq-var-dart = "DART-MX8M"
ANDROID_IMAGE_CKSUM:imx8mq-var-dart = "d1fc38168aca51d39196111dbb9a77dc610ad5f1e92eb56d5d10b34cba4aabea"

ANDROID_IMAGE_FILENAME:imx8qxp-var-som = "mx8x-android-15.0.0_1.2.0-v1.0"
ANDROID_IMAGE_FOLDER:imx8qxp-var-som = "VAR-SOM-MX8X"
ANDROID_IMAGE_CKSUM:imx8qxp-var-som = "2391e3c4be4e28548864f6ca05149fc4f309b17e7b0befa77e9d815ff537f438"

ANDROID_IMAGE_FILENAME:imx8qm-var-som = "mx8-android-15.0.0_1.2.0-v1.0"
ANDROID_IMAGE_FOLDER:imx8qm-var-som = "VAR-SOM-MX8"
ANDROID_IMAGE_CKSUM:imx8qm-var-som = "348dee1a6f551b4129f0521e718625ad4f94cf5953391bc9e7631926bba578d6"

INSTALL_ANDROID_SCRIPT = "mx8_install_android.sh"
INSTALL_ANDROID_SCRIPT:var-som-mx6 = "install_android_emmc.sh"

INSTALL_ANDROID_SCRIPT_TARGET = "install_android.sh"
INSTALL_ANDROID_SCRIPT_TARGET:var-som-mx6 = "install_android_emmc.sh"

do_install() {
    install -d ${D}/opt/images/Android
    install -Dm 0755  ${S}/scripts/${INSTALL_ANDROID_SCRIPT} ${D}${bindir}/${INSTALL_ANDROID_SCRIPT_TARGET}
    cp -a ${S}/android/* ${D}/opt/images/Android
    chown -R root:root ${D}/opt/images/Android

    tar -cf - android | zstd -19 -T0 -f -o ${PN}.tar.zst

    install -Dm 0644 ${S}/${PN}.tar.zst ${DEPLOY_DIR_IMAGE}/${PN}.tar.zst
    cd ${DEPLOY_DIR_IMAGE}
    ln -sf ${PN}.tar.zst ${DEPLOY_DIR_IMAGE}/${PN}-${MACHINE}.tar.zst
}

FILES:${PN} = "\
    ${bindir}/${INSTALL_ANDROID_SCRIPT_TARGET} \
    /opt/images/Android/* \
"

RDEPENDS:${PN} = "\
    android-tools \
    android-tools-adbd \
    android-tools-fstools \
    bash \
    e2fsprogs-mke2fs \
    e2fsprogs-e2fsck \
    f2fs-tools \
    util-linux-sfdisk \
    zstd \
"

INSANE_SKIP:${PN} += "arch"

COMPATIBLE_MACHINE = "(mx8-nxp-bsp|var-som-mx6)"

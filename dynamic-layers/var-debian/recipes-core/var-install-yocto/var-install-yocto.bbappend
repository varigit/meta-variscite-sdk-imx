
# Adjust IMGS_PATH variable for Debian.
do_install:append:debian() {
    if [ -n "${VAR_RECOVERY_IMAGES_TARGET_PATH}" ]; then
        sed -i "s|^IMGS_PATH=.*|IMGS_PATH=\"${VAR_RECOVERY_IMAGES_TARGET_PATH}\"|g" ${D}${bindir}/${INSTALL_SCRIPT_NAME}
    fi
}

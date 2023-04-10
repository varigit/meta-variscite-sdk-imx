DESCRIPTION = "Same image as fsl-image-gui with Chromium browser added."
LICENSE = "MIT"


require recipes-fsl/images/fsl-image-gui.bb

IMAGE_INSTALL:append = "chromium-ozone-wayland"


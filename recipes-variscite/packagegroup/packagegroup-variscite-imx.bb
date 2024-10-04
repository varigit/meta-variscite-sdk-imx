SUMMARY = "Variscite Package Group for i.MX Machines"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

PACKAGES = "\
    ${PN}-docker \
    ${PN}-ml \

RDEPENDS:${PN}-docker:mx8-nxp-bsp = "\
    docker-moby \
    python3-docker-compose \
"

RDEPENDS:${PN}-docker:mx9-nxp-bsp = "\
    docker-moby \
    python3-docker-compose \
"

# Machine Learning package for i.MX8 machines
RDEPENDS:${PN}-ml:mx8-nxp-bsp = "\
    python3-opencv \
    python3-pip \
    python3-requests \
    python3-sympy \
    packagegroup-imx-ml \
"

# Machine Learning package for i.MX9 machines
RDEPENDS:${PN}-ml:mx9-nxp-bsp = "\
    python3-opencv \
    python3-pip \
    python3-requests \
    python3-sympy \
    packagegroup-imx-ml \
"

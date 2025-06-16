RDEPENDS:${PN} += "python3-core"

do_install:append() {
       sed -i -r "1 s=^\#\!/.*/bin/nativepython3?=\#\!/usr/bin/python3=g" ${D}/${PYTHON_SITEPACKAGES_DIR}/bin/deepview-modelclient
       sed -i -r "1 s=^\#\!/bin/sh=\#\!/usr/bin/python3=g" ${D}/${PYTHON_SITEPACKAGES_DIR}/bin/deepview-modelclient
}

QML_USE_SWCTX = "yes"
QML_USE_SWCTX:imxgpu3d = "no"

PACKAGECONFIG += "examples"

do_install:append () {
	if [ "${QML_USE_SWCTX}" = "yes" ]; then
		install -d ${D}${sysconfdir}/profile.d

		# allow using QML with SW rendering for machines with no GPU3D support, such as iMX6UL, iMX7 and iMX93
		echo "export QMLSCENE_DEVICE=softwarecontext" >> ${D}${sysconfdir}/profile.d/qt6.sh
	fi
}

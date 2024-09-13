# Remove networkd package when systemd-networkd is not the preferred connectivity manager
PACKAGECONFIG:remove:var-som = "${@bb.utils.contains('PREFERRED_CONNECTIVITY_MANAGER', 'systemd-networkd', \
		'','networkd', d)} \
"

SRC_URI := "${@d.getVar('SRC_URI').replace('branch=assimp_5.0_release', 'nobranch=1')}"

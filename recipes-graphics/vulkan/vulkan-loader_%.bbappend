SRC_URI := "${@d.getVar('SRC_URI').replace('branch=master', 'branch=main')}"

# i.MX95 ELE RSA/ECC drivers in NXP OP-TEE 4.10 can return an error instead
# of using their configured software fallback. Keep software RSA/ECC on the
# DART-MX95. Re-evaluate for later OP-TEE versions before adding a
# corresponding versioned bbappend.
EXTRA_OEMAKE:append:imx95-var-dart = " \
    CFG_IMX_ELE_RSA_DRV=n \
    CFG_IMX_ELE_ECC_DRV=n \
"

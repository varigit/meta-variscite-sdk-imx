How to use the Variscite SD card creation script:
=================================================

This utility is provided on an "AS IS" basis.
This is the script we use to create our recovery SD card.
For machines with Android support, it is a part of a larger script we use to create our recovery SD card, which also includes Android.
It is a good example for using the output of the Yocto build to create a bootable SD card, and use it to flash the target NAND flash/eMMC.

Note:
Before running this script you need to bitbake fsl-image-gui.


Usage:
sudo MACHINE=<var-som-mx6|imx6ul-var-dart|imx7-var-som|imx8mq-var-dart|imx8mm-var-dart|imx8qxp-var-som|imx8qxpb0-var-som|imx8qm-var-som|imx8mn-var-som|imx8mp-var-dart> var-create-yocto-sdcard.sh <options> device_node

options:
  -h              Display help message
  -s              Only Show partition sizes to be written, without actually writing them
  -a              Automatically set the rootfs partition size to fill the SD card (leaving spare 4MiB)
  -r ROOTFS_NAME  Select an alternative Rootfs for recovery images
                  (default: "build_xwayland/tmp/deploy/images/<machine>/fsl-image-gui-<machine>")

If you don't use the '-a' option, a default rootfs size of 7400MiB will be used.
The '-r' option allows you to create a bootable sdcard with an alternative image for the installation to NAND flash or eMMC.
Example: "-r tmp/deploy/images/var-som-mx6/fsl-image-qt5-var-som-mx6" -- selects the "Qt5 image with X11" recovery image


Once the script is done, use the SD card to boot, and then to flash your internal storage by running:
install_yocto.sh

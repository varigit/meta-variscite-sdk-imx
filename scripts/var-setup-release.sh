#!/bin/sh
#
# i.MX Yocto Project Build Environment Setup Script
#
# Copyright (C) 2011-2016 Freescale Semiconductor
# Copyright 2017 NXP
# Copyright 2021 Variscite
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

usage()
{
    echo -e "\nUsage: MACHINE=<machine> DISTRO=<distro> source var-setup-release.sh <build-dir>
    Optional parameters: [-h]"
}

# Print the usage menu if invalid options are specified
if [ "$#" -ne 1 ]; then usage
   return 1
fi

BUILD_DIR="$1"

# Determine if FSLC or NXP distro
if [[ "$DISTRO" == *"fsl-imx"* ]]; then
    MACHINE=${MACHINE} DISTRO=${DISTRO} . imx-setup-release.sh -b ${BUILD_DIR}
    # Remove duplicate BBLAYERS entries
    awk -i inplace '/meta-filesystems/&&c++>0 {next} 1' conf/bblayers.conf
    awk -i inplace '/meta-chromium/&&c++>0 {next} 1' conf/bblayers.conf
    awk -i inplace '/meta-clang/&&c++>0 {next} 1' conf/bblayers.conf
    awk -i inplace '/meta-networking/&&c++>0 {next} 1' conf/bblayers.conf
    awk -i inplace '/meta-qt5/&&c++>0 {next} 1' conf/bblayers.conf
    awk -i inplace '/meta-python2/&&c++>0 {next} 1' conf/bblayers.conf
    awk -i inplace '/meta-gnome/&&c++>0 {next} 1' conf/bblayers.conf
    awk -i inplace '/meta-virtualization/&&c++>0 {next} 1' conf/bblayers.conf
    # Remove fslc specific layers
    awk -i inplace '!/meta-freescale-ml/' conf/bblayers.conf
    # Remove apt package management
    awk -i inplace '!/package-management/' conf/local.conf
    awk -i inplace '!/package_deb/' conf/local.conf
else
    MACHINE=${MACHINE} DISTRO=${DISTRO} . setup-environment ${BUILD_DIR}
fi

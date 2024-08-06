DESCRIPTION = "Linux kernel for ${MACHINE}"
LICENSE = "GPL-2.0-only"
SECTION = "kernel"

MODULE = "linux-3.14.28"

inherit kernel machine_kernel_pr

MACHINE_KERNEL_PR:append = "r2"

COMPATIBLE_MACHINE = "gb7252"

SRC_URI[md5sum] = "c1e96f702ca737630f5acb0dce2388e7"
SRC_URI[sha256sum] = "72928012a7dbacbf95a371d9faa6800a20afd6b106958298cfc41028878aac4e"

LIC_FILES_CHKSUM = "file://COPYING;md5=d7810fab7487fb0aad327b76f1be7cd7"

SRC_URI += "https://source.mynonpublic.com/gigablue/linux/gigablue-linux-${PV}-20170331.tar.gz \
	file://defconfig \
	file://gbfindkerneldevice.py \
	file://linux_dvb_adapter.patch \
	file://bcm_genet_disable_warn.patch \
	file://linux_prevent_usb_dma_from_bmem.patch \
	file://rt2800usb_fix_warn_tx_status_timeout_to_dbg.patch \
	file://usb_core_hub_msleep.patch \
	file://rtl8712_fix_build_error.patch \
	file://kernel-add-support-for-gcc6.patch \
	file://kernel-add-support-for-gcc7.patch \
	file://kernel-add-support-for-gcc8.patch \
	file://kernel-add-support-for-gcc9.patch \
	file://kernel-add-support-for-gcc10.patch \
	file://kernel-add-support-for-gcc11.patch \
	file://kernel-add-support-for-gcc12.patch \
	file://kernel-add-support-for-gcc13.patch \
	file://build-with-gcc12-fixes.patch \
	file://0001-Support-TBS-USB-drivers.patch \
	file://0001-STV-Add-PLS-support.patch \
	file://0001-STV-Add-SNR-Signal-report-parameters.patch \
	file://0001-stv090x-optimized-TS-sync-control.patch \
	file://blindscan2.patch \
	file://genksyms_fix_typeof_handling.patch \
	file://0001-tuners-tda18273-silicon-tuner-driver.patch \
	file://01-10-si2157-Silicon-Labs-Si2157-silicon-tuner-driver.patch \
	file://02-10-si2168-Silicon-Labs-Si2168-DVB-T-T2-C-demod-driver.patch \
	file://0003-cxusb-Geniatech-T230-support.patch \
	file://CONFIG_DVB_SP2.patch \
	file://dvbsky.patch \
	file://rtl2832u-2.patch \
	file://0004-log2-give-up-on-gcc-constant-optimizations.patch \
	file://0005-uaccess-dont-mark-register-as-const.patch \
	file://0006-makefile-disable-warnings.patch \
	file://move-default-dialect-to-SMB3.patch \
	file://fix-multiple-defs-yyloc.patch \
	file://fix-linker-issue-undefined-reference.patch \
	file://linux3.4-ARM-8933-1-replace-Sun-Solaris-style-flag-on-section.patch \
	file://fix-build-with-binutils-2.41.patch \
	file://initramfs.cpio.gz;unpack=0 \
	file://cmdline.patch \
"

S = "${WORKDIR}/linux-${PV}"
B = "${WORKDIR}/build"

export OS = "Linux"
KERNEL_IMAGETYPE = "zImage"
KERNEL_OBJECT_SUFFIX = "ko"
KERNEL_IMAGEDEST = "tmp"
KERNEL_OUTPUT = "arch/${ARCH}/boot/${KERNEL_IMAGETYPE}"

FILES:${KERNEL_PACKAGE_NAME}-image = "/${KERNEL_IMAGEDEST}/zImage /${KERNEL_IMAGEDEST}/gbfindkerneldevice.py"

kernel_do_install:append() {
        install -d ${D}/${KERNEL_IMAGEDEST}
        install -m 0755 ${KERNEL_OUTPUT} ${D}/${KERNEL_IMAGEDEST}
        install -m 0755 ${WORKDIR}/gbfindkerneldevice.py ${D}/${KERNEL_IMAGEDEST}
}

kernel_do_configure:prepend() {
	install -d ${B}/usr
	install -m 0644 ${WORKDIR}/initramfs.cpio.gz ${B}/
}
kernel_do_compile() {
        unset CFLAGS CPPFLAGS CXXFLAGS LDFLAGS MACHINE
        oe_runmake ${KERNEL_IMAGETYPE_FOR_MAKE} ${KERNEL_ALT_IMAGETYPE} CC="${KERNEL_CC}" LD="${KERNEL_LD}" EXTRA_CFLAGS="-Wno-attribute-alias -Wno-error=date-time"
        if test "${KERNEL_IMAGETYPE_FOR_MAKE}.gz" = "${KERNEL_IMAGETYPE}"; then
                gzip -9c < "${KERNEL_IMAGETYPE_FOR_MAKE}" > "${KERNEL_OUTPUT}"
        fi
}

pkg_postinst:kernel-image () {
    if [ "x$D" == "x" ]; then
        if [ -f /${KERNEL_IMAGEDEST}/${KERNEL_IMAGETYPE} ] ; then
            ${PYTHON_PN} /${KERNEL_IMAGEDEST}/gbfindkerneldevice.py
            dd if=/${KERNEL_IMAGEDEST}/${KERNEL_IMAGETYPE} of=/dev/kernel
        fi
    fi
    rm -f /${KERNEL_IMAGEDEST}/${KERNEL_IMAGETYPE}
    true
}

pkg_postrm:kernel-image () {
}

FILESEXTRAPATHS:prepend := "${THISDIR}/linux-gigablue-${KV}:"

do_rm_work() {
}

# extra tasks
addtask kernel_link_images after do_compile before do_install
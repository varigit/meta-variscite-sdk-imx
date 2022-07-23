DESCRIPTION = "Freescale Image to validate wxWidgets on i.MX machines. \
This image contains everything used to test i.MX machines including GUI, \
demos and lots of applications. This creates a very large image, not \
suitable for production."
LICENSE = "MIT"


require recipes-fsl/images/fsl-image-gui.bb

IMAGE_INSTALL += " \
	g++-symlinks \
	gcc-symlinks \
	m4 \
	make \
	gdk-pixbuf \
	gdk-pixbuf-bin \
	gtk+3-dev \
	gtk+3-demo \
	wxwidgets \
	wxwidgets-dev \
	wxwidgets-samples \
	wxdashboard \
"


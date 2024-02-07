#!/bin/sh

westonapp_start()
{
    # set WAYLAND_DISPLAY to global socket
    . /etc/profile.d/weston.sh

    # wait for wayland to start
    while [ ! -e $WAYLAND_DISPLAY ]
    do
        sleep 0.1
    done

    # start the application
    kill -9 $(pidof weston-app) 2>/dev/null || true
    /usr/bin/weston-app &
}

westonapp_stop()
{
    kill -9 $(pidof weston-app) 2>/dev/null || true
}

###########################
#  Execution starts here  #
###########################
case $1 in

start)
	westonapp_start
	;;
stop)
	westonapp_stop
	;;
esac

exit 0

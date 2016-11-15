# Process this file with autoconf to produce a configure script.

AC_PREREQ(2.59)
AC_INIT([bluetruth_outstation_gui],[1.0],[radoslaw.golebiewski@simulation-systems.co.uk])

#Use tar-pax format to allow unlimited length file names
AM_INIT_AUTOMAKE([1.9 tar-pax foreign subdir-objects -Wall -Werror])
AC_CONFIG_SRCDIR([config.h.in])
AC_CONFIG_HEADER([config.h])

# Checks for programs.
AC_PROG_CXX
AC_PROG_CC
AC_PROG_INSTALL
AM_PROG_CC_C_O
AC_PROG_AWK
AC_PROG_SED
AC_PROG_LEX
AC_PROG_BISON([3.0])
AC_PROG_YACC

#Check for version of GCC
AX_CHECK_COMPILE_FLAG([-std=c++0x], [CXXFLAGS="$CXXFLAGS -std=c++0x -DSTD_CPP_0X"])

# Configure libtool
AC_PROG_LIBTOOL
AC_CONFIG_MACRO_DIR([m4])

# Checks for optional programs.
#AC_PROG_TRY_DOXYGEN

# Checks for libraries.

# Checks for header files.
AC_HEADER_STDC
AC_HEADER_SYS_WAIT
AC_CHECK_HEADERS([stddef.h stdlib.h string.h strings.h unistd.h])

# Checks for typedefs, structures, and compiler characteristics.
AC_HEADER_STDBOOL
AC_C_CONST
AC_C_INLINE
AC_C_RESTRICT
AC_TYPE_SIZE_T
AC_C_VOLATILE
AC_CHECK_TYPES([ptrdiff_t])

# Checks for library functions.
AC_FUNC_FORK
AC_FUNC_MALLOC
AC_FUNC_STAT
AC_CHECK_FUNCS([rmdir strcasecmp strchr strdup strerror])


# Create necessary project links
#$srcdir/create_project_links.sh


STD_INC_PATH="-I/usr/include -I/usr/local/include"
STD_LIB_PATH="-L/usr/local/lib"


dnl ---------------------------------------------------------------------------
dnl Check if wxWidgets library is available
dnl ---------------------------------------------------------------------------

AM_OPTIONS_WXCONFIG
reqwx=2.8.10
#AM_PATH_WXCONFIG($reqwx, wxWin=1, core)
#AM_PATH_WXCONFIG($reqwx, wxWin=1, core, --unicode --debug)
AM_PATH_WXCONFIG($reqwx, wxWin=1, core)
if test "$wxWin" != 1; then
	AC_MSG_ERROR([
		wxWidgets must be installed on your system.

		Please check that wx-config is in path, the directory
		where wxWidgets libraries are installed (returned by
		'wx-config --libs' or 'wx-config --static --libs' command)
		is in LD_LIBRARY_PATH or equivalent variable and
		wxWidgets version is $reqwx or above.
		])
fi


CPPFLAGS="$CPPFLAGS $WX_CPPFLAGS"
CXXFLAGS="$CXXFLAGS $WX_CXXFLAGS_ONLY"
CFLAGS="$CFLAGS $WX_CFLAGS_ONLY"
LIBS="$LIBS $WX_LIBS"
dnl ---------------------------------------------------------------------------


dnl ---------------------------------------------------------------------------
dnl Check if libboost library is available
dnl ---------------------------------------------------------------------------
BOOST_REQUIRE(1.42)
BOOST_SYSTEM
BOOST_ASIO
BOOST_CHRONO
BOOST_DATE_TIME
BOOST_FILESYSTEM


dnl ---------------------------------------------------------------------------
dnl Check for the presence of openssl libraries and headers
dnl ---------------------------------------------------------------------------
AX_CHECK_OPENSSL([], [
		AC_MSG_ERROR([
		libssl must bee installed on your system.

		Please install libssl development package.])
		])

dnl ---------------------------------------------------------------------------
dnl Check for the presence of LIBSSH2 libraries and headers
dnl ---------------------------------------------------------------------------
AC_CHECK_LIB(ssh2, libssh2_channel_open_ex, [], [
		AC_MSG_ERROR([
		libssh2 must bee installed on your system.

		Please install libssh2 development package.])
		])


#dnl ---------------------------------------------------------------------------
#dnl Check for the presence of libbluetooth library and header
#dnl ---------------------------------------------------------------------------
#AC_LANG_CPLUSPLUS
##Check for the header
#AC_CHECK_HEADERS([bluetooth/bluetooth.h bluetooth/hci.h bluetooth/hci_lib.h], [], [],
#[[
##ifdef HAVE_BLUETOOTH_BLUETOOTH_H
##include <bluetooth/bluetooth.h>
##endif
##ifdef HAVE_BLUETOOTH_HCI_H
##include <bluetooth/hci.h>
##endif
#]])
##Assume passed... Now append link parameters so that the next test can be executed
#LIBS="$LIBS -lbluetooth"
#AC_MSG_CHECKING([checking if libbluetooth library exists and is usable])
#AC_LINK_IFELSE([
#    AC_LANG_SOURCE(   [[
##include <stdlib.h>
##include <unistd.h>
##include <sys/socket.h>
##include <bluetooth/bluetooth.h>
##include <bluetooth/hci.h>
##include <bluetooth/hci_lib.h>
#int main()
#{
#hci_devid("test");
#return 0;
#}
#]] )],
#[AC_MSG_RESULT([yes])],
#[AC_MSG_ERROR([
#		libbluetooth must bee installed on your system.
#
#		Please install libbluetooth development package.])
#		])

# Create necessary project links
$srcdir/create_project_links.sh

AC_CONFIG_FILES([Makefile])
AC_OUTPUT


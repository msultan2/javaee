# Process this file with autoconf to produce a configure script.

AC_PREREQ(2.59)
AC_INIT([brdf],[1.00],[radoslaw.golebiewski@simulation-systems.co.uk])

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
#AX_CHECK_COMPILE_FLAG([-std=c++11], [CXXFLAGS="$CXXFLAGS -std=c++11"])

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
dnl Check if libboost library is available
dnl ---------------------------------------------------------------------------
BOOST_REQUIRE(1.54)
BOOST_SYSTEM
BOOST_ASIO
BOOST_CHRONO
BOOST_DATE_TIME
BOOST_FILESYSTEM
BOOST_LOG
BOOST_PROGRAM_OPTIONS


dnl ---------------------------------------------------------------------------
dnl Check for the presence of libbluetooth library and header
dnl ---------------------------------------------------------------------------
AC_LANG_CPLUSPLUS
# Check for the header
AC_CHECK_HEADERS([mongo/client/dbclient.h mongo/client/connpool.h], [], [],
[[
#include <mongo/client/dbclient.h>
#include <mongo/client/connpool.h>
]])
#Assume passed... Now append link parameters so that the next test can be executed
LIBS="$LIBS -L/usr/local/lib -lmongoclient \
-lboost_system -lboost_thread -lboost_filesystem -lboost_date_time -lboost_program_options -lboost_regex \
-lrt -lpthread"
AC_MSG_CHECKING([checking if libmongoclient library exists and is usable])
AC_LINK_IFELSE([
    AC_LANG_SOURCE(   [[
#include <mongo/client/dbclient.h>
#include <mongo/client/connpool.h>
int main()
{
    mongo::client::initialize();
    mongo::client::shutdown();
    return 0;
}
]] )],
[AC_MSG_RESULT([yes])],
[AC_MSG_ERROR([
		libmongoclient must bee installed on your system.

		Please install libmongoclient development package.])
		])

# Create necessary project links
$srcdir/create_project_links.sh

AC_CONFIG_FILES([Makefile])
AC_OUTPUT

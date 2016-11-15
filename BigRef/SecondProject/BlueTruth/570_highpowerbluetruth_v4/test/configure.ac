# Process this file with autoconf to produce a configure script.

AC_PREREQ(2.59)
AC_INIT([outstation_test],[4.03],[radoslaw.golebiewski@simulation-systems.co.uk])

AC_ARG_ENABLE(coverage, AC_HELP_STRING([--enable-coverage],[enable code coverage tests]))

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
dnl Check if libboost library is available
dnl ---------------------------------------------------------------------------
BOOST_REQUIRE(1.42)
BOOST_SYSTEM
BOOST_ASIO
BOOST_CHRONO
BOOST_DATE_TIME
BOOST_FILESYSTEM


dnl ---------------------------------------------------------------------------
dnl Check for the presence of tinyxml library and header
dnl ---------------------------------------------------------------------------
AC_LANG_CPLUSPLUS

AS_IF([test "x$enable_coverage" = "xyes"],
    [
        AM_CXXFLAGS="$AM_CXXFLAGS -fprofile-arcs -ftest-coverage -fno-inline"
        AM_CFLAGS="$AM_CFLAGS -fprofile-arcs -ftest-coverage -fno-inline"
        AC_CHECK_PROG([have_lcov],lcov,yes,no)

        AS_IF([test "x$have_lcov" = "xno"],
            [
                AC_MSG_ERROR([
                Unable to locate the coverage visualisation program, lcov.
                Install the program and rerun ./configure.
                ])
            ]
        )

        AC_CHECK_PROG([have_gcov],gcov,yes,no)

        AS_IF([test "x$have_gcov" = "xno"],
            [
                AC_MSG_ERROR([
                Unable to locate gcov.
                Install the program and rerun ./configure.
                ])
            ]
        )

        AC_CHECK_PROG([have_genhtml],genhtml,yes,no)

        AS_IF([test "x$have_genhtml" = "xno"],
            [
                AC_MSG_ERROR([
                Unable to locate genhtml (part of lcov).
                Install the program and rerun ./configure.
                ])
            ]
        )
        AC_CHECK_PROG([have_geninfo],geninfo,yes,no)

        AS_IF([test "x$have_geninfo" = "xno"],
            [
                AC_MSG_ERROR([
                Unable to locate geninfo (part of lcov).
                Install the program and rerun ./configure.
                ])
            ]
        )
    ]
)
AM_CONDITIONAL([ENABLED_COVERAGE],[test "x$enable_coverage" = "xyes"])

AC_SUBST([AM_CXXFLAGS])
AC_SUBST([AM_CFLAGS])


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


AC_CHECK_FILE($srcdir/create_project_links.sh, [
	# Create necessary project links
	$srcdir/create_project_links.sh ])


AC_CONFIG_FILES([Makefile])
AC_OUTPUT


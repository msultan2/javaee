bin_PROGRAMS = brdf

ACLOCAL_AMFLAGS = -I m4

brdf_SOURCES = \
    $(common_files) \
    $(controller_files) \
    $(logger_files) \
    $(model_files) \
    $(multithreading_files) \
    $(parsing_files) \
    $(view_files) \
    $(lib_files)

brdf_CPPFLAGS = \
    -Wall \
    -Wextra \
    -g -O0 \
    -fno-default-inline \
    -Wno-write-strings \
    -Wno-deprecated-declarations \
    -DUNICODE \
    -DPKG_DATA_DIRECTORY=\"$(pkgdatadir)\" \
    -DSYSCONF_DIRECTORY=\"$(sysconfdir)\" \
    -DLOCAL_STATE_DIRECTORY=\"$(localstatedir)\" \
    $(brdf_include_files_cppflags) \
    $(BOOST_CPPFLAGS)


##Additionally install documentation
##user_manual_file = SSL4253A.pdf
##brdf_docdir = $(pkgdatadir)/doc
##brdf_doc_DATA = \
##    $(top_srcdir)/doc/$(user_manual_file)
##brdf_full_user_manual_path = $(brdf_docdir)/$(user_manual_file)


#-------------------------------------------------------------------------

#Specify compilation includes and the source directories
brdf_include_files_cppflags = \
    -I@top_srcdir@/src \
    -I@top_srcdir@/src/common \
    -I@top_srcdir@/src/controller \
    -I@top_srcdir@/src/logger \
    -I@top_srcdir@/src/model \
    -I@top_srcdir@/src/model/common \
    -I@top_srcdir@/src/model/communication \
    -I@top_srcdir@/src/model/configuration \
    -I@top_srcdir@/src/multithreading \
    -I@top_srcdir@/src/parsing \
    -I@top_srcdir@/src/view \
    -I@top_srcdir@/libs

brdf_LDFLAGS = $(BOOST_THREAD_LDFLAGS) $(BOOST_THREAD_LIBS) \
	"-Wl,-rpath,/opt/brdf/lib"

brdf_LDADD = -lmongoclient \
	-lboost_system -lboost_thread -lboost_filesystem \
	-lboost_date_time -lboost_program_options \
	-lboost_log -lboost_log_setup -lboost_regex \
	-lrt -lpthread

#
# List of used files follows:
#

common_files = \
    @top_srcdir@/src/common/app.cpp \
    @top_srcdir@/src/common/app.h \
    @top_srcdir@/src/common/applicationconfiguration.cpp \
    @top_srcdir@/src/common/applicationconfiguration.h \
    @top_srcdir@/src/common/clock.cpp \
    @top_srcdir@/src/common/clock.h \
    @top_srcdir@/src/common/identifiable.h \
    @top_srcdir@/src/common/identifiable.hpp \
    @top_srcdir@/src/common/initialiser.cpp \
    @top_srcdir@/src/common/initialiser.h \
    @top_srcdir@/src/common/iobservable.cpp \
    @top_srcdir@/src/common/iobservable.h \
    @top_srcdir@/src/common/iobserver.cpp \
    @top_srcdir@/src/common/iobserver.h \
    @top_srcdir@/src/common/os_utilities.cpp \
    @top_srcdir@/src/common/os_utilities.h \
    @top_srcdir@/src/common/stdafx.cpp \
    @top_srcdir@/src/common/stdafx.h \
    @top_srcdir@/src/common/types.cpp \
    @top_srcdir@/src/common/types.h \
    @top_srcdir@/src/common/unicode_types.h \
    @top_srcdir@/src/common/utils.cpp \
    @top_srcdir@/src/common/utils.h \
    @top_srcdir@/src/common/version.cpp \
    @top_srcdir@/src/common/version.h

controller_files = \
    @top_srcdir@/src/controller/controller.cpp \
    @top_srcdir@/src/controller/controller.h

logger_files = \
    @top_srcdir@/src/logger/boostlogger.h \
    @top_srcdir@/src/logger/boostlogger.cpp \
    @top_srcdir@/src/logger/logger.h \
    @top_srcdir@/src/logger/logger.cpp

model_files = \
    @top_srcdir@/src/model/model.cpp \
    @top_srcdir@/src/model/model.h \
    @top_srcdir@/src/model/common/activetask.cpp \
    @top_srcdir@/src/model/common/activetask.h \
    @top_srcdir@/src/model/common/fastdatapacket.cpp \
    @top_srcdir@/src/model/common/fastdatapacket.h \
    @top_srcdir@/src/model/common/itask.cpp \
    @top_srcdir@/src/model/common/itask.h \
    @top_srcdir@/src/model/common/uri.cpp \
    @top_srcdir@/src/model/common/uri.h \
    @top_srcdir@/src/model/communication/activeboostasio.cpp \
    @top_srcdir@/src/model/communication/activeboostasio.h \
    @top_srcdir@/src/model/communication/activeboostasiotcpclient.cpp \
    @top_srcdir@/src/model/communication/activeboostasiotcpclient.h \
    @top_srcdir@/src/model/communication/boostasio.cpp \
    @top_srcdir@/src/model/communication/boostasio.h \
    @top_srcdir@/src/model/communication/iconnectionconsumer.cpp \
    @top_srcdir@/src/model/communication/iconnectionconsumer.h \
    @top_srcdir@/src/model/communication/iconnectionproducerclient.cpp \
    @top_srcdir@/src/model/communication/iconnectionproducerclient.h \
    @top_srcdir@/src/model/communication/brdf_server/brdfserverhttpclient.cpp \
    @top_srcdir@/src/model/communication/brdf_server/brdfserverhttpclient.h \
    @top_srcdir@/src/model/communication/brdf_server/brdfserverreporter.cpp \
    @top_srcdir@/src/model/communication/brdf_server/brdfserverreporter.h \
    @top_srcdir@/src/model/communication/brdf_server/ibrdfserverreporter.cpp \
    @top_srcdir@/src/model/communication/brdf_server/ibrdfserverreporter.h \
    @top_srcdir@/src/model/communication/brdf_server/ihttpclient.cpp \
    @top_srcdir@/src/model/communication/brdf_server/ihttpclient.h \
    @top_srcdir@/src/model/communication/brdf_server/periodicallysendrawdatatask.cpp \
    @top_srcdir@/src/model/communication/brdf_server/periodicallysendrawdatatask.h \
    @top_srcdir@/src/model/configuration/brdfmongoconfiguration.cpp \
    @top_srcdir@/src/model/configuration/brdfmongoconfiguration.h \
    @top_srcdir@/src/model/configuration/brdfxmlconfiguration.cpp \
    @top_srcdir@/src/model/configuration/brdfxmlconfiguration.h \
    @top_srcdir@/src/model/configuration/ibrdfxmlconfiguration.cpp \
    @top_srcdir@/src/model/configuration/ibrdfxmlconfiguration.h \
    @top_srcdir@/src/model/configuration/xmlconfiguration.cpp \
    @top_srcdir@/src/model/configuration/xmlconfiguration.h \
    @top_srcdir@/src/model/mongo/mongoclient.cpp \
    @top_srcdir@/src/model/mongo/mongoclient.h

multithreading_files = \
    @top_srcdir@/src/multithreading/activeobject.cpp \
    @top_srcdir@/src/multithreading/activeobject.h \
    @top_srcdir@/src/multithreading/atomicvariable.h \
    @top_srcdir@/src/multithreading/boostsemaphore.cpp \
    @top_srcdir@/src/multithreading/boostsemaphore.h \
    @top_srcdir@/src/multithreading/eventhandler.cpp \
    @top_srcdir@/src/multithreading/eventhandler.h

view_files = \
    @top_srcdir@/src/view/view.cpp \
    @top_srcdir@/src/view/view.h

lib_files = \
    @top_srcdir@/libs/tinyxml/tinystr.cpp \
    @top_srcdir@/libs/tinyxml/tinystr.h \
    @top_srcdir@/libs/tinyxml/tinyxml.cpp \
    @top_srcdir@/libs/tinyxml/tinyxml.h \
    @top_srcdir@/libs/tinyxml/tinyxmlerror.cpp \
    @top_srcdir@/libs/tinyxml/tinyxmlparser.cpp

parsing_files = \
    @top_srcdir@/src/parsing/http_response_parser.yy \
    @top_srcdir@/src/parsing/http_response_scanner.ll \
	\
    @top_srcdir@/src/parsing/http_response.cpp \
    @top_srcdir@/src/parsing/http_response.h \
    @top_srcdir@/src/parsing/httpresponseparser.cpp \
    @top_srcdir@/src/parsing/httpresponseparser.h \
    @top_srcdir@/src/parsing/parser_common.cpp \
    @top_srcdir@/src/parsing/parser_common.h \
	\
    @top_srcdir@/build/http_response_scanner.h \
    @top_srcdir@/src/parsing/http_response_parser.hh

#Add dependencies between lex and bison so that headers and implementation files
#are created in the right order
@top_srcdir@/build/http_response_scanner.h : \
    @top_srcdir@/src/parsing/http_response_parser.hh
@top_srcdir@/src/parsing/http_response_parser.hh : \
    @top_srcdir@/src/parsing/http_response_parser.cc

#Delete these files in maintainer-clean mode
MAINTAINERCLEANFILES = \
    @top_srcdir@/build/http_response_scanner.h \
    @top_srcdir@/src/parsing/http_response_scanner.cc \
    @top_srcdir@/src/parsing/http_response_parser.cc \
    @top_srcdir@/src/parsing/http_response_parser.hh

# Recover from removal of scanner/parser headers
BUILT_SOURCES = \
    @top_srcdir@/build/http_response_scanner.h \
    @top_srcdir@/src/parsing/http_response_parser.hh

@top_srcdir@/build/http_response_scanner.h: @top_srcdir@/src/parsing/http_response_scanner.ll
	flex -t $< > /dev/null

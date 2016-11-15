bin_PROGRAMS = outstation

ACLOCAL_AMFLAGS = -I m4

outstation_SOURCES = \
    $(common_files) \
    $(controller_files) \
    $(logger_files) \
    $(model_files) \
    $(multithreading_files) \
    $(parsing_files) \
    $(view_files) \
    $(lib_files) \
    $(linux_additions) \
    $(debian_package_files) \
    $(outstation_doc_DATA)

nodist_outstation_SOURCES = \
    $(nodist_parsing_files)

outstation_CPPFLAGS = \
    -Wall \
    -Wextra \
    -g -O0 \
    -fno-default-inline \
    -Wno-write-strings \
    -Wno-deprecated-declarations \
    -fno-omit-frame-pointer \
    -DUNICODE \
    -DBIN_DIRECTORY=\"$(bindir)\" \
    -DPKG_DATA_DIRECTORY=\"$(pkgdatadir)\" \
    -DSYSCONF_DIRECTORY=\"$(sysconfdir)\" \
    -DLOCAL_STATE_DIRECTORY=\"$(localstatedir)\" \
    $(outstation_include_files_cppflags) \
    $(BOOST_CPPFLAGS)


#-------------------------------------------------------------------------
#Items that will cause the outstation icon to be displayed in the menu in Simulation Systems Ltd submenu

#Setup special menu "Simulation Systems Ltd"
#outstation_desktop_menudir = /etc/xdg/menus/applications-merged
#outstation_desktop_menu_DATA = \
#    $(top_srcdir)/linux-additions/simulationsystems.menu

#outstation_desktop_menudescriptiondir = $(datadir)/desktop-directories/simulationsystems
#outstation_desktop_menudescription_DATA = \
#    $(top_srcdir)/linux-additions/SimulationSystems.directory

#Icon that will be displayed for the outstation application
outstation_pixmapsdir = $(datadir)/pixmaps/simulationsystems
outstation_pixmaps_DATA = \
    $(top_srcdir)/linux-additions/outstation.png

#Copy outstation.desktop.stub to outstation.desktop and setup icon path
outstation.desktop: $(top_srcdir)/linux-additions/outstation.desktop.stub
	cat $(top_srcdir)/linux-additions/outstation.desktop.stub | sed s/^Icon=/Icon=`echo $(outstation_pixmapsdir)/outstation.png | awk '{gsub("/","\\\\/"); print }'`/ >> outstation.desktop

outstation_desktopdir = $(datadir)/applications/simulationsystems
outstation_desktop_DATA = outstation.desktop

DISTCLEANFILES = outstation.desktop

#Add coreConfiguration files to be installed into sysconf directory ($(prefix)/etc)
outstation_sysconfdir = $(sysconfdir)
outstation_sysconf_DATA = \
    @top_srcdir@/src/bluetruth_top/misc/core_configuration.xml \
    @top_srcdir@/src/bluetruth_top/misc/core_configuration.xsd \
    @top_srcdir@/src/bluetruth_top/misc/generic_outstation_to_instation_key \
    @top_srcdir@/src/bluetruth_top/misc/functional_configuration.conf

outstation_bindir = $(bindir)
outstation_bin_DATA = \
    @top_srcdir@/src/bluetruth_top/misc/reverse_ssh \
    @top_srcdir@/src/bluetruth_top/misc/get_modem_signal_level \
    @top_srcdir@/src/bluetruth_top/misc/get_modem_status.exp

EXTRA_DIST = \
    $(outstation_sysconf_DATA) \
    $(outstation_bin_DATA)

##Additionally install documentation
##user_manual_file = SSL4xxx.pdf
##outstation_docdir = $(pkgdatadir)/doc
##outstation_doc_DATA = \
##    $(top_srcdir)/doc/$(user_manual_file)
##outstation_full_user_manual_path = $(outstation_docdir)/$(user_manual_file)


#Create /var/log/bt and /var/cache/bt directories during installation so that log files can be written there
install-data-hook:
	mkdir --parents --mode=777 $(localstatedir)/log/bt
	mkdir --parents --mode=777 $(localstatedir)/cache/bt
	chmod 755 $(outstation_bindir)/reverse_ssh
	chmod 400 $(outstation_sysconfdir)/generic_outstation_to_instation_key
	chmod 755 $(outstation_bindir)/get_modem_signal_level
	chmod 755 $(outstation_sysconfdir)/get_modem_status.exp

#-------------------------------------------------------------------------

#Specify compilation includes and the source directories
outstation_include_files_cppflags = \
    -I@top_srcdir@/src \
    -I@top_srcdir@/src/common \
    -I@top_srcdir@/src/controller \
    -I@top_srcdir@/src/model \
    -I@top_srcdir@/src/model/communication \
    -I@top_srcdir@/src/model/configuration \
    -I@top_srcdir@/src/model/common \
    -I@top_srcdir@/src/view \
    -I@top_srcdir@/build \
    -I@top_srcdir@/src/bluetruth_top/libs \
    -I@top_srcdir@/src/bluetruth_top/src/common \
    -I@top_srcdir@/src/bluetruth_top/src/logger \
    -I@top_srcdir@/src/bluetruth_top/src/model \
    -I@top_srcdir@/src/bluetruth_top/src/model/bluetooth \
    -I@top_srcdir@/src/bluetruth_top/src/model/communication \
    -I@top_srcdir@/src/bluetruth_top/src/model/configuration \
    -I@top_srcdir@/src/bluetruth_top/src/model/common \
    -I@top_srcdir@/src/bluetruth_top/src/multithreading \
    -I@top_srcdir@/src/bluetruth_top/src/parsing \
    -I@top_srcdir@/src/bluetruth_top/src/view

outstation_LDFLAGS = $(BOOST_THREAD_LDFLAGS) $(BOOST_THREAD_LIBS)
outstation_LDADD = -lrt -lpthread \
	-lboost_system -lboost_date_time -lboost_chrono \
	-lboost_thread -lboost_filesystem -lcrypto -lboost_program_options

#
# List of used files follows:
#

common_files = \
    @top_srcdir@/src/common/app.cpp \
    @top_srcdir@/src/common/app.h \
    @top_srcdir@/src/common/applicationconfiguration.cpp \
    @top_srcdir@/src/common/applicationconfiguration.h \
    @top_srcdir@/src/bluetruth_top/src/common/execute_command.cpp \
    @top_srcdir@/src/bluetruth_top/src/common/execute_command.h \
    @top_srcdir@/src/bluetruth_top/src/common/targetver.h \
    @top_srcdir@/src/bluetruth_top/src/common/clock.h \
    @top_srcdir@/src/bluetruth_top/src/common/clock.cpp \
    @top_srcdir@/src/bluetruth_top/src/common/identifiable.h \
    @top_srcdir@/src/bluetruth_top/src/common/identifiable.hpp \
    @top_srcdir@/src/bluetruth_top/src/common/initialiser.h \
    @top_srcdir@/src/bluetruth_top/src/common/initialiser.cpp \
    @top_srcdir@/src/bluetruth_top/src/common/iobserver.h \
    @top_srcdir@/src/bluetruth_top/src/common/iobserver.cpp \
    @top_srcdir@/src/bluetruth_top/src/common/iobservable.h \
    @top_srcdir@/src/bluetruth_top/src/common/iobservable.cpp \
    @top_srcdir@/src/bluetruth_top/src/common/os_utilities.h \
    @top_srcdir@/src/bluetruth_top/src/common/os_utilities.cpp \
    @top_srcdir@/src/common/stdafx.cpp \
    @top_srcdir@/src/common/stdafx.h \
    @top_srcdir@/src/bluetruth_top/src/common/types.cpp \
    @top_srcdir@/src/bluetruth_top/src/common/types.h \
    @top_srcdir@/src/bluetruth_top/src/common/unicode_types.h \
    @top_srcdir@/src/bluetruth_top/src/common/utils.cpp \
    @top_srcdir@/src/bluetruth_top/src/common/utils.h \
    @top_srcdir@/src/common/version.cpp \
    @top_srcdir@/src/common/version.h

parsing_files = \
    @top_srcdir@/src/bluetruth_top/src/parsing/backgrounddevices_parser.yy \
    @top_srcdir@/src/bluetruth_top/src/parsing/backgrounddevices_scanner.ll \
    @top_srcdir@/src/bluetruth_top/src/parsing/configuration_parser.yy \
    @top_srcdir@/src/bluetruth_top/src/parsing/configuration_scanner.ll \
    @top_srcdir@/src/bluetruth_top/src/parsing/http_response_parser.yy \
    @top_srcdir@/src/bluetruth_top/src/parsing/http_response_scanner.ll \
    @top_srcdir@/src/bluetruth_top/src/parsing/parani_output_parser.yy \
    @top_srcdir@/src/bluetruth_top/src/parsing/parani_output_scanner.ll \
    @top_srcdir@/src/bluetruth_top/src/parsing/response_body_parser.yy \
    @top_srcdir@/src/bluetruth_top/src/parsing/response_body_scanner.ll \
\
    @top_srcdir@/src/bluetruth_top/src/parsing/backgrounddevicesparser.cpp \
    @top_srcdir@/src/bluetruth_top/src/parsing/backgrounddevicesparser.h \
    @top_srcdir@/src/bluetruth_top/src/parsing/configurationparser.cpp \
    @top_srcdir@/src/bluetruth_top/src/parsing/configurationparser.h \
    @top_srcdir@/src/bluetruth_top/src/parsing/http_response.cpp \
    @top_srcdir@/src/bluetruth_top/src/parsing/http_response.h \
    @top_srcdir@/src/bluetruth_top/src/parsing/httpresponseparser.cpp \
    @top_srcdir@/src/bluetruth_top/src/parsing/httpresponseparser.h \
    @top_srcdir@/src/bluetruth_top/src/parsing/paranioutputparser.cpp \
    @top_srcdir@/src/bluetruth_top/src/parsing/paranioutputparser.h \
    @top_srcdir@/src/bluetruth_top/src/parsing/parser_common.cpp \
    @top_srcdir@/src/bluetruth_top/src/parsing/parser_common.h \
    @top_srcdir@/src/bluetruth_top/src/parsing/responsebodyparser.cpp \
    @top_srcdir@/src/bluetruth_top/src/parsing/responsebodyparser.h \
    \
    @top_srcdir@/build/backgrounddevices_scanner.h \
    @top_srcdir@/src/bluetruth_top/src/parsing/backgrounddevices_parser.hh \
    @top_srcdir@/build/configuration_scanner.h \
    @top_srcdir@/src/bluetruth_top/src/parsing/configuration_parser.hh \
    @top_srcdir@/build/http_response_scanner.h \
    @top_srcdir@/src/bluetruth_top/src/parsing/http_response_parser.hh \
    @top_srcdir@/build/parani_output_scanner.h \
    @top_srcdir@/src/bluetruth_top/src/parsing/parani_output_parser.hh \
    @top_srcdir@/build/response_body_scanner.h \
    @top_srcdir@/src/bluetruth_top/src/parsing/response_body_parser.hh

#Add dependencies between lex and bison so that headers and implementation files
#are created in the right order
@top_srcdir@/build/backgrounddevices_scanner.h : \
	@top_srcdir@/src/bluetruth_top/src/parsing/backgrounddevices_parser.hh
@top_srcdir@/src/bluetruth_top/src/parsing/backgrounddevices_parser.hh : \
	@top_srcdir@/src/bluetruth_top/src/parsing/backgrounddevices_parser.cc

@top_srcdir@/build/configuration_scanner.h : \
    @top_srcdir@/src/bluetruth_top/src/parsing/configuration_parser.hh
@top_srcdir@/src/bluetruth_top/src/parsing/configuration_parser.hh : \
    @top_srcdir@/src/bluetruth_top/src/parsing/configuration_parser.cc

@top_srcdir@/build/http_response_scanner.h : \
    @top_srcdir@/src/bluetruth_top/src/parsing/http_response_parser.hh
@top_srcdir@/src/bluetruth_top/src/parsing/http_response_parser.hh : \
    @top_srcdir@/src/bluetruth_top/src/parsing/http_response_parser.cc

@top_srcdir@/build/parani_output_scanner.h : \
    @top_srcdir@/src/bluetruth_top/src/parsing/parani_output_parser.hh
@top_srcdir@/src/bluetruth_top/src/parsing/parani_output_parser.hh : \
    @top_srcdir@/src/bluetruth_top/src/parsing/parani_output_parser.cc

@top_srcdir@/build/response_body_scanner.h : \
    @top_srcdir@/src/bluetruth_top/src/parsing/response_body_parser.hh
@top_srcdir@/src/bluetruth_top/src/parsing/response_body_parser.hh : \
    @top_srcdir@/src/bluetruth_top/src/parsing/response_body_parser.cc


#Delete these files in maintainer-clean mode
MAINTAINERCLEANFILES = \
    @top_srcdir@/build/backgrounddevices_scanner.h \
    @top_srcdir@/src/bluetruth_top/src/parsing/backgrounddevices_scanner.cc \
    @top_srcdir@/src/bluetruth_top/src/parsing/backgrounddevices_parser.cc \
    @top_srcdir@/src/bluetruth_top/src/parsing/backgrounddevices_parser.hh \
    @top_srcdir@/build/configuration_scanner.h \
    @top_srcdir@/src/bluetruth_top/src/parsing/configuration_scanner.cc \
    @top_srcdir@/src/bluetruth_top/src/parsing/configuration_parser.cc \
    @top_srcdir@/src/bluetruth_top/src/parsing/configuration_parser.hh \
    @top_srcdir@/build/http_response_scanner.h \
    @top_srcdir@/src/bluetruth_top/src/parsing/http_response_scanner.cc \
    @top_srcdir@/src/bluetruth_top/src/parsing/http_response_parser.cc \
    @top_srcdir@/src/bluetruth_top/src/parsing/http_response_parser.hh \
    @top_srcdir@/build/parani_output_scanner.h \
    @top_srcdir@/src/bluetruth_top/src/parsing/parani_output_scanner.cc \
    @top_srcdir@/src/bluetruth_top/src/parsing/parani_output_parser.cc \
    @top_srcdir@/src/bluetruth_top/src/parsing/parani_output_parser.hh \
    @top_srcdir@/build/response_body_scanner.h \
    @top_srcdir@/src/bluetruth_top/src/parsing/response_body_scanner.cc \
    @top_srcdir@/src/bluetruth_top/src/parsing/response_body_parser.cc \
    @top_srcdir@/src/bluetruth_top/src/parsing/response_body_parser.hh

# Recover from removal of scanner/parser headers
BUILT_SOURCES = \
    @top_srcdir@/build/backgrounddevices_scanner.h \
    @top_srcdir@/src/bluetruth_top/src/parsing/backgrounddevices_parser.hh \
    @top_srcdir@/build/configuration_scanner.h \
    @top_srcdir@/src/bluetruth_top/src/parsing/configuration_parser.hh \
    @top_srcdir@/build/http_response_scanner.h \
    @top_srcdir@/src/bluetruth_top/src/parsing/http_response_parser.hh \
    @top_srcdir@/build/parani_output_scanner.h \
    @top_srcdir@/src/bluetruth_top/src/parsing/parani_output_parser.hh \
    @top_srcdir@/build/response_body_scanner.h \
    @top_srcdir@/src/bluetruth_top/src/parsing/response_body_parser.hh

@top_srcdir@/build/backgrounddevices_scanner.h: @top_srcdir@/src/bluetruth_top/src/parsing/backgrounddevices_scanner.ll
	flex -t $< > /dev/null

@top_srcdir@/build/configuration_scanner.h: @top_srcdir@/src/bluetruth_top/src/parsing/configuration_scanner.ll
	flex -t $< > /dev/null

@top_srcdir@/build/http_response_scanner.h: @top_srcdir@/src/bluetruth_top/src/parsing/http_response_scanner.ll
	flex -t $< > /dev/null

@top_srcdir@/build/parani_output_scanner.h: @top_srcdir@/src/bluetruth_top/src/parsing/parani_output_scanner.ll
	flex -t $< > /dev/null

@top_srcdir@/build/response_body_scanner.h: @top_srcdir@/src/bluetruth_top/src/parsing/response_body_scanner.ll
	flex -t $< > /dev/null

controller_files = \
    @top_srcdir@/src/controller/controller.cpp \
    @top_srcdir@/src/controller/controller.h


logger_files = \
    @top_srcdir@/src/bluetruth_top/src/logger/filelogger.h \
    @top_srcdir@/src/bluetruth_top/src/logger/filelogger.cpp \
    @top_srcdir@/src/bluetruth_top/src/logger/ierrordialog.h \
    @top_srcdir@/src/bluetruth_top/src/logger/ierrordialog.cpp \
    @top_srcdir@/src/bluetruth_top/src/logger/ilogsink.h \
    @top_srcdir@/src/bluetruth_top/src/logger/ilogsink.cpp \
    @top_srcdir@/src/bluetruth_top/src/logger/legacyconsolelogsink.h \
    @top_srcdir@/src/bluetruth_top/src/logger/legacyconsolelogsink.cpp \
    @top_srcdir@/src/bluetruth_top/src/logger/legacyfilelogsink.h \
    @top_srcdir@/src/bluetruth_top/src/logger/legacyfilelogsink.cpp \
    @top_srcdir@/src/bluetruth_top/src/logger/logger.h \
    @top_srcdir@/src/bluetruth_top/src/logger/logger.cpp \
    @top_srcdir@/src/bluetruth_top/src/logger/loggerdefinitions.h \
    @top_srcdir@/src/bluetruth_top/src/logger/loghandler.h \
    @top_srcdir@/src/bluetruth_top/src/logger/loghandler.cpp \
    @top_srcdir@/src/bluetruth_top/src/logger/managedfilelogger.h \
    @top_srcdir@/src/bluetruth_top/src/logger/managedfilelogger.cpp \
    @top_srcdir@/src/bluetruth_top/src/view/viewlogsink.h \
    @top_srcdir@/src/bluetruth_top/src/view/viewlogsink.cpp

model_files = \
    @top_srcdir@/src/model/model.cpp \
    @top_srcdir@/src/model/model.h \
    @top_srcdir@/src/bluetruth_top/src/model/common/activetask.h \
    @top_srcdir@/src/bluetruth_top/src/model/common/activetask.cpp \
    @top_srcdir@/src/bluetruth_top/src/model/common/circularbuffer.h \
    @top_srcdir@/src/bluetruth_top/src/model/common/circularbuffer.cpp \
    @top_srcdir@/src/bluetruth_top/src/model/common/datacontainer.h \
    @top_srcdir@/src/bluetruth_top/src/model/common/datacontainer.cpp \
    @top_srcdir@/src/bluetruth_top/src/model/common/fastdatapacket.h \
    @top_srcdir@/src/bluetruth_top/src/model/common/fastdatapacket.cpp \
    @top_srcdir@/src/bluetruth_top/src/model/common/fault.h \
    @top_srcdir@/src/bluetruth_top/src/model/common/fault.cpp \
    @top_srcdir@/src/bluetruth_top/src/model/common/itask.h \
    @top_srcdir@/src/bluetruth_top/src/model/common/itask.cpp \
    @top_srcdir@/src/bluetruth_top/src/model/common/localdeviceconfiguration.h \
    @top_srcdir@/src/bluetruth_top/src/model/common/localdeviceconfiguration.cpp \
    @top_srcdir@/src/bluetruth_top/src/model/common/localdevicerecord.h \
    @top_srcdir@/src/bluetruth_top/src/model/common/localdevicerecord.cpp \
    @top_srcdir@/src/bluetruth_top/src/model/common/losslessdatareporter.h \
    @top_srcdir@/src/bluetruth_top/src/model/common/losslessdatareporter.cpp \
    @top_srcdir@/src/bluetruth_top/src/model/common/md5_rfc1321.h \
    @top_srcdir@/src/bluetruth_top/src/model/common/md5_rfc1321.cpp \
    @top_srcdir@/src/bluetruth_top/src/model/common/queuedetector.h \
    @top_srcdir@/src/bluetruth_top/src/model/common/queuedetector.cpp \
    @top_srcdir@/src/bluetruth_top/src/model/common/remotedevicerecord.h \
    @top_srcdir@/src/bluetruth_top/src/model/common/remotedevicerecord.cpp \
    @top_srcdir@/src/bluetruth_top/src/model/common/uri.h \
    @top_srcdir@/src/bluetruth_top/src/model/common/uri.cpp \
    @top_srcdir@/src/bluetruth_top/src/model/communication/activeboostasio.h \
    @top_srcdir@/src/bluetruth_top/src/model/communication/activeboostasio.cpp \
    @top_srcdir@/src/bluetruth_top/src/model/communication/activeboostasiotcpclient.h \
    @top_srcdir@/src/bluetruth_top/src/model/communication/activeboostasiotcpclient.cpp \
    @top_srcdir@/src/bluetruth_top/src/model/communication/boostasio.h \
    @top_srcdir@/src/bluetruth_top/src/model/communication/boostasio.cpp \
    @top_srcdir@/src/bluetruth_top/src/model/communication/iconnectionconsumer.h \
    @top_srcdir@/src/bluetruth_top/src/model/communication/iconnectionconsumer.cpp \
    @top_srcdir@/src/bluetruth_top/src/model/communication/iconnectionproducerclient.h \
    @top_srcdir@/src/bluetruth_top/src/model/communication/iconnectionproducerclient.cpp \
    @top_srcdir@/src/bluetruth_top/src/model/communication/bluetooth/acquiredevicestask.h \
    @top_srcdir@/src/bluetruth_top/src/model/communication/bluetooth/acquiredevicestask.cpp \
    @top_srcdir@/src/bluetruth_top/src/model/communication/bluetooth/bluetooth_utils.h \
    @top_srcdir@/src/bluetruth_top/src/model/communication/bluetooth/bluetooth_utils.cpp \
    @top_srcdir@/src/bluetruth_top/src/model/communication/bluetooth/configurationsaver.h \
    @top_srcdir@/src/bluetruth_top/src/model/communication/bluetooth/configurationsaver.cpp \
    @top_srcdir@/src/bluetruth_top/src/model/communication/bluetooth/devicediscoverer.h \
    @top_srcdir@/src/bluetruth_top/src/model/communication/bluetooth/devicediscoverer.cpp \
    @top_srcdir@/src/bluetruth_top/src/model/communication/bluetooth/bluetoothdriver_nativebluez.h \
    @top_srcdir@/src/bluetruth_top/src/model/communication/bluetooth/bluetoothdriver_nativebluez.cpp \
    @top_srcdir@/src/bluetruth_top/src/model/communication/bluetooth/bluetoothdriver_parani.h \
    @top_srcdir@/src/bluetruth_top/src/model/communication/bluetooth/bluetoothdriver_parani.cpp \
    @top_srcdir@/src/bluetruth_top/src/model/communication/bluetooth/bluetoothdriver_rawhci.h \
    @top_srcdir@/src/bluetruth_top/src/model/communication/bluetooth/bluetoothdriver_rawhci.cpp \
    @top_srcdir@/src/bluetruth_top/src/model/communication/bluetooth/periodicallyprocessbackgrounddevicestask.h \
    @top_srcdir@/src/bluetruth_top/src/model/communication/bluetooth/periodicallyprocessbackgrounddevicestask.cpp \
    @top_srcdir@/src/bluetruth_top/src/model/communication/bluetooth/periodicallyremovenonpresentdevicestask.h \
    @top_srcdir@/src/bluetruth_top/src/model/communication/bluetooth/periodicallyremovenonpresentdevicestask.cpp \
    @top_srcdir@/src/bluetruth_top/src/model/communication/gsmmodem/activegsmmodemmonitor.h \
    @top_srcdir@/src/bluetruth_top/src/model/communication/gsmmodem/activegsmmodemmonitor.cpp \
    @top_srcdir@/src/bluetruth_top/src/model/communication/gsmmodem/gsmmodemsignallevelprocessor.h \
    @top_srcdir@/src/bluetruth_top/src/model/communication/gsmmodem/gsmmodemsignallevelprocessor.cpp \
    @top_srcdir@/src/bluetruth_top/src/model/communication/instation/ihttpclient.h \
    @top_srcdir@/src/bluetruth_top/src/model/communication/instation/ihttpclient.cpp \
    @top_srcdir@/src/bluetruth_top/src/model/communication/instation/iinstationdatacontainer.h \
    @top_srcdir@/src/bluetruth_top/src/model/communication/instation/iinstationdatacontainer.cpp \
    @top_srcdir@/src/bluetruth_top/src/model/communication/instation/iinstationreporter.h \
    @top_srcdir@/src/bluetruth_top/src/model/communication/instation/iinstationreporter.cpp \
    @top_srcdir@/src/bluetruth_top/src/model/communication/instation/instationhttpclient.h \
    @top_srcdir@/src/bluetruth_top/src/model/communication/instation/instationhttpclient.cpp \
    @top_srcdir@/src/bluetruth_top/src/model/communication/instation/instationreporter.h \
    @top_srcdir@/src/bluetruth_top/src/model/communication/instation/instationreporter.cpp \
    @top_srcdir@/src/bluetruth_top/src/model/communication/instation/isignaturegenerator.h \
    @top_srcdir@/src/bluetruth_top/src/model/communication/instation/isignaturegenerator.cpp \
    @top_srcdir@/src/bluetruth_top/src/model/communication/instation/periodicallysendcongestionreporttask.h \
    @top_srcdir@/src/bluetruth_top/src/model/communication/instation/periodicallysendcongestionreporttask.cpp \
    @top_srcdir@/src/bluetruth_top/src/model/communication/instation/periodicallysendstatisticsreporttask.h \
    @top_srcdir@/src/bluetruth_top/src/model/communication/instation/periodicallysendstatisticsreporttask.cpp \
    @top_srcdir@/src/bluetruth_top/src/model/communication/instation/periodicallysendstatusreporttask.h \
    @top_srcdir@/src/bluetruth_top/src/model/communication/instation/periodicallysendstatusreporttask.cpp \
    @top_srcdir@/src/bluetruth_top/src/model/communication/instation/retrieveconfigurationtask.h \
    @top_srcdir@/src/bluetruth_top/src/model/communication/instation/retrieveconfigurationtask.cpp \
    @top_srcdir@/src/bluetruth_top/src/model/communication/instation/signaturegenerator.h \
    @top_srcdir@/src/bluetruth_top/src/model/communication/instation/signaturegenerator.cpp \
    @top_srcdir@/src/bluetruth_top/src/model/communication/ssh/ireversesshconnector.h \
    @top_srcdir@/src/bluetruth_top/src/model/communication/ssh/ireversesshconnector.cpp \
    @top_srcdir@/src/bluetruth_top/src/model/communication/ssh/reversesshconnector.h \
    @top_srcdir@/src/bluetruth_top/src/model/communication/ssh/reversesshconnector.cpp \
    @top_srcdir@/src/bluetruth_top/src/model/configuration/coreconfiguration.h \
    @top_srcdir@/src/bluetruth_top/src/model/configuration/coreconfiguration.cpp \
    @top_srcdir@/src/bluetruth_top/src/model/configuration/icoreconfiguration.h \
    @top_srcdir@/src/bluetruth_top/src/model/configuration/icoreconfiguration.cpp \
    @top_srcdir@/src/bluetruth_top/src/model/configuration/iniconfiguration.cpp \
    @top_srcdir@/src/bluetruth_top/src/model/configuration/iniconfiguration.h \
    @top_srcdir@/src/bluetruth_top/src/model/configuration/iniparameter.cpp \
    @top_srcdir@/src/bluetruth_top/src/model/configuration/iniparameter.h \
    @top_srcdir@/src/bluetruth_top/src/model/configuration/iseedconfiguration.h \
    @top_srcdir@/src/bluetruth_top/src/model/configuration/iseedconfiguration.cpp \
    @top_srcdir@/src/bluetruth_top/src/model/configuration/seedconfiguration.h \
    @top_srcdir@/src/bluetruth_top/src/model/configuration/seedconfiguration.cpp \
    @top_srcdir@/src/bluetruth_top/src/model/configuration/xmlconfiguration.h \
    @top_srcdir@/src/bluetruth_top/src/model/configuration/xmlconfiguration.cpp \
    @top_srcdir@/src/bluetruth_top/src/model/bluetooth/lib/amp.h \
    @top_srcdir@/src/bluetruth_top/src/model/bluetooth/lib/bluetooth.c \
    @top_srcdir@/src/bluetruth_top/src/model/bluetooth/lib/bluetooth.h \
    @top_srcdir@/src/bluetruth_top/src/model/bluetooth/lib/hci.c \
    @top_srcdir@/src/bluetruth_top/src/model/bluetooth/lib/hci.h \
    @top_srcdir@/src/bluetruth_top/src/model/bluetooth/lib/hci_lib.h \
    @top_srcdir@/src/bluetruth_top/src/model/bluetooth/tools/hcidump.c \
    @top_srcdir@/src/bluetruth_top/src/model/bluetooth/tools/hcidump.h \
    @top_srcdir@/src/bluetruth_top/src/model/bluetooth/tools/parser/amp.c \
    @top_srcdir@/src/bluetruth_top/src/model/bluetooth/tools/parser/bpa.c \
    @top_srcdir@/src/bluetruth_top/src/model/bluetooth/tools/parser/csr.c \
    @top_srcdir@/src/bluetruth_top/src/model/bluetooth/tools/parser/ericsson.c \
    @top_srcdir@/src/bluetruth_top/src/model/bluetooth/tools/parser/hci_parser.c \
    @top_srcdir@/src/bluetruth_top/src/model/bluetooth/tools/parser/hci_parser.h \
    @top_srcdir@/src/bluetruth_top/src/model/bluetooth/tools/parser/lmp.c \
    @top_srcdir@/src/bluetruth_top/src/model/bluetooth/tools/parser/parser.c \
    @top_srcdir@/src/bluetruth_top/src/model/bluetooth/tools/parser/parser.h

multithreading_files = \
    @top_srcdir@/src/bluetruth_top/src/multithreading/activeobject.cpp \
    @top_srcdir@/src/bluetruth_top/src/multithreading/activeobject.h \
    @top_srcdir@/src/bluetruth_top/src/multithreading/atomicvariable.h \
    @top_srcdir@/src/bluetruth_top/src/multithreading/boostsemaphore.cpp \
    @top_srcdir@/src/bluetruth_top/src/multithreading/boostsemaphore.h \
    @top_srcdir@/src/bluetruth_top/src/multithreading/criticalsection.cpp \
    @top_srcdir@/src/bluetruth_top/src/multithreading/criticalsection.h \
    @top_srcdir@/src/bluetruth_top/src/multithreading/event.cpp \
    @top_srcdir@/src/bluetruth_top/src/multithreading/event.h \
    @top_srcdir@/src/bluetruth_top/src/multithreading/eventhandler.cpp \
    @top_srcdir@/src/bluetruth_top/src/multithreading/eventhandler.h \
    @top_srcdir@/src/bluetruth_top/src/multithreading/lock.cpp \
    @top_srcdir@/src/bluetruth_top/src/multithreading/lock.h \
    @top_srcdir@/src/bluetruth_top/src/multithreading/mutex.cpp \
    @top_srcdir@/src/bluetruth_top/src/multithreading/mutex.h \
    @top_srcdir@/src/bluetruth_top/src/multithreading/semaphore_.cpp \
    @top_srcdir@/src/bluetruth_top/src/multithreading/semaphore_.h

view_files = \
    @top_srcdir@/src/view/view.cpp \
    @top_srcdir@/src/view/view.h

lib_files = \
    @top_srcdir@/src/bluetruth_top/libs/tinyxml/tinystr.cpp \
    @top_srcdir@/src/bluetruth_top/libs/tinyxml/tinystr.h \
    @top_srcdir@/src/bluetruth_top/libs/tinyxml/tinyxml.cpp \
    @top_srcdir@/src/bluetruth_top/libs/tinyxml/tinyxml.h \
    @top_srcdir@/src/bluetruth_top/libs/tinyxml/tinyxmlerror.cpp \
    @top_srcdir@/src/bluetruth_top/libs/tinyxml/tinyxmlparser.cpp

linux_additions = \
	@top_srcdir@/linux-additions/outstation.desktop.stub \
	@top_srcdir@/linux-additions/outstation.png \
	@top_srcdir@/linux-additions/SimulationSystems.directory \
	@top_srcdir@/linux-additions/simulationsystems.menu \
	@top_srcdir@/create_project_links.sh


debian_package_files = \
	@top_srcdir@/debian/changelog \
	@top_srcdir@/debian/compat \
	@top_srcdir@/debian/control \
	@top_srcdir@/debian/copyright \
	@top_srcdir@/debian/rules \
	@top_srcdir@/debian/source/format

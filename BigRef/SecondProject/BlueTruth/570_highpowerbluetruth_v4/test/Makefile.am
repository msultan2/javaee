bin_PROGRAMS = outstation_test

ACLOCAL_AMFLAGS = -I m4

outstation_test_SOURCES = \
    $(test_files) \
    $(common_files) \
    $(controller_files) \
    $(logger_files) \
    $(model_files) \
    $(multithreading_files) \
    $(parsing_files) \
    $(lib_files) \
    $(view_files) \
    $(outstation_doc_DATA)


outstation_test_CPPFLAGS = \
    -Wall \
    -Wextra \
    -g -O0 \
    -fno-default-inline \
    -Wno-write-strings \
    -Wno-deprecated-declarations \
    -DUNICODE \
    -DTESTING \
    -DPKG_DATA_DIRECTORY=\"$(pkgdatadir)\" \
    -DSYSCONF_DIRECTORY=\"$(sysconfdir)\" \
    -DLOCAL_STATE_DIRECTORY=\"$(localstatedir)\" \
    $(outstation_test_include_files_cppflags) \
    $(BOOST_CPPFLAGS)


#Specify compilation includes and the source directories
outstation_test_include_files_cppflags = \
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

outstation_test_LDFLAGS = $(BOOST_THREAD_LDFLAGS) $(BOOST_THREAD_LIBS)
outstation_test_LDADD = -lrt -lpthread -lgtest \
	-lboost_system -lboost_chrono -lboost_date_time \
	-lboost_thread -lboost_filesystem -lcrypto -lboost_log -lboost_log_setup


#
# List of used files follows:
#

test_files = \
    @top_srcdir@/src/common/clock_test.cpp \
    @top_srcdir@/src/common/identifiable_test.cpp \
    @top_srcdir@/src/common/main_test.cpp \
    @top_srcdir@/src/common/test_observer.cpp \
    @top_srcdir@/src/common/test_observer.h \
    @top_srcdir@/src/model/common/circularbuffer_test.cpp \
    @top_srcdir@/src/model/common/datacontainer_test.cpp \
    @top_srcdir@/src/model/common/md5_rfc1321_test.cpp \
    @top_srcdir@/src/model/common/fault_test.cpp \
    @top_srcdir@/src/model/common/fastdatapacket_test.cpp \
    @top_srcdir@/src/model/common/localdevicerecord_test.cpp \
    @top_srcdir@/src/model/common/queuedetector_test.cpp \
    @top_srcdir@/src/model/common/remotedevicerecord_test.cpp \
    @top_srcdir@/src/model/common/uri_test.cpp \
    @top_srcdir@/src/model/communication/activeboostasio_test.cpp \
    @top_srcdir@/src/model/communication/activeboostasiotcpclient_test.cpp \
    @top_srcdir@/src/model/communication/test_connectionconsumer.cpp \
    @top_srcdir@/src/model/communication/test_connectionconsumer.h \
    @top_srcdir@/src/model/communication/test_connectionproducerclient.cpp \
    @top_srcdir@/src/model/communication/test_connectionproducerclient.h \
    @top_srcdir@/src/model/communication/bluetooth/bluetooth_utils_test.cpp \
    @top_srcdir@/src/model/communication/bluetooth/devicediscoverer_test.cpp \
    @top_srcdir@/src/model/communication/bluetooth/periodicallyprocessbackgrounddevicestask_test.cpp \
    @top_srcdir@/src/model/communication/bluetooth/periodicallyremovenonpresentdevicestask_test.cpp \
    @top_srcdir@/src/model/communication/instation/periodicallysendcongestionreporttask_test.cpp \
    @top_srcdir@/src/model/communication/instation/periodicallysendstatisticsreporttask_test.cpp \
    @top_srcdir@/src/model/communication/instation/periodicallysendstatusreporttask_test.cpp \
    @top_srcdir@/src/model/communication/bluetooth/test_bluetoothdriver_rawhci.cpp \
    @top_srcdir@/src/model/communication/bluetooth/test_bluetoothdriver_rawhci.h \
    @top_srcdir@/src/model/communication/gsmmodem/gsmmodemsignallevelprocessor_test.cpp \
    @top_srcdir@/src/model/communication/gsmmodem/test_gsmmodemsignallevelprocessor.cpp \
    @top_srcdir@/src/model/communication/gsmmodem/test_gsmmodemsignallevelprocessor.h \
    @top_srcdir@/src/model/communication/instation/instationhttpclient_test.cpp \
    @top_srcdir@/src/model/communication/instation/instationreporter_test.cpp \
    @top_srcdir@/src/model/communication/instation/reversesshconnector_test.cpp \
    @top_srcdir@/src/model/communication/instation/signaturegenerator_test.cpp \
    @top_srcdir@/src/model/communication/instation/test_httpclient.cpp \
    @top_srcdir@/src/model/communication/instation/test_httpclient.h \
    @top_srcdir@/src/model/communication/instation/test_instationreporter.cpp \
    @top_srcdir@/src/model/communication/instation/test_instationreporter.h \
    @top_srcdir@/src/model/communication/instation/test_reversesshconnector.cpp \
    @top_srcdir@/src/model/communication/instation/test_reversesshconnector.h \
    @top_srcdir@/src/model/communication/instation/test_signaturegenerator.cpp \
    @top_srcdir@/src/model/communication/instation/test_signaturegenerator.h \
    @top_srcdir@/src/model/configuration/coreconfiguration_test.cpp \
    @top_srcdir@/src/model/configuration/iniconfiguration_test.cpp \
    @top_srcdir@/src/model/configuration/seedconfiguration_test.cpp \
    @top_srcdir@/src/model/configuration/test_coreconfiguration.cpp \
    @top_srcdir@/src/model/configuration/test_coreconfiguration.h \
    @top_srcdir@/src/model/configuration/test_seedconfiguration.cpp \
    @top_srcdir@/src/model/configuration/test_seedconfiguration.h \
    @top_srcdir@/src/logger/boostlogger_test.cpp \
    @top_srcdir@/src/multithreading/activeobject_test.cpp \
    @top_srcdir@/src/multithreading/boostsemaphore_test.cpp \
    @top_srcdir@/src/multithreading/test_activeobject.cpp \
    @top_srcdir@/src/multithreading/test_activeobject.h \
    @top_srcdir@/src/parsing/configurationparser_test.cpp \
    @top_srcdir@/src/parsing/httpresponseparser_test.cpp \
    @top_srcdir@/src/parsing/inhandgsmmodemparser_test.cpp \
    @top_srcdir@/src/parsing/responsebodyparser_test.cpp


common_files = \
    @top_srcdir@/src/bluetruth_top/src/common/clock.cpp \
    @top_srcdir@/src/bluetruth_top/src/common/clock.h \
    @top_srcdir@/src/bluetruth_top/src/common/targetver.h \
    @top_srcdir@/src/bluetruth_top/src/common/definitions.h \
    @top_srcdir@/src/bluetruth_top/src/common/identifiable.hpp \
    @top_srcdir@/src/bluetruth_top/src/common/initialiser.h \
    @top_srcdir@/src/bluetruth_top/src/common/initialiser.cpp \
    @top_srcdir@/src/bluetruth_top/src/common/iobserver.h \
    @top_srcdir@/src/bluetruth_top/src/common/iobserver.cpp \
    @top_srcdir@/src/bluetruth_top/src/common/iobservable.h \
    @top_srcdir@/src/bluetruth_top/src/common/iobservable.cpp \
    @top_srcdir@/src/bluetruth_top/src/common/os_utilities.h \
    @top_srcdir@/src/bluetruth_top/src/common/os_utilities.cpp \
    @top_srcdir@/src/bluetruth_top/src/common/types.cpp \
    @top_srcdir@/src/bluetruth_top/src/common/types.h \
    @top_srcdir@/src/bluetruth_top/src/common/unicode_types.h \
    @top_srcdir@/src/bluetruth_top/src/common/utils.cpp \
    @top_srcdir@/src/bluetruth_top/src/common/utils.h \
    @top_srcdir@/src/common/app.cpp \
    @top_srcdir@/src/common/app.h \
    @top_srcdir@/src/common/applicationconfiguration.cpp \
    @top_srcdir@/src/common/applicationconfiguration.h \
    @top_srcdir@/src/common/stdafx.cpp \
    @top_srcdir@/src/common/stdafx.h \
    @top_srcdir@/src/common/version.cpp \
    @top_srcdir@/src/common/version.h

parsing_files = \
    @top_srcdir@/src/bluetruth_top/src/parsing/backgrounddevices_parser.yy \
    @top_srcdir@/src/bluetruth_top/src/parsing/backgrounddevices_scanner.ll \
    @top_srcdir@/src/bluetruth_top/src/parsing/configuration_parser.yy \
    @top_srcdir@/src/bluetruth_top/src/parsing/configuration_scanner.ll \
    @top_srcdir@/src/bluetruth_top/src/parsing/http_response_parser.yy \
    @top_srcdir@/src/bluetruth_top/src/parsing/http_response_scanner.ll \
    @top_srcdir@/src/bluetruth_top/src/parsing/inhand_gsm_modem_output_parser.yy \
    @top_srcdir@/src/bluetruth_top/src/parsing/inhand_gsm_modem_output_scanner.ll \
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
    @top_srcdir@/src/bluetruth_top/src/parsing/inhandgsmmodemoutputparser.cpp \
    @top_srcdir@/src/bluetruth_top/src/parsing/inhandgsmmodemoutputparser.h \
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
    @top_srcdir@/build/inhand_gsm_modem_output_scanner.h \
    @top_srcdir@/src/bluetruth_top/src/parsing/inhand_gsm_modem_output_parser.hh \
    @top_srcdir@/build/parani_output_scanner.h \
    @top_srcdir@/src/bluetruth_top/src/parsing/parani_output_parser.hh \
    @top_srcdir@/build/response_body_scanner.h \
    @top_srcdir@/src/bluetruth_top/src/parsing/response_body_parser.hh

#Delete these files in maintainer-clean mode
MAINTAINERCLEANFILES = \
    @top_srcdir@/build/backgrounddevices_scanner.h \
    @top_srcdir@/build/backgrounddevices_parser.h \
    @top_srcdir@/build/configuration_scanner.h \
    @top_srcdir@/build/configuration_parser.hh \
    @top_srcdir@/build/http_response_scanner.h \
    @top_srcdir@/build/http_response_parser.hh \
    @top_srcdir@/build/inhand_gsm_modem_output_scanner.h \
    @top_srcdir@/build/inhand_gsm_modem_output_parser.hh \
    @top_srcdir@/build/parani_output_scanner.h \
    @top_srcdir@/build/parani_output_parser.hh \
    @top_srcdir@/build/response_body_scanner.h \
    @top_srcdir@/build/response_body_parser.hh

# Recover from removal of scanner/parser headers
BUILT_SOURCES = \
    @top_srcdir@/build/backgrounddevices_scanner.h \
    @top_srcdir@/build/configuration_scanner.h \
    @top_srcdir@/build/http_response_scanner.h \
    @top_srcdir@/build/inhand_gsm_modem_output_scanner.h \
    @top_srcdir@/build/parani_output_scanner.h \
    @top_srcdir@/build/response_body_scanner.h

@top_srcdir@/build/backgrounddevices_scanner.h: @top_srcdir@/src/bluetruth_top/src/parsing/backgrounddevices_scanner.ll
	flex -t $< > /dev/null

@top_srcdir@/build/configuration_scanner.h: @top_srcdir@/src/bluetruth_top/src/parsing/configuration_scanner.ll
	flex -t $< > /dev/null

@top_srcdir@/build/http_response_scanner.h: @top_srcdir@/src/bluetruth_top/src/parsing/http_response_scanner.ll
	flex -t $< > /dev/null

@top_srcdir@/build/inhand_gsm_modem_output_scanner.h: @top_srcdir@/src/bluetruth_top/src/parsing/inhand_gsm_modem_output_scanner.ll
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
    @top_srcdir@/src/bluetruth_top/src/model/communication/gsmmodem/activegsmmodemsshmonitor.h \
    @top_srcdir@/src/bluetruth_top/src/model/communication/gsmmodem/activegsmmodemsshmonitor.cpp \
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
    @top_srcdir@/src/bluetruth_top/src/model/communication/ssh/sshclient.h \
    @top_srcdir@/src/bluetruth_top/src/model/communication/ssh/sshclient.cpp \
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
    @top_srcdir@/src/bluetruth_top/src/model/bluetooth/lib/bluetooth.c \
    @top_srcdir@/src/bluetruth_top/src/model/bluetooth/lib/bluetooth.h \
    @top_srcdir@/src/bluetruth_top/src/model/bluetooth/lib/amp.h \
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
    @top_srcdir@/src/bluetruth_top/src/multithreading/lock.cpp \
    @top_srcdir@/src/bluetruth_top/src/multithreading/lock.h \
    @top_srcdir@/src/bluetruth_top/src/multithreading/mutex.cpp \
    @top_srcdir@/src/bluetruth_top/src/multithreading/mutex.h

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


.PHONY: coverage-report coverage-report-clean coverage-gcda-clean \
        coverage-gcno-clean cov coverage-prepare run run-tests error-message

if ENABLED_COVERAGE

REPORT_DIR = coverage-report
REPORT_INFO_FILE_BASE = $(REPORT_DIR)/outstation_test.baseline
REPORT_INFO_FILE_TEST = $(REPORT_DIR)/outstation_test.test
REPORT_INFO_FILE_FINAL = $(REPORT_DIR)/outstation_test.lcov


run : all coverage-report-clean coverage-gcda-clean coverage-prepare run-tests coverage-report

run-all : all coverage-report-clean coverage-gcda-clean run-all-tests coverage-report

run-tests :
	./outstation_test --gtest_filter=*

run-all-tests :
	./outstation_test --gtest_filter=*


cov : coverage-report

coverage-prepare:
	mkdir -p $(REPORT_DIR)
#	lcov -c -i -d ../src/bluetruth_top/src -o $(REPORT_INFO_FILE_BASE)

coverage-report :
#	lcov -c -d ../src/bluetruth_top/src -o $(REPORT_INFO_FILE_TEST)
#	lcov -a $(REPORT_DIR)/outstation_test.baseline -a $(REPORT_INFO_FILE_TEST) -o $(REPORT_INFO_FILE_FINAL)
	lcov -c -d ../src/bluetruth_top/src -o $(REPORT_INFO_FILE_FINAL)
	lcov --remove $(REPORT_INFO_FILE_FINAL) "*.yy" -o $(REPORT_INFO_FILE_FINAL)
	lcov --remove $(REPORT_INFO_FILE_FINAL) "*.y" -o $(REPORT_INFO_FILE_FINAL)
	lcov --remove $(REPORT_INFO_FILE_FINAL) "*.ll" -o $(REPORT_INFO_FILE_FINAL)
	lcov --remove $(REPORT_INFO_FILE_FINAL) "*.l" -o $(REPORT_INFO_FILE_FINAL)
	lcov --remove $(REPORT_INFO_FILE_FINAL) "/usr/*" -o $(REPORT_INFO_FILE_FINAL)
	genhtml $(REPORT_INFO_FILE_FINAL) \
		--no-function-coverage \
		--legend --highlight \
		--demangle-cpp \
		--no-branch-coverage \
		--output-directory $(REPORT_DIR)

coverage-report-clean :
		rm -rf $(REPORT_DIR)

coverage-gcda-clean :
		find @top_srcdir@ -name "*.gcda" -type f -delete
		find -L @top_srcdir@/src/bluetruth_top/src -name "*.gcda" -type f -delete
		find -L @top_srcdir@/src/bluetruth_top/libs -name "*.gcda" -type f -delete

coverage-gcno-clean :
		find @top_srcdir@ -name "*.gcno" -type f -delete
		find -L @top_srcdir@/src/bluetruth_top/src -name "*.gcno" -type f -delete
		find -L @top_srcdir@/src/bluetruth_top/libs -name "*.gcno" -type f -delete

clean-local : coverage-report-clean coverage-gcda-clean coverage-gcno-clean
maintainer-clean-local : clean-local

else

cov : error-message
run : error-message
coverage-report : error-message
coverage-report-clean : error-message
coverage-gcda-clean : error-message
coverage-gcno-clean : error-message

endif

error-message :
		@echo "ERROR: the coverage option was not enabled during configuration."
		@echo "Run ./configure --enable-coverage and try again."

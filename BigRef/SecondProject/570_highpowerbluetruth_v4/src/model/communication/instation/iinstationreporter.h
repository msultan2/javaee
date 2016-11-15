/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
    Description:
    Modification History:

    Date        Who     SCJS No     Remarks
    28/11/2013  RG      001         V1.00 First Issue
*/

#ifndef I_INSTATION_REPORTER_H_
#define I_INSTATION_REPORTER_H_

#include "ihttpclient.h"


namespace InStation
{

class IInStationReporter
{
public:

    //! destructor
    virtual ~IInStationReporter();

    virtual void sendRawDeviceDetection() = 0;
    virtual void sendCongestionReport() = 0;
    virtual void sendFullStatusReport() = 0;
    virtual void sendStatusReport(const IHTTPClient::TStatusReportCollection& statusReportCollection) = 0;
    virtual void sendStatisticsReport() = 0;
    virtual void sendConfigurationRequest() = 0;
    virtual void reportFault() = 0;

protected:

    //! default constructor
    IInStationReporter();
    //! copy constructor
    IInStationReporter(const IInStationReporter& );
    //! assignment operator
    IInStationReporter& operator=(const IInStationReporter& );

};

}
//namespace

#endif //I_INSTATION_REPORTER_H_

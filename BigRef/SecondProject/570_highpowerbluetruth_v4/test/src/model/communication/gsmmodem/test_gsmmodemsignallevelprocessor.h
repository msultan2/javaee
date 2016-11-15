/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
    Description:
    Modification History:

    Date        Who     SCJS No     Remarks
    23/10/2013  RG      001         V1.00 First Issue
*/

#ifndef TEST_GSM_MODEM_PARAMETERS_H_
#define TEST_GSM_MODEM_PARAMETERS_H_

#include "gsmmodem/gsmmodemsignallevelprocessor.h"

#include "types.h"


namespace Testing
{

class TestGSMModemSignalLevelProcessor : public GSMModem::SignalLevelProcessor
{

public:

    //! default constructor
    TestGSMModemSignalLevelProcessor();

    //! destructor
    virtual ~TestGSMModemSignalLevelProcessor();


    void setSignalLevel(const int value);

private:

    //! copy constructor
    TestGSMModemSignalLevelProcessor(const TestGSMModemSignalLevelProcessor& );
    //! assignment operator
    TestGSMModemSignalLevelProcessor& operator=(const TestGSMModemSignalLevelProcessor& );
};

}

#endif //TEST_GSM_MODEM_PARAMETERS_H_

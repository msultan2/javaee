/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
    Description:
    Modification History:

    Date        Who     SCJS No     Remarks
    05/06/2010  RG      001         V1.00 First Issue

*/

#ifndef _I_SEED_CONFIGURATION_H_
#define _I_SEED_CONFIGURATION_H_


#include "types.h"
#include <string>


namespace Model
{

class ISeedConfiguration
{
public:

    //! destructor
    virtual ~ISeedConfiguration();

    virtual uint32_t getId() const = 0;
    virtual uint32_t getValue() const = 0;

    virtual bool readAllParametersFromFile(const char* fileName = 0) = 0;

protected:

    //! default constructor
    ISeedConfiguration();
    //! copy constructor. Not implemented
    ISeedConfiguration(const ISeedConfiguration& );
    //! assignment operator. Not implemented
    ISeedConfiguration& operator=(const ISeedConfiguration& );

};

}

#endif //_I_SEED_CONFIGURATION_H_

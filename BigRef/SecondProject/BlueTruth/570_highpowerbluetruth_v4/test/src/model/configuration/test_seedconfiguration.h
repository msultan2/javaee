/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
    Description: This is a class implementing ISeedConfiguration interface
        used for testing (e.g. of InStationHTTPClient).

    Modification History:

    Date        Who     SCJS No     Remarks
    21/10/2010  RG      001         V1.00 First Issue

*/

#ifndef _TEST_SEED_CONFIGURATION_H_
#define _TEST_SEED_CONFIGURATION_H_


#include "iseedconfiguration.h"


namespace Testing
{

class TestSeedConfiguration : public Model::ISeedConfiguration
{
public:

    //! default constructor
    TestSeedConfiguration();
    //! destructor
    virtual ~TestSeedConfiguration();

    virtual uint32_t getId() const;
    void setId(const uint32_t value);
    virtual uint32_t getValue() const;
    void setValue(const uint32_t value);

    virtual bool readAllParametersFromFile(const char* fileName = 0);

protected:

    //! copy constructor. Not implemented
    TestSeedConfiguration(const TestSeedConfiguration& );
    //! assignment operator. Not implemented
    TestSeedConfiguration& operator=(const TestSeedConfiguration& );

    //Private members:
    uint32_t m_id;
    uint32_t m_value;
};

}

#endif //_TEST_SEED_CONFIGURATION_H_

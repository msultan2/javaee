/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
    Description:
    Modification History:

    Date        Who     SCJS No     Remarks
    31/05/2013  RG      001         V1.00 First Issue
*/

#ifndef TEST_SIGNATURE_GENERATOR_H_
#define TEST_SIGNATURE_GENERATOR_H_

#include "instation/isignaturegenerator.h"


namespace Testing
{

class TestSignatureGenerator : public InStation::ISignatureGenerator
{


public:

    //! default constructor
    TestSignatureGenerator();

    //! destructor
    virtual ~TestSignatureGenerator();


    virtual uint32_t getNewSignature();
    void setNewSignature(const uint32_t value);

    virtual void setSeed(const uint32_t );

protected:

    //! copy constructor
    TestSignatureGenerator(const TestSignatureGenerator& );
    //! assignment operator
    TestSignatureGenerator& operator=(const TestSignatureGenerator& );

    uint32_t m_x;
};

}

#endif //TEST_SIGNATURE_GENERATOR_H_

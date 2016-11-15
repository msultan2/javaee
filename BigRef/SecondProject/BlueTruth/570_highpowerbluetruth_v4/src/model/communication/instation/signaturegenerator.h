/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
    Description:
    Modification History:

    Date        Who     SCJS No     Remarks
    31/05/2013  RG      001         V1.00 First Issue
*/

#ifndef SIGNATURE_GENERATOR_H_
#define SIGNATURE_GENERATOR_H_

#include "isignaturegenerator.h"


namespace InStation
{

class SignatureGenerator : public ISignatureGenerator
{

public:

    //! default constructor
    SignatureGenerator();

    //! destructor
    virtual ~SignatureGenerator();


    virtual uint32_t getNewSignature();

    virtual void setSeed(const uint32_t value);

protected:

    //! copy constructor
    SignatureGenerator(const SignatureGenerator& );
    //! assignment operator
    SignatureGenerator& operator=(const SignatureGenerator& );

    uint32_t m_x;
    const uint32_t m_A;
    const uint32_t m_M;
};

}

#endif //SIGNATURE_GENERATOR_H_

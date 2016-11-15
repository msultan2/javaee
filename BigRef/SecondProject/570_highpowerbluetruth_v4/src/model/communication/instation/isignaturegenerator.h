/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
    Description:
    Modification History:

    Date        Who     SCJS No     Remarks
    21/10/2013  RG      001         V1.00 First Issue
*/

#ifndef I_SIGNATURE_GENERATOR_H_
#define I_SIGNATURE_GENERATOR_H_

#include "types.h"


namespace InStation
{

class ISignatureGenerator
{

public:

    //! destructor
    virtual ~ISignatureGenerator();


    virtual uint32_t getNewSignature() = 0;

    virtual void setSeed(const uint32_t value) = 0;

protected:

    //! default constructor
    ISignatureGenerator();
    //! copy constructor
    ISignatureGenerator(const ISignatureGenerator& );
    //! assignment operator
    ISignatureGenerator& operator=(const ISignatureGenerator& );

};

}

#endif //I_SIGNATURE_GENERATOR_H_

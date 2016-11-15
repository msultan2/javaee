/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
    Description: 
    Modification History:

    Date        Who     SCJS No     Remarks
    31/05/2013  RG      001         V1.00 First Issue  
*/

#ifndef IINSTATION_DATA_CONTAINER_H_
#define IINSTATION_DATA_CONTAINER_H_

#include "types.h"

#include <boost/any.hpp>


namespace InStation
{

class IInStationDataContainer
{

public:

    //! destructor
    virtual ~IInStationDataContainer();


protected:

    //! default constructor
    IInStationDataContainer();
    //! copy constructor
    IInStationDataContainer(const IInStationDataContainer& );
    //! assignment operator
    IInStationDataContainer& operator=(const IInStationDataContainer& );

};

}

#endif //IINSTATION_DATA_CONTAINER_H_

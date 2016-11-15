#include "stdafx.h"
#include "instation/iinstationdatacontainer.h"


namespace InStation
{

IInStationDataContainer::~IInStationDataContainer()
{
    //do nothing - abstract class
}

IInStationDataContainer::IInStationDataContainer()
{
    //do nothing - abstract class
}

IInStationDataContainer::IInStationDataContainer(const IInStationDataContainer&)
{
    //do nothing - abstract class
}

IInStationDataContainer& IInStationDataContainer::operator=(const IInStationDataContainer& rhs)
{
    if (this != &rhs)
    {
     //do nothing - abstract class
    }
    else
    {
     //do nothing
    }

    return *this;
}

} //namespace

#include "stdafx.h"
#include "iobserver.h"


IObserver::~IObserver()
{
}

IObserver::IObserver()
{
}

IObserver::IObserver(const IObserver&)
{
    //do nothing - abstract class
}

IObserver& IObserver::operator=(const IObserver& rhs)
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

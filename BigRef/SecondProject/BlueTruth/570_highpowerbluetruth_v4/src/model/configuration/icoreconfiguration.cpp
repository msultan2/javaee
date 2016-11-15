#include "stdafx.h"
#include "icoreconfiguration.h"


namespace Model
{

ICoreConfiguration::~ICoreConfiguration()
{
    //do nothing - abstract class
}

ICoreConfiguration::ICoreConfiguration()
{
    //do nothing - abstract class
}

ICoreConfiguration::ICoreConfiguration(const ICoreConfiguration&)
{
    //do nothing - abstract class
}

ICoreConfiguration& ICoreConfiguration::operator=(const ICoreConfiguration& rhs)
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

#include "stdafx.h"
#include "iseedconfiguration.h"


namespace Model
{

ISeedConfiguration::~ISeedConfiguration()
{
    //do nothing - abstract class
}

ISeedConfiguration::ISeedConfiguration()
{
    //do nothing - abstract class
}

ISeedConfiguration::ISeedConfiguration(const ISeedConfiguration&)
{
    //do nothing - abstract class
}

ISeedConfiguration& ISeedConfiguration::operator=(const ISeedConfiguration& rhs)
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

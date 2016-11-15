#include "stdafx.h"
#include "ibrdfxmlconfiguration.h"


namespace Model
{

IBrdfXmlConfiguration::~IBrdfXmlConfiguration()
{
    //do nothing - abstract class
}

IBrdfXmlConfiguration::IBrdfXmlConfiguration()
{
    //do nothing - abstract class
}

IBrdfXmlConfiguration::IBrdfXmlConfiguration(const IBrdfXmlConfiguration&)
{
    //do nothing - abstract class
}

IBrdfXmlConfiguration& IBrdfXmlConfiguration::operator=(const IBrdfXmlConfiguration& rhs)
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

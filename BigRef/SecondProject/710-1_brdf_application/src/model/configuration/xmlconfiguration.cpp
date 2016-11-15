#include "stdafx.h"
#include "xmlconfiguration.h"

#include "utils.h"

#include <sstream>


namespace Model
{

XMLConfiguration::~XMLConfiguration()
{
    //do nothing
}

XMLConfiguration::XMLConfiguration()
{
    //do nothing
}

XMLConfiguration::XMLConfiguration(const XMLConfiguration&)
{
    //do nothing
}

XMLConfiguration& XMLConfiguration::operator=(const XMLConfiguration& rhs)
{
    if (this != &rhs)
    {
     //do nothing
    }
    else
    {
     //do nothing
    }

    return *this;
}

bool XMLConfiguration::checkStringElement(
    const std::string& name, const char* expectedName,
    const TiXmlNode* node,
    std::string& variable)
{
    bool found = (name == expectedName);
    if (found)
    {
        if (node->FirstChild() != 0)
        {
            const std::string VALUE(node->FirstChild()->ValueTStr().c_str());
            variable = VALUE;
        }
        //else do nothing
    }

    return found;
}

} //namespace

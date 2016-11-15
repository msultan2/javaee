/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
    Description:
    Modification History:

    Date        Who     SCJS No     Remarks
    20/10/2010  RG      001         V1.00 First Issue

*/

#ifndef _XML_CONFIGURATION_H_
#define _XML_CONFIGURATION_H_

#include <string>

#include <tinyxml/tinyxml.h>
//class TiXmlNode;

namespace Model
{

class XMLConfiguration
{
public:

    //! destructor
    virtual ~XMLConfiguration();

protected:

    //! default constructor
    XMLConfiguration();
    //! copy constructor
    XMLConfiguration(const XMLConfiguration& );
    //! assignment operator
    XMLConfiguration& operator=(const XMLConfiguration& );

    bool checkStringElement(
        const std::string& name, const char* expectedName,
        const TiXmlNode* node,
        std::string& variable);

        template<class T>
        bool checkUIntElement(
            const std::string& name, const char* expectedName,
            const TiXmlNode* node,
            T& variable,
            bool& result,
            bool* pFlag = 0);

};

} //namespace


#include "utils.h"
#include <tinyxml/tinyxml.h>

namespace Model
{

/**
 * @param pFlag pointer of the flag checking for presence of element
 * */
template<class T>
bool XMLConfiguration::checkUIntElement(
    const std::string& name, const char* expectedName,
    const TiXmlNode* node,
    T& variable,
    bool& result,
    bool* pFlag)
{
    bool found = (name == expectedName);
    if (found)
    {
        if (node->FirstChild() != 0)
        {
            const std::string VALUE(node->FirstChild()->ValueTStr().c_str());
            unsigned int tmpInt = 0;
            result = result && Utils::stringToUInt(VALUE, tmpInt);
            variable = tmpInt;

            //Mark flag as true to indicate that the parameter is there
            if (pFlag != 0)
            {
                if (!*pFlag)
                    *pFlag = true; //first time occurrence of the element
                else
                    result = false; //duplicated elemented
            }
            //else do nothing
        }
        else
        {
            result = false;
        }
    }

    return found;
}

} //namespace

#endif //_XML_CONFIGURATION_H_

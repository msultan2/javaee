/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
    Description:
    Modification History:

    Date        Who     SCJS No     Remarks
    20/10/2013  RG      001         V1.00 First Issue

 */

#ifndef _SEED_CONFIGURATION_H_
#define _SEED_CONFIGURATION_H_


#include "iseedconfiguration.h"
#include "xmlconfiguration.h"
#include "types.h"

#include <string>
#include <map>
#include <vector>

#include <tinyxml/tinyxml.h>


namespace Model
{

class SeedConfiguration :
    public ISeedConfiguration,
    public XMLConfiguration
{
public:

    //! Default constructor. This is singleton design pattern so this constructor is made private
    SeedConfiguration();

    //! Destructor
    virtual ~SeedConfiguration();

    bool isConfigurationErrorReported() const { return m_configurationErrorReported; }

    virtual void restoreDefaultValues();
    virtual bool readAllParametersFromFile(const char* fileName = 0);
    bool readAllParametersFromString(const char* str);


    //Seed configuration
    virtual uint32_t getId() const;
    virtual uint32_t getValue() const;

private:

    //! copy constructor. Not implemented
    SeedConfiguration(const SeedConfiguration& rhs);
    //! copy assignment operator. Not implemented
    SeedConfiguration& operator=(const SeedConfiguration& rhs);

    bool parseTiXmlDocument(TiXmlDocument& doc);
    bool parseTiXmlElement(TiXmlNode* pParent);
    bool updateNodes(TiXmlNode* pParent);

    void validateConfigurationFile();

    //Private members
    bool m_configurationErrorReported;

    // Configuration
    uint32_t m_id;
    uint32_t m_value;

};

} //namespace

#endif // _SEED_CONFIGURATION_H_

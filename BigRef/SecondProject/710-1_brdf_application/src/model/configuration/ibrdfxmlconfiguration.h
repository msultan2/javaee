/*
    System: BlueTruth Raw Data Feed
    Language/Build: MS VC 2008 / Linux GCC 4.2+
*/

#ifndef I_BRDF_XML_CONFIGURATION_H_
#define I_BRDF_XML_CONFIGURATION_H_


#include "types.h"
#include <string>


namespace Model
{

class IBrdfXmlConfiguration
{
public:

    //! destructor
    virtual ~IBrdfXmlConfiguration();

    virtual std::string getConfigurationVersion() const = 0;

    virtual std::string getDetectorOwner() const = 0;

    virtual std::string getDatabaseHost() const = 0;
    virtual uint64_t getDatabasePort() const = 0;
    virtual std::string getDatabasePassword() const = 0;
    virtual std::string getDatabaseUser() const = 0;

protected:

    //! default constructor
    IBrdfXmlConfiguration();
    //! copy constructor. Not implemented
    IBrdfXmlConfiguration(const IBrdfXmlConfiguration& );
    //! assignment operator. Not implemented
    IBrdfXmlConfiguration& operator=(const IBrdfXmlConfiguration& );

};

}

#endif //I_BRDF_XML_CONFIGURATION_H_

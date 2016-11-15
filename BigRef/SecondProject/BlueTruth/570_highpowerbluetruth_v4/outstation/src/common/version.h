/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
    Description: 
    Modification History:

    Date        Who     SCJS No     Remarks
    11/02/2013  RG      001         V1.00 First Issue
  
*/

#ifndef _VERSION_H_
#define _VERSION_H_

#include <string>

class Version
{
public:

    virtual ~Version();

    static std::string getVersionAsString();
    static std::string getNumber();
    static std::string getDate();
    static std::string getApplicationName();
    static std::string getApplicationNameWithoutSpaces();

private:
    // default constructor, not implemented
    Version();
    // copy constructor, not implemented
    Version(const Version& rhs);
    // copy assignment operator, not implemented
    Version& operator=(const Version& rhs); 

};

#endif //_VERSION_H_


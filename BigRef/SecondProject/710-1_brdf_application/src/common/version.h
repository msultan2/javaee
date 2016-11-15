/*
    System: BlueTruth Raw Data Feed
    Language/Build: MS VC 2008 / Linux GCC 4.2+
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


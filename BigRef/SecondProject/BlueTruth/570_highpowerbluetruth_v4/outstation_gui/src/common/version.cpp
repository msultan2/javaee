#include "stdafx.h"
#include "version.h"

#include <cassert>
#include <stddef.h>
#include <string>

namespace
{
    const char APPLICATION_NAME[] = "BlueTruth OutStation";
    const char APPLICATION_NAME_WITHOUT_SPACES[] = "BlueTruth_OutStation";

    const char MAJOR_VERSION[]  = "1";
    const char MINOR_VERSION[] = "0";
    const char BUILD_VERSION[] = "0";
    const bool THIS_IS_ALPHA_VERSION = false;

    const char DATE_STRING[]  = "05/12/13";
}

std::string Version::getVersionAsString()
{
    std::string versionString;

    versionString += "Version ";
    versionString += MAJOR_VERSION;
    versionString += ".";
    versionString += MINOR_VERSION;

    if (THIS_IS_ALPHA_VERSION)
    {
        versionString += ".";
        versionString += BUILD_VERSION;
        versionString += "alpha";
        versionString += " ";
        versionString += DATE_STRING;
    }
    // else do nothing.

    return versionString;
}

std::string Version::getNumber()
{
    std::string versionString;

    versionString += MAJOR_VERSION;
    versionString += ".";
    versionString += MINOR_VERSION;

    if (THIS_IS_ALPHA_VERSION)
    {
        versionString += ".";
        versionString += BUILD_VERSION;
        versionString += "alpha";
    }
    // else do nothing.

    return versionString;
}

std::string Version::getDate()
{
    return DATE_STRING;
}

std::string Version::getApplicationName()
{
    return APPLICATION_NAME;
}

std::string Version::getApplicationNameWithoutSpaces()
{
    return APPLICATION_NAME_WITHOUT_SPACES;
}

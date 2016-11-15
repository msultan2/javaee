#include "stdafx.h"
#include "version.h"

#include <cassert>
#include <stddef.h>
#include <string>

#include "config.h"


namespace
{
    const char APPLICATION_NAME[] = "OutStation Test Suite";
    const char APPLICATION_NAME_WITHOUT_SPACES[] = "OutStation_Test_Suite";

    const bool THIS_IS_ALPHA_VERSION = false;

    const char DATE_STRING[]  = "21/11/14";
}

std::string Version::getVersionAsString()
{
    std::string versionString;

    versionString += "Version ";
    versionString += PACKAGE_VERSION;

    if (THIS_IS_ALPHA_VERSION)
    {
        versionString += "alpha";
        versionString += " ";
        versionString += DATE_STRING;
        versionString += " (compiled: ";
        versionString += __DATE__;
        versionString += " ";
        versionString += __TIME__;
        versionString += ")";
    }
    // else do nothing.

    return versionString;
}

std::string Version::getNumber()
{
    std::string versionString;

    versionString += PACKAGE_VERSION;

    if (THIS_IS_ALPHA_VERSION)
    {
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

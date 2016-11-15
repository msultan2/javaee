#include "stdafx.h"
#include "version.h"

#include <cassert>
#include <stddef.h>
#include <string>

namespace
{
    const char APPLICATION_NAME[] = "BlueTruth Raw Data Feed";
    const char APPLICATION_NAME_WITHOUT_SPACES[] = "BlueTruth_Raw_Data_Feed";

    const char MAJOR_VERSION[]  = "1";
    const char MINOR_VERSION[] = "0";
    const bool THIS_IS_ALPHA_VERSION = false;

    const char DATE_STRING[]  = "20/03/15";
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

    versionString += MAJOR_VERSION;
    versionString += ".";
    versionString += MINOR_VERSION;

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

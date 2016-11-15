#include "stdafx.h"
#include "initialiser.h"
#include "logger.h"
#include "os_utilities.h"

//#include <direct.h>
#include <cerrno>
#include <sstream>
#include <fstream>
#include <string>


Initialiser::Initialiser(const std::vector<std::tstring>& directories)
:
m_timesInitialised(0)
{
    CreateDirectoryStructure(directories);
}


Initialiser::~Initialiser()
{
    //do nothing
};


bool Initialiser::CreateDirectoryStructure(const std::vector<std::tstring>& directories)
{
    bool result = true;
    for( std::vector<std::tstring>::const_iterator directoryIter(directories.begin()), END(directories.end());
        directoryIter != END;
        ++directoryIter)
    {
        bool tmpResult = OS_Utilities::createDirectory((*directoryIter).c_str());
        result = result && tmpResult;
    }

    return result;
}

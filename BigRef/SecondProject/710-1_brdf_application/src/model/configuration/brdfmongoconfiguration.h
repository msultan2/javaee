/*
    System: BlueTruth Raw Data Feed
    Language/Build: MS VC 2008 / Linux GCC 4.2+
*/


#ifndef BRDF_SERVER_CONFIGURATION_H
#define BRDF_SERVER_CONFIGURATION_H


#include <string>


namespace Model
{

class BrdfMongoConfiguration
{
public:

    std::string host;
    unsigned int port;
    std::string path;

    unsigned int mongoCheckPeriodInSeconds;
    unsigned int connectTimeoutInSeconds;
    unsigned int responseTimeoutInSeconds;
    unsigned int backoffTimeInSeconds;
    unsigned int numberOfRetries;
    unsigned int numberOfDetectionsPerReportLimit;

    BrdfMongoConfiguration();
};

}

#endif //BRDF_SERVER_CONFIGURATION_H

#include "brdfmongoconfiguration.h"


namespace Model
{

BrdfMongoConfiguration::BrdfMongoConfiguration()
:
host("localhost"),
port(80),
path(),
mongoCheckPeriodInSeconds(5),
connectTimeoutInSeconds(15),
responseTimeoutInSeconds(10),
backoffTimeInSeconds(10),
numberOfRetries(3),
numberOfDetectionsPerReportLimit(100)
{
    //do nothing
}

}

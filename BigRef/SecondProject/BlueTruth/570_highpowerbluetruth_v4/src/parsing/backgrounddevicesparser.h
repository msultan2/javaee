/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
    Description:
    Modification History:

    Date        Who     SCJS No     Remarks
    31/05/2013  RG      001         V1.00 First Issue
 */

#ifndef _BACKGROUND_DEVICES_PARSER_H_
#define _BACKGROUND_DEVICES_PARSER_H_

#include "types.h"

#include <vector>


struct TBackgroundDevicesItem
{
    uint64_t address;
    uint64_t firstObservationTimeUTC;
    uint64_t lastObservationTimeUTC;

    TBackgroundDevicesItem() : address(0), firstObservationTimeUTC(0), lastObservationTimeUTC(0) {}
};

struct TBackgroundDevices
{
    typedef std::vector<TBackgroundDevicesItem> TBackgroundDevicesCollection;
    TBackgroundDevicesCollection items;

    TBackgroundDevices() : items() {}

    void addItem(const TBackgroundDevicesItem& item);
    void print();
};

struct TBackgroundDevicesParserContext
{
    void* pScanner;
    int columnNo;
    TBackgroundDevices backgrounddevices;
    TBackgroundDevicesItem tmpBackgroundDevicesItem;

    TBackgroundDevicesParserContext();
    ~TBackgroundDevicesParserContext();
};

class BackgroundDevicesParser
{
public:

    virtual ~BackgroundDevicesParser();


    static bool parse(const std::string& input, TBackgroundDevicesParserContext& parsingResult);
    static bool parse(const char* input, const size_t size, TBackgroundDevicesParserContext& parsingResult);

private:
    //! default constructor, not implemented
    BackgroundDevicesParser();
    //! copy constructor, not implemented
    BackgroundDevicesParser& operator=(const BackgroundDevicesParser&);
    //! copy assignment operator, not implemented
    BackgroundDevicesParser(const BackgroundDevicesParser& rhs);
};

#endif // _BACKGROUND_DEVICES_PARSER_H_

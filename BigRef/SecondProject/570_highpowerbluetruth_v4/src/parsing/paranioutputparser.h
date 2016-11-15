/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
    Description: 
    Modification History:

    Date        Who     SCJS No     Remarks
    31/05/2013  RG      001         V1.00 First Issue  
*/

#ifndef PARANI_OUTPUT_PARSER_H_
#define PARANI_OUTPUT_PARSER_H_

#include <map>
#include <vector>
#include <string>


struct TParaniOutputBluetoothSettings
{
    std::string bdaddress;
    std::string deviceName;
    std::string operationMode;
    std::string operationStatus;
    std::string authentication;
    std::string dataEncryption;
    std::string hardwareFlowControl;
};

struct TParaniOutputRegisterEntry
{
    std::string name;
    std::string valueString;

    TParaniOutputRegisterEntry() : name(), valueString() {}
};

struct TParaniOutputInquiryResultEntry
{
    std::string address;
    std::string name;
    std::string deviceClass;

    TParaniOutputInquiryResultEntry() : address(), name(), deviceClass() {}
};

struct TParaniOutput
{
    typedef std::map<std::string, TParaniOutputRegisterEntry> TRegisterEntryCollection;
    TRegisterEntryCollection registerEntries;

    typedef std::map<std::string, TParaniOutputInquiryResultEntry> TParaniOutputInquiryResultEntryCollection;
    TParaniOutputInquiryResultEntryCollection inquiryResult;

    TParaniOutputBluetoothSettings bluetoothSettings;

    bool result;


    TParaniOutput() : registerEntries(), bluetoothSettings(), result(false) {}

    void addRegisterEntry(const TParaniOutputRegisterEntry& item);
    void addInquiryResultEntry(const TParaniOutputInquiryResultEntry& item);
    void print();
};

struct TParaniOutputContext
{
    void* pScanner;
    int columnNo;
    TParaniOutput paraniOutput;

    std::vector<std::string> tmpStringCollection;

    TParaniOutputContext();
    ~TParaniOutputContext();

    void reset();
};


class ParaniOutputParser
{
public:

    virtual ~ParaniOutputParser();


    static bool parse(const std::string& input, TParaniOutputContext& parsingResult);
#ifdef TESTING
    static void test();
#endif

private:
    //! default constructor, not implemented
    ParaniOutputParser();
    //! copy constructor, not implemented
    ParaniOutputParser& operator=(const ParaniOutputParser&);
    //! copy assignment operator, not implemented
    ParaniOutputParser(const ParaniOutputParser& rhs);

};

#endif // PARANI_OUTPUT_PARSER_H_

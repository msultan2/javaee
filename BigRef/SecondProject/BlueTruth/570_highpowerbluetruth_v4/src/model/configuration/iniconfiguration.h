/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
    Description:
    Modification History:

    Date        Who     SCJS No     Remarks
    31/05/2013  RG      001         V1.00 First Issue
*/

#ifndef _INI_CONFIGURATION_H_
#define _INI_CONFIGURATION_H_


#include "configurationparser.h"
#include "types.h"

#include "iniparameter.h"

#include <string>


namespace Model
{

class IniConfiguration
{
public:

    //! Default constructor.
    IniConfiguration();

    //! constructor from a string
    explicit IniConfiguration(const std::string& configurationText);

    //! constructor from a char string
    explicit IniConfiguration(const char* configurationText);

    //! destructor
    virtual ~IniConfiguration();

    //! assignment operator
    IniConfiguration& operator=(const IniConfiguration& );

    //! @brief Load configuration file
    //! @param useValueFromApplicationDataDirectory true - load from system configuration
    //!   directory (Unix: prefix/etc), false - load from cache directory
    //! @return true if found and loaded
    bool loadFromFile(const bool useValueFromSystemConfigurationDirectory);
    static const bool LOAD_FILE_FROM_SYSTEM_CONFIGURATION_DIRECTORY = true;
    static const bool LOAD_FILE_FROM_CACHE_DIRECTORY = false;


    //! @brief Process the configuration file/text and set all parameter values accordingly
    //! @return true on success, false on failure
    bool processConfigurationText();

    //! @brief Save ini file to cache directory
    //! @return true if ok, false otherwise
    bool saveToFile();

    bool isValid() const { return m_isValid; }
    bool isParameterErrorSet() const { return m_parameterError; }


    //! @brief get int64_t value parameter
    bool getValueInt64(const EValueTypeId valueTypeId, int64_t& value) const;

    //! @brief get bool value parameter
    bool getValueDouble(const EValueTypeId valueTypeId, double& value) const;

    //! @brief get bool value parameter
    bool getValueBool(const EValueTypeId valueTypeId, bool& value) const;

    //! @brief get string value parameter
    bool getValueString(const EValueTypeId valueTypeId, std::string& value) const;

    std::string getMD5Hash() const;

    static const char LAST_SAVED_CONFIGURATION_FILE_NAME[];
    static const char INSTALLATION_CONFIGURATION_FILE_NAME[];

#ifdef TESTING
    //! @brief set int64_t value parameter
    bool setValueInt64(const EValueTypeId valueTypeId, const int64_t value);

    //! @brief set bool value parameter
    bool setValueBool(const EValueTypeId valueTypeId, const bool value);

    //! @brief set double value parameter
    bool setValueDouble(const EValueTypeId valueTypeId, const double value);

    //! @brief set string value parameter
    bool setValueString(const EValueTypeId valueTypeId, const std::string& value);
#endif

    void printAllValuesToTheLog();

    void overwriteValuesWithCommandLineParameters(const std::string& statusReportURL);

protected:

    //! copy constructor. Not implemented
    IniConfiguration(const IniConfiguration& );

private:

    /** @brief Check if the entry with a name exists and if so assign the new value
     * @param entryName name of the entry
     * @param entryValue value to be assigned
     * @return true if value found, false if not
     */
    bool assignNewValueByName(
        const std::string& entryName,
        const TConfigurationItem& entryValue);

    /** @brief Assign a value to a parameter and perform all possible checking at this stage
     * @param value value to be assigned
     * @param pParameter parameter to which assign the value
     * @return true if value correct, false otherwise
     */
    bool assignNewIntValueWithCheck(
        const int64_t value,
        IniParameter* pParameter);

    /** @brief Assign a value to a parameter and perform all possible checking at this stage
     * @param value value to be assigned
     * @param pParameter parameter to which assign the value
     * @return true if value correct, false otherwise
     */
    bool assignNewDoubleValueWithCheck(
        const double value,
        IniParameter* pParameter);

    /** @brief Assign a value to a parameter and perform all possible checking at this stage
     * @param value value to be assigned
     * @param pParameter parameter to which assign the value
     * @return true if value correct, false otherwise
     */
    bool assignNewStringValueWithCheck(
        const std::string& value,
        IniParameter* pParameter);

    void initialiseParameterMap();

#ifndef TESTING
    //! @brief set int64_t value parameter
    bool setValueInt64(const EValueTypeId valueTypeId, const int64_t value);

    //! @brief set bool value parameter
    bool setValueBool(const EValueTypeId valueTypeId, const bool value);

    //! @brief set double value parameter
    bool setValueDouble(const EValueTypeId valueTypeId, const double value);

    //! @brief set string value parameter
    bool setValueString(const EValueTypeId valueTypeId, const std::string& value);
#endif

    TIniParameterMap m_parameterMap;
    std::string m_configurationText;

    bool m_isValid;
    bool m_parameterError;
};

}

#endif //_INI_CONFIGURATION_H_

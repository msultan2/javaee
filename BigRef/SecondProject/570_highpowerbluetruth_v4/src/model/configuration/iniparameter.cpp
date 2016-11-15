#include "stdafx.h"
#include "iniparameter.h"


namespace Model
{

IniParameter::IniParameter()
:
m_name(),
m_valueType(eUNDEFINED_VALUE_TYPE),
m_value(0),
m_minValue(0),
m_maxValue(0),
m_doubleValue(0.0),
m_minDoubleValue(0.0),
m_maxDoubleValue(0.0),
m_textValue()
{
    //do nothing
}

IniParameter::IniParameter(
    const std::string name,
    const EValueType valueType,
    const int64_t value,
    const int64_t minValue,
    const int64_t maxValue)
:
m_name(name),
m_valueType(valueType),
m_value(value),
m_minValue(minValue),
m_maxValue(maxValue),
m_doubleValue(0.0),
m_minDoubleValue(0.0),
m_maxDoubleValue(0.0),
m_textValue()
{
    //do nothing
}

IniParameter::IniParameter(
    const std::string name,
    const EValueType valueType,
    const bool value)
:
m_name(name),
m_valueType(valueType),
m_value(value?1:0),
m_minValue(0),
m_maxValue(1),
m_doubleValue(0.0),
m_minDoubleValue(0.0),
m_maxDoubleValue(0.0),
m_textValue()
{
    assert(valueType == eBool);
}

IniParameter::IniParameter(
    const std::string name,
    const EValueType valueType,
    const double value,
    const double minValue,
    const double maxValue)
:
m_name(name),
m_valueType(valueType),
m_value(0),
m_minValue(0),
m_maxValue(0),
m_doubleValue(value),
m_minDoubleValue(minValue),
m_maxDoubleValue(maxValue),
m_textValue()
{
    //do nothing
}

IniParameter::IniParameter(
    const std::string name,
    const EValueType valueType,
    const char* value)
:
m_name(name),
m_valueType(valueType),
m_value(0),
m_minValue(0),
m_maxValue(0),
m_doubleValue(0.0),
m_minDoubleValue(0.0),
m_maxDoubleValue(0.0),
m_textValue(value)
{
    //do nothing
}

IniParameter::IniParameter(
    const std::string name,
    const EValueType valueType,
    const std::string& value)
:
m_name(name),
m_valueType(valueType),
m_value(0),
m_minValue(0),
m_maxValue(0),
m_doubleValue(0.0),
m_minDoubleValue(0.0),
m_maxDoubleValue(0.0),
m_textValue(value)
{
    //do nothing
}

IniParameter::IniParameter(const IniParameter& rhs)
:
m_name(rhs.m_name),
m_valueType(rhs.m_valueType),
m_value(rhs.m_value),
m_minValue(rhs.m_minValue),
m_maxValue(rhs.m_maxValue),
m_doubleValue(rhs.m_doubleValue),
m_minDoubleValue(rhs.m_minDoubleValue),
m_maxDoubleValue(rhs.m_maxDoubleValue),
m_textValue(rhs.m_textValue)
{
    //do nothing
}

IniParameter::~IniParameter()
{
    //do nothing
}

IniParameter& IniParameter::operator=(const IniParameter& rhs)
{
    if (this != & rhs)
    {
        m_name = rhs.m_name;
        m_valueType = rhs.m_valueType;
        m_value = rhs.m_value;
        m_minValue = rhs.m_minValue;
        m_maxValue = rhs.m_maxValue;
        m_textValue = rhs.m_textValue;
        m_doubleValue = rhs.m_doubleValue;
        m_minDoubleValue = rhs.m_minDoubleValue;
        m_maxDoubleValue = rhs.m_maxDoubleValue;
    }
    else
    {
        //do nothing
    }

    return *this;
}

std::string IniParameter::getTypeAsString() const
{
    std::string result;

    switch (m_valueType)
    {
        case eInt64:
        {
            result = "Int64";
            break;
        }
        case eBool:
        {
            result = "Bool";
            break;
        }
        case eDouble:
        {
            result = "Double";
            break;
        }
        case eString:
        {
            result = "String";
            break;
        }
        default:
        {
            result = "Type Unknown";
            break;
        }
    }

    return result;
}

} //namespace

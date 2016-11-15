#include "stdafx.h"
#include "ierrordialog.h"

namespace View
{

IErrorDialog::~IErrorDialog()
{
    //do nothing - abstract class
}

IErrorDialog::IErrorDialog()
{
    //do nothing - abstract class
}

IErrorDialog::IErrorDialog(const IErrorDialog&)
{
    //do nothing - abstract class
}

IErrorDialog& IErrorDialog::operator=(const IErrorDialog& rhs)
{
    if (this != &rhs)
    {
     //do nothing - abstract class
    }
    else
    {
     //do nothing
    }

    return *this;
}

} //namespace

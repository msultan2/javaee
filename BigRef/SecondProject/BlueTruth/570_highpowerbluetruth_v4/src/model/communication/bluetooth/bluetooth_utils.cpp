#include "stdafx.h"
#include "bluetooth_utils.h"

#include <sstream>
#include <cstdlib>


namespace BlueTooth
{

std::string decodeDeviceClass(const uint32_t deviceClass)
{
//This function is based on
//https://www.bluetooth.org/en-us/specification/assigned-numbers-overview/baseband and
//http://bluetooth-pentest.narod.ru/software/bluetooth_class_of_device-service_generator.html
//In practice, most Bluetooth clients scan their surroundings in two successive steps: they first look for all bluetooth devices around them and find out their "class". You can do this on Linux with the hcitool scan command. Then, they use SDP in order to check if a device in a given class offers the type of service that they want.
//This means that the hcid.conf "class" parameter needs to be set up properly if particular services are running on the host, such as "PAN", or "OBEX Obect Push", etc: in general a device looking for a service such as "Network Access Point" will only scan for this service on devices containing "Networking" in their major service class.
//The Class of Device/Service (CoD) field has a variable format. The format is indicated using the 'Format Type field' within the CoD. The length of the Format Type field is variable and ends with two bits different from '11'. The version field starts at the least significant bit of the CoD and may extend upwards.
//In the 'format #1' of the CoD (Format Type field = 00), 11 bits are assigned as a bit-mask (multiple bits can be set) each bit corresponding to a high level generic category of service class. Currently 7 categories are defined. These are primarily of a 'public service' nature. The remaining 11 bits are used to indicate device type category and other device-specific characteristics. Any reserved but otherwise unassigned bits, such as in the Major Service Class field, should be set to 0.
//The Major and Minor classes are intended to define a general family of devices with which any particular implementation wishes to be associated. No assumptions should be made about specific functionality or characteristics of any application based solely on the assignment of the Major or Minor device class.
//The Major Class segment is the highest level of granularity for defining a Bluetooth Device. The main function of a device is used to determine the major class grouping. There are 32 different possible major classes.
//The 'Minor Device Class field' (bits 7 to 2 in the CoD), are to be interpreted only in the context of the Major Device Class (but independent of the Service Class field). Thus the meaning of the bits may change, depending on the value of the 'Major Device Class field'. When the Minor Device Class field indicates a device class, then the primary device class should be reported, e.g. a cellular phone that can also work as a cordless handset should use 'Cellular' in the minor device class field.

    static const std::string COMMA(", ");
    static const std::string SLASH("/");
    std::ostringstream ss;
    std::string insertSlashIfRequired;

    //Major Service Class
    if (deviceClass & 0x800000)
    {
        ss << insertSlashIfRequired << "Information"; //WEB-server, WAP-server etc
        insertSlashIfRequired = SLASH;
    }
    //else do nothing

    if (deviceClass & 0x400000)
    {
        ss << insertSlashIfRequired << "Telephony"; //cordless telephony, modem, headset service etc
        insertSlashIfRequired = SLASH;
    }
    //else do nothing

    if (deviceClass & 0x200000)
    {
        ss << insertSlashIfRequired << "Audio"; //speaker, microphone, headset service etc
        insertSlashIfRequired = SLASH;
    }
    //else do nothing

    if (deviceClass & 0x100000)
    {
        ss << insertSlashIfRequired << "Object Tansfer"; //v-inbox, v-folder
        insertSlashIfRequired = SLASH;
    }
    //else do nothing

    if (deviceClass & 0x80000)
    {
        ss << insertSlashIfRequired << "Capturing"; //scanner, microphone etc
        insertSlashIfRequired = SLASH;
    }
    //else do nothing

    if (deviceClass & 0x40000)
    {
        ss << insertSlashIfRequired << "Rendering"; //printing, speaker etc
        insertSlashIfRequired = SLASH;
    }
    //else do nothing

    if (deviceClass & 0x20000)
    {
        ss << insertSlashIfRequired << "Networking"; //LAN, Ad hoc etc
        insertSlashIfRequired = SLASH;
    }
    //else do nothing

    if (deviceClass & 0x10000)
    {
        ss << insertSlashIfRequired << "Positioning"; //location identification
        insertSlashIfRequired = SLASH;
    }
    //else do nothing

    if (deviceClass & 0x2000)
    {
        ss << insertSlashIfRequired << "Limited Discoverable Mode";
        insertSlashIfRequired = COMMA;
    }
    //else do nothing

    if (insertSlashIfRequired.empty())
    {
        return "Unknown Class of Device/Service";
    }
    //else continue

    ss << ", ";


    //Major Device Class
    switch (deviceClass & 0x1F00)
    {
        case 0x100:
        {
            ss << "Computer ("; //desktop, notebook, PDA, organizers

            //Minor Device Class
            uint32_t deviceClassMasked = deviceClass & 0xFF;
            if (deviceClassMasked == 0x4)
            {
                ss << "Desktop workstation";
            }
            else if (deviceClassMasked == 0x8)
            {
                ss << "Server-class computer";
            }
            else if (deviceClassMasked == 0xC)
            {
                ss << "Laptop";
            }
            else if (deviceClassMasked == 0x10)
            {
                ss << "Handheld PC/PDA (clam shell)";
            }
            else if (deviceClassMasked == 0x14)
            {
                ss << "Palm sized PC/PDA";
            }
            else if (deviceClassMasked == 0x18)
            {
                ss << "Wearable computer (watch size)";
            }
            else
            {
                ss << "Uncategorized, code for device not assigned";
            }
            //else do nothing

            ss << ")";
            break;
        }
        case 0x200:
        {
            ss << "Phone ("; //cellular, cordless, payphone, modem

            //Minor Device Class
            uint32_t deviceClassMasked = deviceClass & 0xFF;
            if (deviceClassMasked == 0x4)
            {
                ss << "Cellular";
            }
            else if (deviceClassMasked == 0x8)
            {
                ss << "Cordless";
            }
            else if (deviceClassMasked == 0xC)
            {
                ss << "Smart phone";
            }
            else if (deviceClassMasked == 0x10)
            {
                ss << "Wired modem or voice gateway";
            }
            else if (deviceClassMasked == 0x14)
            {
                ss << "Common ISDN Access";
            }
            else
            {
                ss << "Uncategorized, code for device not assigned";
            }
            //else do nothing

            ss << ")";
            break;
        }
        case 0x300:
        {
            ss << "LAN/Network Access point";
            break;
        }
        case 0x400:
        {
            ss << "Audio/Video ("; //headset, speaker, stereo, video display etc

            //Minor Device Class
            uint32_t deviceClassMasked = deviceClass & 0xFF;
            if (deviceClassMasked == 0x4)
            {
                ss << "Wearable Headset Device";
            }
            else if (deviceClassMasked == 0x8)
            {
                ss << "Hands-free Device";
            }
            else if (deviceClassMasked == 0x10)
            {
                ss << "Microphone";
            }
            else if (deviceClassMasked == 0x14)
            {
                ss << "Loudspeaker";
            }
            else if (deviceClassMasked == 0x18)
            {
                ss << "Headphones";
            }
            else if (deviceClassMasked == 0x1C)
            {
                ss << "Portable Audio";
            }
            else if (deviceClassMasked == 0x20)
            {
                ss << "Car audio";
            }
            else if (deviceClassMasked == 0x24)
            {
                ss << "Set-top box";
            }
            else if (deviceClassMasked == 0x28)
            {
                ss << "HiFi Audio Device";
            }
            else if (deviceClassMasked == 0x2C)
            {
                ss << "VCR";
            }
            else if (deviceClassMasked == 0x30)
            {
                ss << "Video Camera";
            }
            else if (deviceClassMasked == 0x34)
            {
                ss << "Camcorder";
            }
            else if (deviceClassMasked == 0x38)
            {
                ss << "Video Monitor";
            }
            else if (deviceClassMasked == 0x3C)
            {
                ss << "Video Display and Loudspeaker";
            }
            else if (deviceClassMasked == 0x40)
            {
                ss << "Video Conferencing";
            }
            else if (deviceClassMasked == 0x48)
            {
                ss << "Gaming/Toy";
            }
            else
            {
                ss << "Uncategorized, code for device not assigned";
            }
            //else do nothing

            ss << ")";
            break;
        }
        case 0x500:
        {
            ss << "Peripheral ("; //mouse, joystick, keyboards etc

            //Minor Device Class
            //keyboard/pointing device field
            uint32_t deviceClassMasked = deviceClass & 0xF0;
            if (deviceClassMasked == 0x40)
            {
                ss << "Keyboard";
            }
            else if (deviceClassMasked == 0x80)
            {
                ss << "Pointing device";
            }
            else if (deviceClassMasked == 0xC0)
            {
                ss << "Combo keyboard/pointing device";
            }
            else
            {
                ss << "Not Keyboard / Not Pointing Device";
            }
            //else do nothing

            //sub-field for the device type
            deviceClassMasked = deviceClass & 0x1F;
            if (deviceClassMasked == 0x04)
            {
                ss << ", Joystick";
            }
            else if (deviceClassMasked == 0x08)
            {
                ss << ", Gamepad";
            }
            else if (deviceClassMasked == 0x0C)
            {
                ss << ", Remote control";
            }
            else if (deviceClassMasked == 0x10)
            {
                ss << ", Sensing device";
            }
            else if (deviceClassMasked == 0x14)
            {
                ss << ", Digitizer tablet";
            }
            else if (deviceClassMasked == 0x18)
            {
                ss << ", Card Reader";
            }
            //else do nothing

            ss << ")";
            break;
        }
        case 0x600:
        {
            std::string insertCommaIfRequired;
            ss << "Imaging"; //printing, scanner, camera, display etc

            //Minor Device Class
            if (deviceClass & 0x10)
            {
                ss << insertCommaIfRequired << "Display";
                insertCommaIfRequired = COMMA;
            }
            //else do nothing

            if (deviceClass & 0x20)
            {
                ss << insertCommaIfRequired << "Camera";
                insertCommaIfRequired = COMMA;
            }
            //else do nothing

            if (deviceClass & 0x40)
            {
                ss << insertCommaIfRequired << "Scanner";
                insertCommaIfRequired = COMMA;
            }
            //else do nothing

            if (deviceClass & 0x80)
            {
                ss << insertCommaIfRequired << "Printer";
                insertCommaIfRequired = COMMA;
            }
            //else do nothing

            if (insertCommaIfRequired.empty())
            {
                ss << "Uncategorized, code for device not assigned";
            }
            //else do nothing

            ss << ")";
            break;
        }
        case 0x700:
        {
            ss << "Weareable";

            //Minor Device Class
            uint32_t deviceClassMasked = deviceClass & 0xFF;
            if (deviceClassMasked == 0x4)
            {
                ss << "Wrist Watch";
            }
            else if (deviceClassMasked == 0x8)
            {
                ss << "Pager";
            }
            else if (deviceClassMasked == 0x0C)
            {
                ss << "Jacket";
            }
            else if (deviceClassMasked == 0x10)
            {
                ss << "Helmet";
            }
            else if (deviceClassMasked == 0x14)
            {
                ss << "Glasses";
            }
            else
            {
                ss << "Uncategorized, code for device not assigned";
            }
            //else do nothing

            ss << ")";
            break;
        }
        case 0x800:
        {
            ss << "Toy (";

            //Minor Device Class
            uint32_t deviceClassMasked = deviceClass & 0xFF;
            if (deviceClassMasked == 0x4)
            {
                ss << "Robot";
            }
            else if (deviceClassMasked == 0x8)
            {
                ss << "Vehicle";
            }
            else if (deviceClassMasked == 0x0C)
            {
                ss << "Doll / Action Figure";
            }
            else if (deviceClassMasked == 0x10)
            {
                ss << "Controller";
            }
            else if (deviceClassMasked == 0x14)
            {
                ss << "Game";
            }
            else
            {
                ss << "Uncategorized, code for device not assigned";
            }
            //else do nothing

            ss << ")";
            break;
        }
        case 0x000:
        {
            ss << "Miscellaneous";
            break;
        }
        case 0x1F00:
        {
            ss << "Uncategorized, specific device code not specified";
            break;
        }
        default:
        {
            break;
        }
    }
    //else do nothing

    return ss.str();
}

}

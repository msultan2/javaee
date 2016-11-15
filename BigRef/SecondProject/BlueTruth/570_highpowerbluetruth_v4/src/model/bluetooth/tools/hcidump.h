/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
    Description:
    Modification History:

    Date        Who     SCJS No     Remarks
    13/08/2013  RG      001         V1.00 First Issue

    WARNING:
    The implementation contained in this module is GPL licensed.
    This module interacts with bluez library (www.bluez.org) which is GPL
    licensed. The source contains some code directly copied from the
    library source or associated tools source (hcidump in particular).
    To overcome the licencing issues and use this module some techniques
    have to be applied - see http://www.gnu.org/licenses/gpl-faq.html#NFUseGPLPlugins.
*/


#ifndef HCI_DUMP_H_
#define HCI_DUMP_H_

#include <stdint.h>

#ifdef __cplusplus
extern "C"
{
#endif


int open_socket(int dev);

//Copied from hcidump.c Line 83
struct btsnoop_pkt {
	uint32_t	size;		/* Original Length */
	uint32_t	len;		/* Included Length */
	uint32_t	flags;		/* Packet Flags */
	uint32_t	drops;		/* Cumulative Drops */
	uint64_t	ts;		/* Timestamp microseconds */
	uint8_t		data[0];	/* Packet Data */
} __attribute__ ((packed));
#define BTSNOOP_PKT_SIZE (sizeof(struct btsnoop_pkt))

#define SNAP_LEN	HCI_MAX_FRAME_SIZE

#ifdef __cplusplus
}
#endif

#endif //HCI_DUMP_H_

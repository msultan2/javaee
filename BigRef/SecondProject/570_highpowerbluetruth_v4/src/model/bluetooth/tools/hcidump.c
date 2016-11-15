#include "hcidump.h"

#include "bluetooth/lib/bluetooth.h"
#include "bluetooth/lib/hci.h"
#include "bluetooth/lib/hci_lib.h"

#include <errno.h>
#include <sys/ioctl.h>


//Copied from hcidump.c Line 521
int open_socket(int dev)
{
	struct sockaddr_hci addr;
	struct hci_filter flt;
	struct hci_dev_info di;
	int sk, dd, opt;

	if (dev != HCI_DEV_NONE) {
		dd = hci_open_dev(dev);
		if (dd < 0) {
			perror("Can't open device");
			return -1;
		}

		if (hci_devinfo(dev, &di) < 0) {
			perror("Can't get device info");
            hci_close_dev(dd);
			return -1;
		}

/* Commented by RG as it does not affect the program result but fails here
		opt = hci_test_bit(HCI_RAW, &di.flags);
		if (ioctl(dd, HCISETRAW, opt) < 0) {
			if (errno == EACCES) {
				perror("Can't access device");
                hci_close_dev(dd);
				return -1;
			}
		}
*/
		hci_close_dev(dd);
	}

	/* Create HCI socket */
	sk = socket(AF_BLUETOOTH, SOCK_RAW, BTPROTO_HCI);
	if (sk < 0) {
		perror("Can't create raw socket");
		return -1;
	}

	opt = 1;
	if (setsockopt(sk, SOL_HCI, HCI_DATA_DIR, &opt, sizeof(opt)) < 0) {
		perror("Can't enable data direction info");
		return -1;
	}

	opt = 1;
	if (setsockopt(sk, SOL_HCI, HCI_TIME_STAMP, &opt, sizeof(opt)) < 0) {
		perror("Can't enable time stamp");
		return -1;
	}

	/* Setup filter */
	hci_filter_clear(&flt);
	hci_filter_all_ptypes(&flt);
	hci_filter_all_events(&flt);
	if (setsockopt(sk, SOL_HCI, HCI_FILTER, &flt, sizeof(flt)) < 0) {
		perror("Can't set filter");
		return -1;
	}

	/* Bind socket to the HCI device */
	memset(&addr, 0, sizeof(addr));
	addr.hci_family = AF_BLUETOOTH;
	addr.hci_dev = dev;
	if (bind(sk, (struct sockaddr *) &addr, sizeof(addr)) < 0) {
		printf("Can't attach to device hci%d. %s(%d)\n",
					dev, strerror(errno), errno);
		return -1;
	}

	return sk;
}

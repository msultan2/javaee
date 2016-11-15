#!/usr/bin/python3

import collections
import getopt
import math
import pymongo
import re  # Regular expressions
import select
import sys
import time
import threading


PROGRAM_VERSION = "brdf_instation_simulator  Version 1.0\n" \
                  "Copyright (C) 2015 Simulation Systems Ltd."

PROGRAM_HELP = """Usage: brdf_instation_simulator OPTIONS

OPTIONS:
-o, --hostname <host name>\n\tmongo database host name
-r, --port <port number>\n\t mongo database port number
-u, --username <username>\n\tmongo database username
-p, --password <password>\n\tmongo database password
-m, --matching <value>\n\tnumber of devices matching owner group
-n, --notmatching <value>\n\tnumber of devices NOT matching owner group
-s, --seen <value>\n\tnumber of devices seen by each detector
-t, --reportPeriod <value>\n\ttime in seconds between sending reports to mongo database

"""


OWNER_TO_BE_REPORTED = 'cv'
OWNER_TO_BE_REPORTED_FULL_NAME = 'ClearView'
OWNER_NOT_TO_BE_REPORTED = 'ssl'
OWNER_NOT_TO_BE_REPORTED_FULL_NAME = 'Simulation Systems Ltd'


PROGRAM_MENU = """
Menu:
q - quit the program

d - delete all detections in database
D - delete unused detectors in database
f - fire one message
l - list all detections in database
n - show number of detections in database
N - show detection distribution in time in database
o - show owners of detectors
O - show detectors owned by \'""" + OWNER_TO_BE_REPORTED + """\'
p - pause sending data to database
r - resume sending data to database
s - show program status

Setting of variable parameters:
[mM][0-9]+ - change number of devices matching owner group, e.g. 'm2' to change to 2 devices
[nN][0-9]+ - change number of devices NOT matching owner group, e.g. 'n5' to change to 5 devices
[sS][0-9]+ - change number of devices seen by each detector,
[tT][0-9]+ - change time in seconds between sending reports, ,e.g. 't10' to change time to 10 seconds
"""


DEFAULT_DURATION = 10
DEFAULT_REF_TIME = 1
DEFAULT_END_TIME = 1


def query_yes_no(question, default="yes"):
    """Ask a yes/no question via raw_input() and return their answer.

    "question" is a string that is presented to the user.
    "default" is the presumed answer if the user just hits <Enter>.
        It must be "yes" (the default), "no" or None (meaning
        an answer is required of the user).

    The "answer" return value is True for "yes" or False for "no".
    """
    valid = {"yes": True, "y": True, "ye": True,
             "no": False, "n": False}
    if default is None:
        prompt = " [y/n] "
    elif default == "yes":
        prompt = " [Y/n] "
    elif default == "no":
        prompt = " [y/N] "
    else:
        raise ValueError("invalid default answer: '%s'" % default)

    while True:
        print(question + prompt)
        choice = input().lower()
        if default is not None and choice == '':
            return valid[default]
        elif choice in valid:
            return valid[choice]
        else:
            print("Please respond with 'yes' or 'no' "
                  "(or 'y' or 'n').\n")


class GlobalData(object):
    shouldExit = False
    numberOfDevicesMatchingOwnerGroup = 1
    numberOfDevicesNotMatchingOwnerGroup = 1
    numberOfDevicesSeenByEachDetector = 1
    timeInSecondsBetweenSendingReports = 1
    allowSendingOfData = False
    fireSingleMessage = False
    numberOfReportsSent = 0
    numberOfDetectionsReported = 0
    hostName = '127.0.0.1'
    portNumber = 27017
    userName = 'instation'
    password = 'ssl1324'


def show_program_status():
    global globalData

    if globalData.allowSendingOfData:
        print("Mode: Sending")
    else:
        print("Mode: Paused")
    print("Number of reports sent: " + str(globalData.numberOfReportsSent))
    print("Number of detections reported: " + str(globalData.numberOfDetectionsReported))

    print("Mongo:")
    print("\tHost: " + globalData.hostName)
    print("\tPort: " + str(globalData.portNumber))
    print("\tUsername: " + globalData.userName)

    print("Miscellaneous:")
    print(
        "\tNumber of devices matching owner group pattern: " +
        str(globalData.numberOfDevicesMatchingOwnerGroup) +
        " (m" + str(globalData.numberOfDevicesMatchingOwnerGroup) + ")")
    print(
        "\tNumber of devices NOT matching owner group pattern: " +
        str(globalData.numberOfDevicesNotMatchingOwnerGroup) +
        " (n" + str(globalData.numberOfDevicesNotMatchingOwnerGroup) + ")")
    print(
        "\tNumber of devices seen by each detector: " +
        str(globalData.numberOfDevicesSeenByEachDetector) +
        " (s" + str(globalData.numberOfDevicesSeenByEachDetector) + ")")
    print(
        "\tTime in seconds between sending reports: " +
        str(globalData.timeInSecondsBetweenSendingReports) +
        " (t" + str(globalData.timeInSecondsBetweenSendingReports) + ")")


def connect_to_database():
    global owners, detectors, detections

    # Connect to host
    try:
        client = pymongo.MongoClient(globalData.hostName, globalData.portNumber)
    except:
        print("ERROR: Failure to connect to mongo database")
        sys.exit(1)

    # Authenticate
    try:
        client.brdf.authenticate(globalData.userName, globalData.password, mechanism='MONGODB-CR')
    except:
        print("ERROR: Failure to authenticate")
        sys.exit(2)

    db = client.brdf
    owners = db.owners
    detectors = db.detectors
    detections = db.detections


# Check if the all records in owners collection exist. If not add them
def add_owner(owner_name, owner_full_name, send_reports=0):
    object_id = owners.find_one({"name": owner_name})
    if object_id is None:
        owner = {"name": owner_name, "full_name": owner_full_name, "send_reports": send_reports}
        owners.insert(owner)
        print("Owner " + owner_name + " was added to the owners collection")


def add_all_owners_to_database():
    add_owner(OWNER_TO_BE_REPORTED, OWNER_TO_BE_REPORTED_FULL_NAME, 1)
    add_owner(OWNER_NOT_TO_BE_REPORTED, OWNER_NOT_TO_BE_REPORTED_FULL_NAME)
    add_owner("ha", "Highways Agency")
    add_owner("void", "Void User")


# Check if the all records in detectors collection exist. If not add them
def add_detector(detector_name, owner_name, send_reports, be_verbose):
    object_id = detectors.find_one({"name": detector_name})
    if object_id is None:
        # Convert send_reports to string
        send_reports_str = "0"
        if send_reports:
            send_reports_str = "1"

        detector = {"name": detector_name, "owner": owner_name, "send_reports": send_reports_str}
        detectors.insert(detector)
        if be_verbose:  # Say what we are doing
            print("Detector " + detector_name + " was added to the detectors collection")


def redefine_detectors_and_add_them_to_database(be_verbose=True):
    global globalData, detectorSet

    # Create collection of detectors
    detector = collections.namedtuple('Detector', ['name', 'owner', 'send_reports'])
    detectorSet = []

    i_max = globalData.numberOfDevicesMatchingOwnerGroup
    j_max = globalData.numberOfDevicesNotMatchingOwnerGroup
    for i in range(0, i_max):
        detectorSet.append(detector("YES" + str(i), OWNER_TO_BE_REPORTED, True))
    for j in range(0, j_max):
        detectorSet.append(detector("NO" + str(j), OWNER_NOT_TO_BE_REPORTED, False))

    for i in range(len(detectorSet)):
        detector = detectorSet[i]
        add_detector(detector.name, detector.owner, detector.send_reports, be_verbose)


def show_number_of_detections_in_database():  # menu item 'n'
    global owners, detectors, detections

    print("Total number of detections in database: " + str(detections.count()))
    for owner in owners.find():
        owner_name = owner["name"]
        total_owner_detections = 0
        for detector in detectors.find({"owner": owner_name}):
            detector_id = detector["name"]
            total_owner_detections += detections.find({"outstationId": detector_id}).count()

        print("\'" + owner_name + "\' detections: \t" + str(total_owner_detections))


def show_timely_distribution_of_detections_in_database():  # menu item 'N'
    global owners, detectors, detections

    timeResolutionInMinutes = 30
    timeThresholdToReportInMinutes = 150
    startTimeInMinutes = 0
    endTimeInMinutes = timeResolutionInMinutes
    TAB_WIDTH = 8

    print("Distribution of detections in database: ")
    print("age          ", end="")
    for owner in owners.find():
        print(" \t" + owner["name"], end="")
    print("")

    timeThresholdInSeconds = int(time.time())  # accuracy of 1 second is fine here

    while startTimeInMinutes < timeThresholdToReportInMinutes:

        # Print time range
        firstColumnStr = str(startTimeInMinutes) + ".." + str(endTimeInMinutes) + " "
        while len(firstColumnStr) < TAB_WIDTH:
            firstColumnStr += " "
        print(firstColumnStr, end="")

        for owner in owners.find():
            owner_name = owner["name"]
            total_owner_detections = 0
            for detector in detectors.find({"owner": owner_name}):
                detector_id = detector["name"]
                total_owner_detections += detections.find({
                    "$and": [
                            {"time": {"$lt": timeThresholdInSeconds - startTimeInMinutes*60}},
                            {"time": {"$gte": timeThresholdInSeconds - endTimeInMinutes*60}},
                            {"outstationId": detector_id}
                            ]}).count()

            print(" \t" + str(total_owner_detections), end="")
        print("")
        startTimeInMinutes += timeResolutionInMinutes
        endTimeInMinutes += timeResolutionInMinutes

    # Print time range
    firstColumnStr = str(startTimeInMinutes) + "+ "
    while len(firstColumnStr) < TAB_WIDTH:
        firstColumnStr += " "
    print(firstColumnStr, end="")

    for owner in owners.find():
        owner_name = owner["name"]
        total_owner_detections = 0
        for detector in detectors.find({"owner": owner_name}):
            detector_id = detector["name"]
            total_owner_detections += detections.find({
                "$and": [
                        {"time": {"$lt": timeThresholdInSeconds - startTimeInMinutes*60}},
                        {"outstationId": detector_id}
                        ]}).count()

        print(" \t" + str(total_owner_detections), end="")
    print("")
    startTimeInMinutes += timeResolutionInMinutes
    endTimeInMinutes += timeResolutionInMinutes


    # Print records that are in the future
    print("Records with timestamp in the future:")
    firstColumnStr = ""
    while len(firstColumnStr) < TAB_WIDTH:
        firstColumnStr += " "
    print(firstColumnStr, end="")

    for owner in owners.find():
        owner_name = owner["name"]
        total_owner_detections = 0
        for detector in detectors.find({"owner": owner_name}):
            detector_id = detector["name"]
            total_owner_detections += detections.find({
                "$and": [
                        {"time": {"$gt": timeThresholdInSeconds}},
                        {"outstationId": detector_id}
                        ]}).count()

        print(" \t" + str(total_owner_detections), end="")
    print("")
    startTimeInMinutes += timeResolutionInMinutes
    endTimeInMinutes += timeResolutionInMinutes


def show_owners_of_detectors():  # menu item 'o'
    global owners, detectors

    print("Total number of detectors in database: " + str(detectors.count()))
    for owner in owners.find():
        owner_name = owner["name"]
        for detector in detectors.find({"owner": owner_name}):
            detector_id = detector["name"]
            print("detector \'" + detector_id + '\': owner \'' + owner_name + "\'")


def show_detectors_to_be_reported():  # menu item 'O'
    global owners, detectors

    owner_name = OWNER_TO_BE_REPORTED
    print("Total number of detectors owned by \'" + OWNER_TO_BE_REPORTED + "\' in database: " +
          str(detectors.find({"owner": owner_name}).count()))
    for detector in detectors.find({"owner": owner_name}):
        detector_id = detector["name"]
        print("detector \'" + detector_id + '\': owner \'' + owner_name + "\'")


# Print all detections in database
def print_all_detections_in_database():  # menu item 'l'
    global detections

    detections_found = False
    for detection in detections.find():
        print(detection)
        detections_found = True

    if not detections_found:
        print("No detections found in database")


# Remove all detections from database
def delete_all_detections_from_database():
    global detections

    if query_yes_no("Are you sure you want to delete all detections from database?", "no"):
        number_of_detections = detections.count()

        print("Removing " + str(number_of_detections) + " detections from database...")
        detections.remove()
        print(str(number_of_detections) + " detections have been removed from database")
    else:
        print("No records have been removed")


# Review all detectors from database to match m and n options
def review_all_detectors_from_database(silent=False):
    global detections

    if silent:
        detectors.remove()
        redefine_detectors_and_add_them_to_database(False)
    else:
        if query_yes_no("Are you sure you want to delete all unused detectors from database?", "no"):
            print("Removing unused detectors from database...")
            detectors.remove()
            redefine_detectors_and_add_them_to_database(False)
            print("Unused detectors have been removed from database. " + str(detectors.count()) + " detectors left.")
        else:
            print("No records have been removed")


def kbhit():
    dr = select.select([sys.stdin], [], [], 0.1)[0]
    return dr != []


def monitor_keyboard():
    global globalData

    number_of_devices_matching_owner_group_pattern = re.compile('[mM][0-9]+')
    number_of_devices_not_matching_owner_group_pattern = re.compile('[nN][0-9]+')
    number_of_devices_seen_by_each_detector_pattern = re.compile('[sS][0-9]+')
    time_in_seconds_between_sending_reports_pattern = re.compile('[tT][0-9]+')

    while True:
        if kbhit():
            text = sys.stdin.readline()

            if number_of_devices_matching_owner_group_pattern.match(text) is not None:
                globalData.numberOfDevicesMatchingOwnerGroup = int(text[1:])
                print("Number of devices matching owner group changed to " + str(
                    globalData.numberOfDevicesMatchingOwnerGroup))
                redefine_detectors_and_add_them_to_database(True)
                continue

            if number_of_devices_not_matching_owner_group_pattern.match(text) is not None:
                globalData.numberOfDevicesNotMatchingOwnerGroup = int(text[1:])
                print("Number of devices not matching owner group changed to " + str(
                    globalData.numberOfDevicesNotMatchingOwnerGroup))
                redefine_detectors_and_add_them_to_database(True)
                continue

            if number_of_devices_seen_by_each_detector_pattern.match(text) is not None:
                globalData.numberOfDevicesSeenByEachDetector = int(text[1:])
                print("Number of devices seen by each detector changed to " + str(
                    globalData.numberOfDevicesSeenByEachDetector))
                continue

            if time_in_seconds_between_sending_reports_pattern.match(text) is not None:
                globalData.timeInSecondsBetweenSendingReports = int(text[1:])
                print("Time in seconds between sending reports changed to " + str(
                    globalData.timeInSecondsBetweenSendingReports))
                continue

            if text == 'd\n':
                delete_all_detections_from_database()
                continue

            if text == 'D\n':
                review_all_detectors_from_database()
                continue

            if (text == 'f\n') or (text == 'F\n'):
                globalData.fireSingleMessage = True
                continue

            if (text == 'l\n') or (text == 'L\n'):
                print_all_detections_in_database()
                continue

            if text == 'n\n':
                show_number_of_detections_in_database()
                continue

            if text == 'N\n':
                show_timely_distribution_of_detections_in_database()
                continue

            if text == 'o\n':
                show_owners_of_detectors()
                continue

            if text == 'O\n':
                show_detectors_to_be_reported()
                continue

            if (text == 'p\n') or (text == 'P\n'):
                globalData.allowSendingOfData = False
                print("Pausing sending of data to database...")
                continue

            if (text == 'r\n') or (text == 'R\n'):
                globalData.allowSendingOfData = True
                print("Resuming sending of data to database...")
                continue

            if (text == 's\n') or (text == 'S\n'):
                show_program_status()
                continue

            if (text == '?\n') or (text == 'h\n') or (text == 'H\n'):
                print(PROGRAM_MENU)
                continue

            if (text == 'q\n') or (text == 'Q\n'):
                globalData.shouldExit = True
                print("About to exit...")

            if globalData.shouldExit:
                break

        time.sleep(0.1)


def threaded_report_detections():
    global globalData

    mac_address = 0
    last_report_posix_time = math.floor(time.time())

    if not globalData.allowSendingOfData:
        print("Sending of data to database is currently paused. Press 'r' to resume...")

    while not globalData.shouldExit:

        # Wait timeInSecondsBetweenSendingReports seconds
        # Allow dynamic check of time so that if this value is reduced we act immediately
        i = 0
        while True:
            if i > 10 * globalData.timeInSecondsBetweenSendingReports:
                break

            if globalData.fireSingleMessage:
                break

            time.sleep(0.1)
            i += 1

        current_posix_time = math.floor(time.time())

        if globalData.allowSendingOfData:
            print("Sending detections")
        elif globalData.fireSingleMessage:
            print("Sending single detections' record...")
            globalData.fireSingleMessage = False
        else:
            continue

        globalData.numberOfReportsSent += 1

        for i in range(len(detectorSet)):
            detector = detectorSet[i]
            detection_set = []
            for j in range(0, globalData.numberOfDevicesSeenByEachDetector):
                mac_address_string = str(mac_address)
                detection = {
                    'id': mac_address_string,
                    'startTime': last_report_posix_time,
                    'refTime': DEFAULT_REF_TIME,
                    'endTime': DEFAULT_END_TIME}
                mac_address += 1
                globalData.numberOfDetectionsReported += 1

                detection_set.append(detection)

            raw_data_report = {
                'outstationId': detector.name,
                'time': current_posix_time,
                'duration': str(current_posix_time - last_report_posix_time),
                'status': 'OK',
                'detections': detection_set}

            detections.insert(raw_data_report)

        last_report_posix_time = current_posix_time


if __name__ == '__main__':
    global globalData, detectorSet, owners, detectors, detections

    # Define global data so that it can be updated when parsing program options
    globalData = GlobalData
    argumentError = False

    # Extract options: host name, port number and others
    try:
        opts, args = getopt.getopt(sys.argv[1:],
                                   "Vho:r:u:p:m:n:s:t:",
                                   ["version",
                                    "help",
                                    "host=", "port=", "username=", "password=",
                                    "matching=", "notmatching=", "seen=", "reportPeriod="])
    except getopt.GetoptError:
        print(sys.argv[0] + PROGRAM_HELP)
        sys.exit(2)

    for opt, arg in opts:
        if opt in ("-V", "--version"):
            print(PROGRAM_VERSION)
            sys.exit()
        elif opt in ("-h", "--help"):
            print(PROGRAM_VERSION)
            print("")
            print(PROGRAM_HELP)
            sys.exit()
        elif opt in ("-o", "--host"):
            globalData.hostName = arg
        elif opt in ("-r", "--port"):
            globalData.portNumber = int(arg)
        elif opt in ("-u", "--username"):
            globalData.userName = arg
        elif opt in ("-p", "--password"):
            globalData.password = arg
        elif opt in ("-m", "--matching"):
            try:
                globalData.numberOfDevicesMatchingOwnerGroup = int(arg)
            except:
                print("ERROR: Invalid \'matching\' value")
                argumentError = True
        elif opt in ("-n", "--notmatching"):
            try:
                globalData.numberOfDevicesNotMatchingOwnerGroup = int(arg)
            except:
                print("ERROR: Invalid \'nonmatching\' value")
                argumentError = True
        elif opt in ("-s", "--seen"):
            try:
                globalData.numberOfDevicesSeenByEachDetector = int(arg)
            except:
                print("ERROR: Invalid \'seen\' value")
                argumentError = True
        elif opt in ("-t", "--reportPeriod"):
            try:
                globalData.timeInSecondsBetweenSendingReports = int(arg)
            except:
                print("ERROR: Invalid \'reportPeriod\' value")
                argumentError = True

    if argumentError:
        sys.exit(1)

    detectorSet = []

    connect_to_database()
    add_all_owners_to_database()
    redefine_detectors_and_add_them_to_database(True)

    # Run the keyboard monitoring function
    reportDetectionsThread = threading.Thread(target=threaded_report_detections, args=[])
    # Run the main working thread
    monitorKeyboardThread = threading.Thread(target=monitor_keyboard, args=[])

    reportDetectionsThread.start()
    monitorKeyboardThread.start()

    reportDetectionsThread.join()
    monitorKeyboardThread.join()

print("Done")

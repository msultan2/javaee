#!/usr/bin/python

import time
import BaseHTTPServer
import sys, getopt

from threading import Thread
from time import sleep

import sys
import select


PROGRAM_HELP = """\nUsage: instation_simulator.py -n <host name> -p <port number>

When using the program the following keys change action:
q - quit the program

b - add REBOOT command to the next response
c - add CLOSE SSH CONNECTION command to the next response
d - delay subsequent responses by 60 seconds
D - stop delaying subsequent responses
e - add CHANGE SEED command to the next response
f - add FLUSH BACKGROUND command to the next response
i - send invalid message body
I - send corrupted message
l - add LATCH BACKGROUND command to the next response
o - add OPEN SSH CONNECTION command to the next response (port 50000)
O - add OPEN SSH CONNECTION command to the next response (port 50001)
r - add RELOAD CONFIGURATION command to the next response
R - read configuration from the functional_configuration.conf file
s - change the next response status to 400 (BAD REQUEST)
t - add GET STATUS REPORT command to the next response
"""

HOST_NAME = 'localhost'
PORT_NUMBER = 8082

FUNCTIONAL_FILE_NAME = "functional_configuration.conf"

#Various status codes
STATUS_OK=200
STATUS_BAD_REQUEST=400


#Helper functions
def getch():
    return sys.stdin.read(1)

def kbhit():
    dr = select.select([sys.stdin], [], [], 0.1)[0]
    return dr != []


class GlobalData(object):
    status = STATUS_OK
    reloadConfiguration = False
    reboot = False
    closeSSHConnection = False
    openSSHConnection1 = False
    openSSHConnection2 = False
    changeSeed = False
    latchBackground = False
    flushBackground = False
    getStatusReport = False
    delayResponse = False
    corruptedMessage = False
    invalidMessageBody = False
    shouldExit = False
    #Use the first configuration set initially
    functionalConfigurationSet = ""

    print "Loading configuration from " + FUNCTIONAL_FILE_NAME + " file"
    try:
        configFile = open(FUNCTIONAL_FILE_NAME, "r")
        functionalConfigurationSet = configFile.read()
        print "The following configuration will be used:\n" + functionalConfigurationSet
    except IOError:
        print "Error opening file " + FUNCTIONAL_FILE_NAME


class MyHTTPHandler(BaseHTTPServer.BaseHTTPRequestHandler):

    def log_date_time_string(self):
        now = time.time()
        year, month, day, hh, mm, ss, x, y, z = time.localtime(now)
        s = "%02d/%02d/%04d %02d:%02d:%02d" % (
                day, month, year, hh, mm, ss)
        return s

    def print_request(self):
        #Print request in my format
        print "----- " + self.log_date_time_string() + ": request from " + self.client_address[0] + "\n[->] " + self.raw_requestline
        try:
            if (self.headers['Content-Length'] <> ""):
                content_length = int(self.headers['Content-Length'])
                post_data = self.rfile.read(content_length)
                print post_data
        except:
            pass


    def log_request(self, code='-', size='-'):
        pass


    def send_response(self, code, message=None):
        self.log_request(code)
        if message is None:
            if code in self.responses:
                message = self.responses[code][0]
            else:
                message = ''
        response = "%s %d %s" % (self.protocol_version, code, message)
        self.wfile.write(response + "\r\n")
        print "[<-] " + response
        self.send_header('Date', self.date_time_string())

    def send_header(self, keyword, value):
        header = "%s: %s" % (keyword, value)
        self.wfile.write(header + "\r\n")
        print "%s: %s" % (keyword, value)

    def end_headers(self):
        self.wfile.write("\r\n")
        print

    def send_body(self, body):
        self.wfile.write(body)
        print body


    def do_HEAD(s):
        s.print_request()
        s.send_response(globalData.status)
        s.send_header("Content-type", "text/plain")
        s.send_header("Content-length", "0")
        s.end_headers()


    def do_GET(s):
        s.print_request()

        if (globalData.delayResponse):
            print 'Delaying response by 60 seconds...'
            time.sleep(60) #time sleep in seconds

        #Prepare a response
        s.send_response(globalData.status)
        s.send_header("Content-type", "text/plain")
        s.send_header("Content-length", len(globalData.functionalConfigurationSet))
        s.end_headers()
        s.send_body(globalData.functionalConfigurationSet)
        if (globalData.status != STATUS_OK):
            globalData.status = STATUS_OK


    def do_POST(s):
        s.print_request()

        if (globalData.delayResponse):
            print 'Delaying response by 60 seconds...'
            time.sleep(60) #time sleep in seconds

        if (not globalData.corruptedMessage):
            #Prepare a response
            s.send_response(globalData.status)

            if (globalData.status != STATUS_OK):
                globalData.status = STATUS_OK

            contents = ""
            if (globalData.reloadConfiguration):
                if (contents != ""):
                    contents += ","
                contents += 'reloadConfiguration'
                globalData.reloadConfiguration = False
            if (globalData.reboot):
                if (contents != ""):
                    contents += ","
                contents += 'reboot'
                globalData.reboot = False
            if (globalData.closeSSHConnection):
                if (contents != ""):
                    contents += ","
                contents += 'closeSSHConnection'
                globalData.closeSSHConnection = False
            if (globalData.openSSHConnection1):
                if (contents != ""):
                    contents += ","
                contents += 'openSSHConnection:50000'
                globalData.openSSHConnection1 = False
            if (globalData.openSSHConnection2):
                if (contents != ""):
                    contents += ","
                contents += 'openSSHConnection:50001'
                globalData.openSSHConnection2 = False
            if (globalData.changeSeed):
                if (contents != ""):
                    contents += ","
                contents += 'changeSeed'
                globalData.changeSeed = False
            if (globalData.getStatusReport):
                if (contents != ""):
                    contents += ","
                contents += 'getStatusReport'
                globalData.getStatusReport = False
            if (globalData.latchBackground):
                if (contents != ""):
                    contents += ","
                contents += 'latchBackground:15'
                globalData.latchBackground = False
            if (globalData.flushBackground):
                if (contents != ""):
                    contents += ","
                contents += 'flushBackground'
                globalData.flushBackground = False
            if (globalData.invalidMessageBody):
                if (contents != ""):
                    contents += ","
                contents += 'invalid message body!'
                globalData.invalidMessageBody = False


            if (len(contents) > 0):
                s.send_header("Content-type", "text/plain")
                s.send_header("Content-length", len(contents))
            s.end_headers()
            if (len(contents) > 0):
                s.send_body(contents)

        else: #corruptedMessage == True
            #do not send status
            #s.send_response(globalData.status)
            s.send_header("Content-type", "text/plain")
            s.send_header("Content-length", 0)
            s.end_headers()

            globalData.corruptedMessage = False


def monitor_keyboard(globalData):
    while 1:
        c = -1
        if kbhit():
            c = ord(getch())

        #if (c != -1):
        #    print "Button " + str(c) + " pressed\n"


        if (c == ord('b') or c == ord('B')):
            print 'Next response will contain REBOOT command\n'
            globalData.reboot = True

        if (c == ord('c') or c == ord('C')):
            print 'Next response will contain CLOSE SSH CONNECTION command\n'
            globalData.closeSSHConnection = True

        if (c == ord('d')):
            print 'All subsequent responses will be delayed by 60 seconds\n'
            globalData.delayResponse = True

        if (c == ord('D')):
            print 'Stopping delaying responses\n'
            globalData.delayResponse = False

        if (c == ord('e') or c == ord('E')):
            print 'Next response will contain CHANGE SEED command\n'
            globalData.changeSeed = True

        if (c == ord('f') or c == ord('F')):
            print 'Next response will contain FLUSH BACKGROUND command\n'
            globalData.flushBackground = True

        if (c == ord('i')):
            print 'Next response will contain invalid message body\n'
            globalData.invalidMessageBody = True

        if (c == ord('I')):
            print 'Next response will contain corrupted message\n'
            globalData.corruptedMessage = True

        if (c == ord('l') or c == ord('L')):
            print 'Next response will contain LATCH BACKGROUND command\n'
            globalData.latchBackground = True

        if (c == ord('o')):
            print 'Next response will contain OPEN SSH CONNECTION command (port 50000)\n'
            globalData.openSSHConnection1 = True

        if (c == ord('O')):
            print 'Next response will contain OPEN SSH CONNECTION command (port 50001)\n'
            globalData.openSSHConnection2 = True

        if (c == ord('q') or c == ord('Q')):
            globalData.shouldExit = True
            httpd.shutdown()
            print 'About to exit...'
            break

        if (c == ord('r')):
            print 'Next response will contain RELOAD_CONFIGURATION command'
            globalData.reloadConfiguration = True

        if (c == ord('R')):
            print "Reloading configuration from " + FUNCTIONAL_FILE_NAME + " file\n"
            try:
                configFile = open(FUNCTIONAL_FILE_NAME, "r")
                globalData.functionalConfigurationSet = configFile.read()
                print globalData.functionalConfigurationSet
            except IOError:
                print "Error opening file " + FUNCTIONAL_FILE_NAME

        if (c == ord('s') or c == ord('S')):
            print 'Next response status will be BAD_REQUEST\n'
            globalData.status = STATUS_BAD_REQUEST

        if (c == ord('t') or c == ord('T')):
            print 'Next response will contain GET STATUS REPORT command\n'
            globalData.getStatusReport = True


        if (c == ord('?') or c == ord('h') or c == ord('H')):
            print sys.argv[0], PROGRAM_HELP

        if globalData.shouldExit:
            break


if __name__ == '__main__':
    global globalData, httpd

    #Extract options: host name and port number
    try:
        opts, args = getopt.getopt(sys.argv[1:],"hn:p:",["host=","port="])
    except getopt.GetoptError:
        print sys.argv[0], PROGRAM_HELP
        sys.exit(2)
    for opt, arg in opts:
        if opt == '-h':
            print sys.argv[0], PROGRAM_HELP
            sys.exit()
        elif opt in ("-n", "--host"):
            HOST_NAME = arg
        elif opt in ("-p", "--port"):
            PORT_NUMBER = int(arg)

    #Define reponse data structure
    globalData = GlobalData;

    #Run the keyboard monitoring function
    thread = Thread(target = monitor_keyboard, args = (globalData, ))
    thread.start()

    #Run the server
    server_class = BaseHTTPServer.HTTPServer
    httpd = server_class((HOST_NAME, PORT_NUMBER), MyHTTPHandler)
    print "{0}:  Server Starts - {1}:{2}\n".format(time.asctime(), HOST_NAME, PORT_NUMBER)
    try:
        httpd.serve_forever(0.1)
    except KeyboardInterrupt:
        pass
    httpd.server_close()
    print "{0}:  Server Stops - {1}:{2}\n".format(time.asctime(), HOST_NAME, PORT_NUMBER)
    globalData.shouldExit=True

    thread.join()
    print "Finished... Exiting\n"

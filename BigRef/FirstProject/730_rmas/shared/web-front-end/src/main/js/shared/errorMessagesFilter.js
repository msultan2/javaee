/**
 * THIS SOFTWARE IS PROVIDED BY SIMULATION SYSTEMS LTD ``AS IS'' AND
 * ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL SIMULATION
 * SYSTEMS LTD BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2016 © Simulation Systems Ltd. All Rights Reserved.
 */

'use strict';

var errorMessages = {
    "Unknown": "An unexpected error occurred. Please contact the RMAS service desk",
    "User registration successful": "User registration successful. An email will be sent to the user once the access request has been approved or rejected.",
    "User registration failed": "Unable to submit your registration application. Please try again later or contact the RMAS service desk",
    "Failed to get project sponsors": "An error occurred obtaining the project sponsors. Please try again later or contact the RMAS service desk",
    "Failed to get RCCs": "An error occurred obtaining the RCCs. Please try again later or contact the RMAS service desk",
    "User credentials invalid": "Login details are incorrect, please check your username and password.",
    "Authentication expired": "Please log in to use the RMAS service",
    "Device details updated":"Device details updated",
    "Device not registered": "The specified device is not registered in RMAS",
    "Failed to upgrade firmware on the device": "Unable to upgrade firmware on the device",
    "Failed to downgrade firmware on the device": "Unable to downgrade firmware on the device",
    "Failed to find firmware file(s) on the device": "Firmware file(s) not found on the device",
    "Failed to find pending request details":"Unable to find pending request details. Please try again later or contact the RMAS service desk",
    "The firmware is invalid": "The firmware is invalid",
    "The temporary file is not found in the respective folder": "The temporary file is not found in the respective folder",
    "The directory to upload file is not found in the device": "The directory to upload file is not found in the device",
    "Error occured while uploading the file": "Error occured while uploading the file",
    "Failed to upload file to the device": "Unable to upload file to the device",
    "Failed to upload one or more files": "Unable to upload one or more files",
    "Failed to process files":"Unable to process files",
    "Failed to connect to device": "Device is currently unreachable. Please try again later or contact the RMAS service desk if the problem persists",
    "Failed to download one or more files": "Only some of the requested files were available on the device",
    "Device is currently in use": "Device is currently in use, please try again later",
    "Device not currently visible on the network": "Device not currently visible on the network",
    "Incorrect private key": "Incorrect private key",
    "Failed to read directory on device": "Could not find the required folder on the device. Please contact the device manufacturer",
    "File(s) not found on the roadside device": "Could not find the requested file(s) on the device. Please contact the device manufacturer",
    "Error getting the log file(s)": "Requested log file(s) are not  present",
    "Failed to update the key on the device": "Unable to update the key on the device",
    "There are no files to upload":"There are no files to upload",
    "Error occured while getting the results": "Error occured while getting the results. Please try again later or contact the RMAS service desk",
    "Error processing the request": "Error processing the request. Please try again later or contact the RMAS service desk",
    "The empty file(s) will not be uploaded to the device": "The empty file(s) will not be uploaded to the device",
    "Reached maximum devices": "There are more results available than are currently displayed, please refine your search",
    "Unexpected response from device": "Unexpected response from the device.  Please contact the device manufacturer",
    "Error getting public key": "Error getting public key",
    "New SSH key pair generated successfully" : "New SSH key pair generated successfully",
    "Failed to remove old SSH public keys": "Unable to remove old SSH public keys",
    "Password reset request successful": "Password reset email has been sent",
    "Password reset request failed": "Unable to send a password reset email, please contact the RMAS service desk",
    "Password reset successful": "Password reset is successful",
    "Password reset failed": "Unable to reset your password at this time. Please try again later. If the problem persists please contact the RMAS service desk",
    "Passwords should match": "Please enter your new password twice",
    "Invalid password reset token": "Unable to reset password, please request a new password reset",
    "Invalid password": "Current password was incorrect",
    "Could not generate SSH key pair": "Could not generate SSH key pair",
    "Multiple SSH keys": "An error occurred verifying the new SSH key pair, please generate a new SSH key pair",
    "Archive too big": "Archive too big, please select fewer log files",
    "Failed to reset device": "The device could not be reset at this time. Please try again later or contact the device supplier/maintainer",
    "User group save successful": "User group save successful",
    "Failed to get account classifications": "An error occurred obtaining the account classifications. Please try again later or contact the RMAS service desk",
    "User group save failed" : "An error occurred updating the User Group. Please try again later or contact the RMAS service desk",
    "Failed to get user groups" : "An error occurred obtaining the User Groups. Please try again later or contact the RMAS service desk",
    "Duplicate value": "The entry already exists. Please change the name and try again",
    "Pending request updated successfully": "Pending request updated successfully",
    "The user is already present in the system": "The user is already present in the system",
    "Failed to send the E-mail": "Unable to send the E-mail to the user, please contact the RMAS service desk",
    "User details updated successfully": "User details updated successfully",
    "Failed to update user details": "Unable to update user details. Please try again later or contact the RMAS service desk",
    "User name updated": "User details updated successfully, user name change will show the next time you log in",
    "Password changed successfully": "Password changed successfully",
    "Password change failed": "Unable to change password. Please try again later or contact the RMAS service desk",
    "2FA verification failed": "Unable to verify token. Please try again",
    "2FA set up": "Two factor authentication is now active on this account. Please log out and in again.",
    "Failed to get 2FA details": "Unable to retrieve user details. Please try again later",
    "Failed to get new 2FA code": "Unable to generate new two factor authentication code. Please try again later",
    "Request is forbidden": "Permission to perform this operation is denied",
    "2FA User credentials invalid": "Login details are incorrect, please check your username, password and security token and try again.",
    "Device saved successfully": "Device saved successfully",
    "Failed to save the device": "Unable to save the device. Please try again later or contact the RMAS service desk",
    "Duplicate IP address": "Unable to save the device, a device with this IP address already exists",
    "Password expired": "Your password has expired, please change it before continuing",
    "Failed to process static data files": "The static data file on the device appears to be in an invalid format. Please raise with the device supplier/maintainer",
    "Last logged in": "Last logged in: {}",
    "Account classification save successful": "Account classification save successful",
    "Failed to get permissions": "An error occurred obtaining the permissions. Please try again later or contact the helpdesk",
    "User group suspended": "User group has been suspended",
    "User group enabled": "User group has been re-enabled",
    "User group deleted": "User group has been deleted",
    "User account is locked": "Your RMAS account has been locked, please contact the RMAS service desk",
    "User account is suspended": "Your RMAS account has been suspended, please contact the RMAS service desk",
    "You are not allowed to suspend your own user group": "You are not allowed to suspend your own user group",
    "You are not allowed to delete a user group that contains users": "You are not allowed to delete a user group that contains users"
};

angular.module('rmasApp.errorMessage.filter', [])

.filter('errorMessageFilter', function ($log) {

    function getMessageValue(messageKey){
        if (errorMessages.hasOwnProperty(messageKey)) {
            return errorMessages[messageKey];
        } else {
            $log.warn("Message '"+messageKey+"' not found in error message filter");
            return errorMessages.Unknown;
        }
    }

    function isSameNumberOfParameters (args, message) {
        return args.lenght === (message.match(/{}/g) || [].length);
    }

    function getMessageValueWithParameters(messageObj) {
        var message = getMessageValue(messageObj.message);
        var args = messageObj.args;
        if (angular.isArray(args)) {
            if (isSameNumberOfParameters(args, message)) {
                $log.warn("The number of parameters entered does not match the number of parameters of the message");
            }
            args.forEach(function (arg) {
                message = message.replace("{}", arg);
            });
        }
        return message;
    }

    return function (errorMessageEnum) {
        if (angular.isString(errorMessageEnum)) {
            return getMessageValue(errorMessageEnum);
        } else {
            return getMessageValueWithParameters(errorMessageEnum);
        }
    };
});

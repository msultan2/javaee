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
    "Unknown": "An unknown error occurred",
    "User registration successful": "User registration successful. An email will be sent to the user once the access request has been approved or rejected.",
    "User registration failed": "An error occurred submitting your registration application. Please try again later or contact the helpdesk",
    "Failed to get project sponsors": "An error occurred obtaining the project sponsors. Please try again later or contact the helpdesk",
    "Failed to get RCCs": "An error occurred obtaining the RCCs. Please try again later or contact the helpdesk",
    "User credentials invalid": "Login details are incorrect, please check your username and password.",
    "Authentication expired": "Authentication not recognised, please log in again",
    "Device details updated":"Device details updated",
    "Device not registered": "The specified device is not registered in RMAS",
    "Failed to upgrade firmware on the device": "Failed to upgrade firmware on the device",
    "Failed to downgrade firmware on the device": "Failed to downgrade firmware on the device",
    "Failed to find firmware file(s) on the device": "Failed to find firmware file(s) on the device",
    "Failed to find pending request details":"Failed to find pending request details",
    "The firmware is invalid": "The firmware is invalid",
    "The temporary file is not found in the respective folder": "The temporary file is not found in the respective folder",
    "The directory to upload file is not found in the device": "The directory to upload file is not found in the device",
    "Error occured while uploading the file": "Error occured while uploading the file",
    "Failed to upload file to the device": "Failed to upload file to the device",
    "Failed to upload one or more files": "Failed to upload one or more files",
    "Failed to process files":"Failed to process files",
    "Failed to connect to device": "Failed to connect to device",
    "Failed to download one or more files": "Failed to download one or more files",
    "Device is currently in use": "Device is currently in use, please try again later",
    "Device not currently visible on the network": "Device not currently visible on the network",
    "Incorrect private key": "Incorrect private key",
    "Failed to read directory on device": "Failed to read directory on device",
    "File(s) not found on the roadside device": "File(s) not found on the roadside device",
    "Error getting the log file(s)": "Requested log file(s) are not  present",
    "Failed to update the key on the device": "Failed to update the key on the device",
    "There are no files to upload":"There are no files to upload",
    "Error occured while getting the results": "Error occured while getting the results",
    "Error processing the request": "Error processing the request. Please try again later or contact the Help Desk",
    "The empty file(s) will not be uploaded to the device": "The empty file(s) will not be uploaded to the device",
    "Reached maximum devices": "There are more results available than are currently displayed, please refine your search",
    "Unexpected response from device": "Unexpected response from device",
    "Error getting public key": "Error getting public key",
    "New SSH key pair generated successfully" : "New SSH key pair generated successfully",
    "Failed to remove old SSH public keys": "Failed to remove old SSH public keys",
    "Password reset request successful": "Password reset email has been sent",
    "Password reset request failed": "Failed to send a password reset email",
    "Password reset successful": "Password reset is successful",
    "Password reset failed": "Failed to reset password, please request a new password reset",
    "Passwords should match": "Please enter your new password twice",
    "Invalid password reset token": "Failed to reset password, please request a new password reset",
    "Invalid password": "Invalid password entered",
    "Could not generate SSH key pair": "Could not generate SSH key pair",
    "Multiple SSH keys": "An error occurred verifying the new SSH key pair, please generate a new SSH key pair",
    "Archive too big": "Archive too big, please select fewer log files",
    "Failed to reset device": "Failed to reset device",
    "User group save successful": "User group save successful",
    "Failed to get account classifications": "An error occurred obtaining the account classifications. Please try again later or contact the helpdesk",
    "User group save failed" : "An error occurred updating the User Group. Please try again later or contact the Help Desk",
    "Failed to get user groups" : "An error occurred obtaining the User Groups. Please try again later or contact the helpdesk",
    "Duplicate value": "The entry already exists. Please change the name and try again",
    "Pending request updated successfully": "Pending request updated successfully",
    "The user is already present in the system": "The user is already present in the system",
    "Failed to send the E-mail": "Failed to send the E-mail to the user",
    "User details updated successfully": "User details updated successfully",
    "Failed to update user details": "Failed to update user details",
    "User name updated": "User details updated successfully, user name change will show the next time you log in",
    "Password changed successfully": "Password changed successfully",
    "Password change failed": "Password change failed",
    "2FA verification failed": "Token verification failed. Please try again",
    "2FA set up": "Two factor authentication is now active on this account. Please log out and in again.",
    "Failed to get 2FA details": "Failed to retrieve user details. Please try again later",
    "Failed to get new 2FA code": "Failed to generate new two factor authentication code. Please try again later",
    "Request is forbidden": "Permission to perform this operation is denied",
    "2FA User credentials invalid": "Login details are incorrect, please check your username, password and security token and try again.",
    "Device saved successfully": "Device saved successfully",
    "Failed to save the device": "Failed to save the device",
    "Duplicate IP address": "Failed to save the device, a device with this IP address already exists",
    "Password expired": "Your password has expired, please change it before continuing"
};

angular.module('rmasApp.errorMessage.filter', [])

.filter('errorMessageFilter', function ($log) {
    return function (errorMessageEnum) {
        if (errorMessages.hasOwnProperty(errorMessageEnum)) {
            return errorMessages[errorMessageEnum];
        } else {
            $log.warn("Message '", errorMessageEnum, "' not found in error message filter");
            return errorMessages.Unknown;
        }
    };
});

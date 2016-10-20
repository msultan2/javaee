/*
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
package com.ssl.rmas.utils;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ErrorMessage {

    UNKNOWN("Unknown"),
    FAILED_TO_CONNECT_TO_DEVICE("Failed to connect to device"),
    FAILED_TO_DOWNLOAD_ONE_OR_MORE_FILES("Failed to download one or more files"),
    ARCHIVE_TOO_BIG("Archive too big"),
    DEVICE_ALREADY_IN_USE("Device is currently in use"),
    DEVICE_IS_NOT_CURRENTLY_VISIBLE_ON_NETWORK("Device not currently visible on the network"),
    PRIVATE_KEY_USED_TO_CONNECT_TO_DEVICE_WAS_INCORRECT("Incorrect private key"),
    FAILED_TO_READ_DIRECTORY_ON_DEVICE("Failed to read directory on device"),
    FILES_NOT_FOUND_ON_ROADSIDE_DEVICE("File(s) not found on the roadside device"),
    FAILED_TO_UPDATE_KEY_ON_DEVICE("Failed to update the key on the device"),
    FAILED_TO_UPGRADE_FIRMWARE_ON_DEVICE("Failed to upgrade firmware on the device"),
    FAILED_TO_DOWNGRADE_FIRMWARE_ON_DEVICE("Failed to downgrade firmware on the device"),
    FAILED_TO_RESET_DEVICE("Failed to reset device"),
    FAILED_TO_DOWNLOAD_STATIC_DATA("Failed to download static data"),
    FAILED_TO_VERIFY_ON_DEVICE("Failed to execute verify command on the device"),
    FIRMWARE_INVALID("The firmware is invalid"),
    FIRMWARE_NOT_FOUND_ON_ROADSIDE_DEVICE("Failed to find firmware file(s) on the device"),
    TEMPORARY_FILE_NOT_FOUND("The temporary file is not found in the respective folder"),
    DEVICE_DIRECTORY_NOT_FOUND("The directory to upload file is not found in the device"),
    ERROR_WHILE_UPLOADING_FILE("Error occured while uploading the file"),
    FAILED_TO_UPLOAD_FILE("Failed to upload file to the device"),
    FAILED_TO_UPLOAD_ONE_OR_MORE_FILES("Failed to upload one or more files"),
    FAILED_TO_PROCESS_FILES("Failed to process files"),
    THERE_ARE_NO_FILES_TO_UPLOAD("There are no files to upload"),
    DEVICE_DETAILS_UPDATED("Device details updated"),
    ERROR_GETTING_PUBLIC_KEY("Error getting public key"),
    UNEXPECTED_RESPONSE("Unexpected response from device"),
    GENERIC_ERROR("Error processing the request"),
    FORBIDDEN("Request is forbidden"),
    BAD_REQUEST("Invalid request"),
    FAILED_TO_REMOVE_OLD_SSH_PUBLIC_KEYS("Failed to remove old SSH public keys"),
    INVALID_PASSWORD_RESET_TOKEN("Invalid password reset token"),
    INVALID_PASSWORD("Invalid password"),
    KEY_PAIR_GENERATION_ERROR("Could not generate SSH key pair"),
    MULTIPLE_SSH_KEYS("Multiple SSH keys"),
    ERROR_UPDATING_USER_GROUP("Failed to save user group"),
    FAILED_TO_GET_USER_GROUPS("Failed to get user groups"),
    DUPLICATE_USER("The user is already present in the system"),
    FAILED_TO_SEND_EMAIL("Failed to send the E-mail"),
    DUPLICATE_VALUE("Duplicate value");

    private final String message;

    private ErrorMessage(final String message) {
        this.message = message;
    }

    @Override
    @JsonValue
    public String toString() {
        return message;
    }

}

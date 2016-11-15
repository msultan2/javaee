/*
 * THIS SOFTWARE IS PROVIDED BY SIMULATION SYSTEMS LTD ``AS IS'' AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL SIMULATION SYSTEMS LTD BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2016 © Simulation Systems Ltd. All Rights Reserved.
 */
package com.ssl.rmas.entities;

public enum HeaderKeys {
    IP_ADDRESS("ipAddress"),
    BANDWIDTH_LIMIT("bandwidthLimit"),
    PRIVATE_KEY("privateKey"),
    CONNECTION_PARAMS("connectionParams"),
    ACTIVITY_ID("activityId"),
    FILE_PATHS("filePaths"),
    CONTENT_TYPE("Content-Type"),
    CURRENT_ACTIVITY("currentActivity"),
    HTTP_STATUS_CODE("http_statusCode"),
    HTTP_REQUEST_MODE("http_requestMethod"),
    HTTP_REQUEST_URL("http_requestUrl"),
    CURRENT_USER("spring-security-authentication"),
    MAX_DEVICES_REACHED("maxDevicesReached"),
    START_DATE("startDate"),
    END_DATE("endDate"),
    OPERATION_RESULT("operationResult"),
    EMAIL_TEXT_TEMPLATE("emailTextTemplate"),
    EMAIL_HTML_TEMPLATE("emailHTMLTemplate"),
    EMAIL_FROM_ADDRESS("emailFromAddress"),
    EMAIL_TO_ADDRESS("emailToAddress"),
    EMAIL_SUBJECT("emailSubject"),
    EMAIL_ATTACHMENT_RESOURCES("emailAttachmentResources"),
    EMAIL_INLINE_RESOURCES("emailInlineResources"),
    PEW_NUMBER("pewNumber");

    private final String key;

    private HeaderKeys(String key) {
        this.key = key;
    }

    @Override
    public String toString() {
        return key;
    }
}

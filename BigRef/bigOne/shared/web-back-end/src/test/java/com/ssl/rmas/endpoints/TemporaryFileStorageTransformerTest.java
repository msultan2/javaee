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
package com.ssl.rmas.endpoints;

import com.ssl.rmas.entities.HeaderKeys;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import static org.hamcrest.CoreMatchers.equalTo;
import org.junit.Before;
import org.junit.Test;
import org.springframework.integration.http.multipart.UploadedMultipartFile;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.util.LinkedMultiValueMap;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.springframework.integration.support.MessageBuilder;

public class TemporaryFileStorageTransformerTest {

    private Message<?> originalMessage;
    private LinkedMultiValueMap<String, UploadedMultipartFile> multipartRequest;
    private TemporaryFileStorageTransformer temporaryFileStorageTransformer;

    @Before
    public void setUp() throws IOException {
        originalMessage = getMessage();
        multipartRequest = createMultipartRequest();
        temporaryFileStorageTransformer = new TemporaryFileStorageTransformer("rait", "temp");
    }

    @Test
    public void temporaryFileStorageTransformer_shouldAddExpectedPathToTheHeader() throws Exception {
        Message<?> resultMessage = temporaryFileStorageTransformer.saveFiles(originalMessage, originalMessage.getHeaders(), multipartRequest);

        MessageHeaders actual = resultMessage.getHeaders();
        MessageHeaders expected = getExpectedHeaders(originalMessage);

        assertThat(actual.get(HeaderKeys.FILE_PATHS.toString()), is(equalTo(expected.get(HeaderKeys.FILE_PATHS.toString()))));
    }

    private Message<?> getMessage() throws IOException {
        return MessageBuilder.withPayload("")
                .setHeader(HeaderKeys.IP_ADDRESS.toString(), "10.162.49.68")
                .setHeader(HeaderKeys.BANDWIDTH_LIMIT.toString(), "32")
                .setHeader(HeaderKeys.PRIVATE_KEY.toString(), "privateKey")
                .build();
    }

    private MessageHeaders getExpectedHeaders(Message<?> original) {
        return MessageBuilder.fromMessage(original).setHeader(HeaderKeys.FILE_PATHS.toString(), getExpectedFileList()).build().getHeaders();
    }

    private List<Path> getExpectedFileList() {
        List<Path> expectedlistPath = new ArrayList<>();
        expectedlistPath.add(Paths.get(System.getProperty("user.home"), "rait", "temp", "testFile1.txt"));
        expectedlistPath.add(Paths.get(System.getProperty("user.home"), "rait", "temp", "testFile2.txt"));
        return expectedlistPath;
    }

    private LinkedMultiValueMap<String, UploadedMultipartFile> createMultipartRequest() {
        LinkedMultiValueMap<String, UploadedMultipartFile> linkedMultiValueMap = new LinkedMultiValueMap<>();

        byte[] bytes1 = {84, 101, 115, 116, 105, 110, 103, 32, 109, 117, 108, 116, 105, 112, 108, 101, 32, 102, 105, 108, 101, 32, 117, 112, 108, 111, 97, 100, 32, 102, 105, 108, 101, 32, 49, 46, 10};
        String contentType1 = "text/plain";
        String formParameterName1 = "files[1]";
        String originalFilename1 = "testFile1.txt";
        UploadedMultipartFile file1 = new UploadedMultipartFile(bytes1, contentType1, formParameterName1, originalFilename1);

        byte[] bytes2 = {84, 101, 115, 116, 105, 110, 103, 32, 109, 117, 108, 116, 105, 112, 108, 101, 32, 102, 105, 108, 101, 32, 117, 112, 108, 111, 97, 100, 32, 102, 105, 108, 101, 32, 50, 46, 10};
        String contentType2 = "text/plain";
        String formParameterName2 = "files[0]";
        String originalFilename2 = "testFile2.txt";
        UploadedMultipartFile file2 = new UploadedMultipartFile(bytes2, contentType2, formParameterName2, originalFilename2);

        linkedMultiValueMap.add("files[1]", file1);
        linkedMultiValueMap.add("files[0]", file2);
        return linkedMultiValueMap;
    }

}

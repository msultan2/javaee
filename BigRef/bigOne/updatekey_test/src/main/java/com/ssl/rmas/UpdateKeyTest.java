/*
 * THIS SOFTWARE IS PROVIDED BY COSTAIN INTEGRATED TECHNOLOGY SOLUTIONS
 * LIMITED ``AS IS'', WITH NO WARRANTY, TERM OR CONDITION OF ANY KIND,
 * EXPRESS OR IMPLIED, AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL COSTAIN
 * INTEGRATED TECHNOLOGY SOLUTIONS LIMITED BE LIABLE FOR ANY LOSSES, CLAIMS
 * OR DAMAGES OF WHATEVER NATURE, INCLUDING ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES OR LOSSES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE.
 *
 * Copyright 2016 © Costain Integrated Technology Solutions Limited.
 * All Rights Reserved.
 */
package com.ssl.rmas;

import java.io.IOException;

import org.apache.commons.io.IOUtils;

public class UpdateKeyTest {

    public static void main(String[] args) {
        String key="AAAAB3NzaC1yc2EAAAADAQABAAABAQDFojx1LxL5JNR3Vkd7Ea6SfCrnYFfXWwjus4seMjM9\n" +
                "N1pLYGjtBZfL8xe2X8dAPldrJxF/mY0+jXQOOYcsZZ2+RXPDP5qsVNw0o1taW9yO2OC/v1JM\n" +
                "WkKN0UmIhyBgvrN55wanmkxO/TWs2rn4c2IIto6mlpV04Is2RSclH2Ix9VIqrvL0belMFkR2\n" +
                "kb7763IDs2TjJ9Bwo2ApvuGGe0RaWDIZUQbG+FPnid9lDUXMoEmGsMS7i4dBTFWPfpzc7W1+\n" +
                "6W0AbfDRAWa/ZZwHKx9M9oAa9IFQBRNarZt2GjxUd2erMq/vwhSH2afKmqPXC4f2NdTVRe2K\n" +
                "NbPw/Gac3pY7";

        ProcessBuilder pb = new ProcessBuilder("./updatekey", key);
        try {
            Process process = pb.start();
            int exitCode = process.waitFor();
            System.out.println("Finished with code " + exitCode + "\n" + IOUtils.toString(process.getInputStream()));
            System.out.println("\n" + IOUtils.toString(process.getErrorStream()));
        } catch (IOException | InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}

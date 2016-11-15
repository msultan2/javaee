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
 * Copyright 2015 (C) Simulation Systems Ltd. All Rights Reserved.
 *
 * Created on 27th April 2015
 */

package com.ssl.bluetruth.emitter2converter.utils;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Test;

/**
 *
 * @author jtrujillo-brenes
 */
public class ApacheHttpComponentsIntegrationTest {
    
    @Test
    public void SimpleGet() throws IOException {
        HttpClient client = new DefaultHttpClient();
        //HttpGet request = new HttpGet("http://www.vogella.com");
        HttpGet request = new HttpGet("http://172.16.8.170:8080/674_BlueTruthReceiver2-1.00-SNAPSHOT/Statistics");
        HttpResponse response = client.execute(request);

        System.out.println("----------------------------------------");
        System.out.println(response.getStatusLine());
        System.out.println(response.toString());
        System.out.println("----------------------------------------");

        // Get the response
        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        String line;
        while ((line = rd.readLine()) != null) {
            System.out.println(line);
        }
    }
    
    @Test
    public void PostTextPlain() throws IOException {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            String url = "http://172.16.8.170:8080/674_BlueTruthReceiver2-1.00-SNAPSHOT/Statistics";
            String body = "we";            
            HttpPost httppost = new HttpPost(url);
            httppost.setEntity(new StringEntity(body, ContentType.create("plain/text", Consts.UTF_8)));
            System.out.println("RequestLine: "+httppost.getRequestLine());
            try (CloseableHttpResponse response = httpclient.execute(httppost)) {
                System.out.println("----------------------------------------");
                System.out.println("StatusLine: "+response.getStatusLine());
                System.out.println(response.toString());
                System.out.println("----------------------------------------");                
            }  
        }        
    }
    
   
    
}

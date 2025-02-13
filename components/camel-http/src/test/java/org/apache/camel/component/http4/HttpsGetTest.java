/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.http4;

import org.apache.camel.BindToRegistry;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.http4.handler.BasicValidationHandler;
import org.apache.camel.support.jsse.SSLContextParameters;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.bootstrap.HttpServer;
import org.apache.http.impl.bootstrap.ServerBootstrap;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class HttpsGetTest extends BaseHttpsTest {

    private HttpServer localServer;
    
    @BindToRegistry("x509HostnameVerifier")
    private NoopHostnameVerifier hostnameVerifier = new NoopHostnameVerifier();
    
    @BindToRegistry("sslContextParameters")
    private SSLContextParameters sslContextParameters = new SSLContextParameters();
    
    @Before
    @Override
    public void setUp() throws Exception {
        localServer = ServerBootstrap.bootstrap().
                setHttpProcessor(getBasicHttpProcessor()).
                setConnectionReuseStrategy(getConnectionReuseStrategy()).
                setResponseFactory(getHttpResponseFactory()).
                setExpectationVerifier(getHttpExpectationVerifier()).
                setSslContext(getSSLContext()).
                registerHandler("/mail/", new BasicValidationHandler("GET", null, null, getExpectedContent())).create();
        localServer.start();

        super.setUp();
    }

    @After
    @Override
    public void tearDown() throws Exception {
        super.tearDown();

        if (localServer != null) {
            localServer.stop();
        }
    }

    @Test
    public void httpsGet() throws Exception {

        Exchange exchange = template.request("https://127.0.0.1:" + localServer.getLocalPort()
                + "/mail/?x509HostnameVerifier=#x509HostnameVerifier&sslContextParameters=#sslContextParameters",
        new Processor() {
            public void process(Exchange exchange) throws Exception {
            }
        });

        assertExchange(exchange);
    }
}

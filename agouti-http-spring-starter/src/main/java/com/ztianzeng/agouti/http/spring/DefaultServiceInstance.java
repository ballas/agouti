/*
 * Copyright 2018-2019 zTianzeng Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ztianzeng.agouti.http.spring;

import com.ztianzeng.agouti.http.common.AgoutiServiceInstance;
import org.springframework.cloud.client.ServiceInstance;

import java.net.URI;

/**
 * @author zhaotianzeng
 * @version V1.0
 * @date 2019-04-18 20:04
 */
public class DefaultServiceInstance implements AgoutiServiceInstance {


    private final String host;

    private final int port;

    private final boolean secure;


    public DefaultServiceInstance(String host, int port, boolean secure) {
        this.host = host;
        this.port = port;
        this.secure = secure;

    }


    @Override
    public String getHost() {
        return host;
    }

    @Override
    public boolean isSecure() {
        return secure;
    }

    @Override
    public URI getUri() {
        return getUri(this);
    }


    public static URI getUri(AgoutiServiceInstance instance) {
        String scheme = (instance.isSecure()) ? "https" : "http";
        String uri = String.format("%s://%s:%s", scheme, instance.getHost(),
                instance.getPort());
        return URI.create(uri);
    }

    @Override
    public int getPort() {
        return port;
    }
}
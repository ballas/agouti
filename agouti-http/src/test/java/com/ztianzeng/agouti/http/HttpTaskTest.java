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

package com.ztianzeng.agouti.http;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ztianzeng.agouti.core.WorkFlow;
import com.ztianzeng.agouti.core.executor.DefaultExecutor;
import com.ztianzeng.agouti.core.parse.WorkFlowParse;
import com.ztianzeng.agouti.core.resource.AbstractResource;
import com.ztianzeng.agouti.core.resource.ClassPathResource;
import com.ztianzeng.common.tasks.Task;
import com.ztianzeng.common.workflow.WorkFlowDef;
import com.ztianzeng.common.workflow.WorkflowTaskDef;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author zhaotianzeng
 * @version V1.0
 * @date 2019-04-15 14:26
 */
@Slf4j
public class HttpTaskTest {
    private static final String ERROR_RESPONSE = "Something went wrong!";

    private static final String TEXT_RESPONSE = "Text Response";

    private static final double NUM_RESPONSE = 42.42d;

    private static String JSON_RESPONSE;

    private HttpTask httpTask = new HttpTask();


    private static Server server;

    private static ObjectMapper objectMapper = new ObjectMapper();

    @BeforeClass
    public static void init() throws Exception {

        Map<String, Object> map = new HashMap<>();
        map.put("key", "value1");
        map.put("num", 42);
        JSON_RESPONSE = objectMapper.writeValueAsString(map);

        server = new Server(7009);
        ServletContextHandler servletContextHandler = new ServletContextHandler(server, "/", ServletContextHandler.SESSIONS);
        servletContextHandler.setHandler(new EchoHandler());
        server.start();
    }


    @Test
    public void startWorkFlow() {
        DefaultExecutor defaultExecutor = new DefaultExecutor();
        WorkFlowDef workFlowDef = fromResource();

        WorkFlow workFlow = defaultExecutor.startWorkFlow(workFlowDef, null);
        Object d1Key = workFlow.getOutputs().get("d1Key");
        Assert.assertEquals(d1Key, "input_key1");
    }



    private WorkFlowDef fromResource() {
        String path = "workFlowDef.json";
        AbstractResource resource = new ClassPathResource(
                path, ClassLoader.getSystemClassLoader());

        return WorkFlowParse.parse(resource);
    }

    private WorkFlowDef fromDef() {
        WorkFlowDef workFlowDef = new WorkFlowDef();
        workFlowDef.setName("name");
        workFlowDef.setDescription("desc");

        workFlowDef.getOutputParameters().put("d1Key", "${d1.response.body.input_key1}");

        WorkflowTaskDef d1 = new WorkflowTaskDef();
        d1.setName("d1");
        d1.setType("HTTP");
        d1.setAlias("d1");

        HttpTask.Input input = new HttpTask.Input();
        input.setUrl("http://localhost:7009/post");
        Map<String, Object> body = new HashMap<>();
        body.put("input_key1", "value1");
        body.put("input_key2", 45.3d);
        input.setBody(body);
        input.setMethod("POST");

        d1.getInputParameters().put(HttpTask.REQUEST_PARAMETER_NAME, input);


        WorkflowTaskDef d2 = new WorkflowTaskDef();
        d2.setName("d2");
        d2.setType("HTTP");
        d2.setAlias("d2");

        HttpTask.Input d2In = new HttpTask.Input();
        d2In.setUrl("http://localhost:7009/post");
        Map<String, Object> d2B = new HashMap<>();
        d2B.put("B", "value1");
        d2B.put("C", 45.3d);
        d2In.setBody(d2B);
        d2In.setMethod("POST");

        d2.getInputParameters().put(HttpTask.REQUEST_PARAMETER_NAME, d2In);

        workFlowDef.getTasks().add(d1);
        workFlowDef.getTasks().add(d2);
        return workFlowDef;
    }

    @Test
    public void testPost() {
        Task task = new Task();
        HttpTask.Input input = new HttpTask.Input();
        input.setUrl("http://localhost:7009/post");

        Map<String, Object> body = new HashMap<>();
        body.put("input_key1", "value1");
        body.put("input_key2", 45.3d);
        input.setBody(body);

        input.setMethod("POST");

        task.getInputData().put(HttpTask.REQUEST_PARAMETER_NAME, input);

        httpTask.start(new WorkFlow(), task);


        Map<String, Object> hr = (Map<String, Object>) task.getOutputData().get("response");
        Object response = hr.get("body");
        assertEquals(Task.Status.COMPLETED, task.getStatus());
        assertTrue("response is: " + response, response instanceof Map);
    }


    @AfterClass
    public static void cleanup() {
        if (server != null) {
            try {
                server.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static class EchoHandler extends AbstractHandler {

        private TypeReference<Map<String, Object>> mapOfObj = new TypeReference<Map<String, Object>>() {
        };

        @Override
        public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
                throws IOException {
            if (request.getMethod().equals("GET") && request.getRequestURI().equals("/text")) {
                PrintWriter writer = response.getWriter();
                writer.print(TEXT_RESPONSE);
                writer.flush();
                writer.close();
            } else if (request.getMethod().equals("GET") && request.getRequestURI().equals("/json")) {
                response.addHeader("Content-Type", "application/json");
                PrintWriter writer = response.getWriter();
                writer.print(JSON_RESPONSE);
                writer.flush();
                writer.close();
            } else if (request.getMethod().equals("GET") && request.getRequestURI().equals("/failure")) {
                response.addHeader("Content-Type", "text/plain");
                response.setStatus(500);
                PrintWriter writer = response.getWriter();
                writer.print(ERROR_RESPONSE);
                writer.flush();
                writer.close();
            } else if (request.getMethod().equals("POST") && request.getRequestURI().equals("/post")) {
                response.addHeader("Content-Type", "application/json");
                BufferedReader reader = request.getReader();
                Map<String, Object> input = objectMapper.readValue(reader, mapOfObj);
                Set<String> keys = input.keySet();
                for (String key : keys) {
                    input.put(key, key);
                }
                PrintWriter writer = response.getWriter();
                writer.print(objectMapper.writeValueAsString(input));
                writer.flush();
                writer.close();
            } else if (request.getMethod().equals("POST") && request.getRequestURI().equals("/post2")) {
                response.addHeader("Content-Type", "application/json");
                response.setStatus(204);
                BufferedReader reader = request.getReader();
                Map<String, Object> input = objectMapper.readValue(reader, mapOfObj);
                Set<String> keys = input.keySet();
                System.out.println(keys);
                response.getWriter().close();

            } else if (request.getMethod().equals("GET") && request.getRequestURI().equals("/numeric")) {
                PrintWriter writer = response.getWriter();
                writer.print(NUM_RESPONSE);
                writer.flush();
                writer.close();
            } else if (request.getMethod().equals("POST") && request.getRequestURI().equals("/oauth")) {
                //echo back oauth parameters generated in the Authorization header in the response
                response.addHeader("Content-Type", "application/json");
                PrintWriter writer = response.getWriter();
                writer.flush();
                writer.close();
            }
        }

    }
}
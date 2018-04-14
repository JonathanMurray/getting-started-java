/**
 * Copyright 2017 Google Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.example.appengine.java8;

import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import com.google.appengine.tools.cloudstorage.RetryParams;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.util.Map;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// With @WebServlet annotation the webapp/WEB-INF/web.xml is no longer required.
@WebServlet(name = "HelloAppEngine", value = "/file")
public class HelloAppEngine extends HttpServlet {

  private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
  private final GcsUtf8Reader gcsUtf8Reader;

  //No-arg constructor is used in prod
  public HelloAppEngine() {
    this(new GcsUtf8Reader(GcsServiceFactory.createGcsService(new RetryParams.Builder()
        .initialRetryDelayMillis(10)
        .retryMaxAttempts(10)
        .totalRetryPeriodMillis(15000)
        .build())));
  }

  public HelloAppEngine(GcsUtf8Reader gcsUtf8Reader) {
    this.gcsUtf8Reader = gcsUtf8Reader;
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    try {

      response.setContentType("application/json");

      String fileName = request.getQueryString();

      String content = gcsUtf8Reader.readUtf8FromBucket("jonathan-bucket", fileName);

      System.out.println(content);
      respond(response, ImmutableMap.of("content", content));
    } catch (Exception e) {
      e.printStackTrace();
      respond(response, ImmutableMap.of("exception", e.getMessage()));
    }
  }

  private void respond(HttpServletResponse response, Map<String, String> body) throws IOException {
    response.getWriter().println(gson.toJson(body));
  }
}

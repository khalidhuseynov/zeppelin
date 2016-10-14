/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.zeppelin.rest;

import java.util.Collections;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang.StringUtils;
import org.apache.zeppelin.annotation.ZeppelinApi;
import org.apache.zeppelin.notebook.repo.NotebookRepoSync;
import org.apache.zeppelin.notebook.repo.NotebookRepoWithSettings;
import org.apache.zeppelin.rest.message.NotebookRepoSettingsRequest;
import org.apache.zeppelin.server.JsonResponse;
import org.apache.zeppelin.user.AuthenticationInfo;
import org.apache.zeppelin.utils.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * NoteRepo rest API endpoint.
 * 
 */
@Path("/notebook-repositories")
@Produces("application/json")
public class NotebookRepoRestApi {

  private static final Logger LOG = LoggerFactory.getLogger(NotebookRepoRestApi.class);

  Gson gson = new Gson();
  private NotebookRepoSync noteRepos;

  public NotebookRepoRestApi() {}
  
  public NotebookRepoRestApi(NotebookRepoSync noteRepos) {
    this.noteRepos = noteRepos;
  }

  /**
   * List all notebook repository
   */
  @GET
  @ZeppelinApi
  public Response listSettings() {
    AuthenticationInfo subject = new AuthenticationInfo(SecurityUtils.getPrincipal());
    LOG.info("Getting list of NoteRepo for user {}", subject.getUser());
    List<NotebookRepoWithSettings> settings =
        (subject.isAnonymous() ? Collections.EMPTY_LIST : noteRepos.getNotebookRepos(subject));
    return new JsonResponse<>(Status.OK, "", settings).build();
  }
  
  /**
   * Update a specific note repo.
   * 
   * @param message
   * @param settingId
   * @return
   */
  @PUT
  @ZeppelinApi
  public Response updateSetting(String payload) {
    if (StringUtils.isBlank(payload)) {
      return new JsonResponse<>(Status.NOT_FOUND, "", Collections.emptyMap()).build(); 
    }
    AuthenticationInfo subject = new AuthenticationInfo(SecurityUtils.getPrincipal());
    NotebookRepoSettingsRequest newSettings = null;
    try {
      newSettings = gson.fromJson(payload, NotebookRepoSettingsRequest.class);
    } catch (JsonSyntaxException e) {
      LOG.error("Cannot update notebook repo settings", e);
      return new JsonResponse<>(Status.NOT_ACCEPTABLE, "",
                                ImmutableMap.of("error", "Invalid payload structure")).build();
    }
    
    if (newSettings == null) {
      LOG.error("Invalid property");
      return new JsonResponse<>(Status.NOT_ACCEPTABLE, "",
                                ImmutableMap.of("error", "Invalid payload")).build();
    }
    LOG.info("User {} is going to change repo setting", subject.getUser());
    noteRepos.updateNotebookRepo(newSettings.name, newSettings.settings, subject);
    return new JsonResponse<>(Status.OK, "", Maps.newHashMap()).build();
  }
  
}

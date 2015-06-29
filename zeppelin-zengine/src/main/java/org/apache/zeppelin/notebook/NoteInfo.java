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

package org.apache.zeppelin.notebook;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class NoteInfo {
  String id;
  String name;
  long modTime;
  private Map<String, Object> config = new HashMap<String, Object>();

  public NoteInfo(String id, String name, Map<String, Object> config, long timestamp) {
    super();
    this.id = id;
    this.name = name;
    this.config = config;
    this.modTime = timestamp;
  }

  public NoteInfo(Note note) {
    id = note.id();
    name = note.getName();
    config = note.getConfig();
    modTime = note.getModTime();
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Map<String, Object> getConfig() {
    return config;
  }

  public void setConfig(Map<String, Object> config) {
    this.config = config;
  }

  public long getModTime() {
    return modTime;
  }

  public void setMotTime(long timestamp) {
    this.modTime = timestamp;
  }
}

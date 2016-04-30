/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flume.interceptor;

import static org.apache.flume.interceptor.BodySpliceInterceptor.Constants.ATTREX;
import static org.apache.flume.interceptor.BodySpliceInterceptor.Constants.DEFAULT_ATTREX;

import java.util.List;
import org.apache.commons.lang.StringUtils;

import org.apache.flume.Context;
import org.apache.flume.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * Interceptor that filters events selectively based on a configured regular
 * expression matching against the event body.
 *
 * This supports either include- or exclude-based filtering. A given
 * interceptor can only perform one of these functions, but multiple
 * interceptor can be chained together to create more complex
 * inclusion/exclusion patterns. If include-based filtering is configured, then
 * all events matching the supplied regular expression will be passed through
 * and all events not matching will be ignored. If exclude-based filtering is
 * configured, than all events matching will be ignored, and all other events
 * will pass through.
 *
 * Note that all regular expression matching occurs through Java's built in
 * java.util.attrex package.
 *
 * Properties:<p>
 *
 *   attrex: Regular expression for matching excluded events.
 *          (default is ".*")<p>
 *
 *
 * Sample config:<p>
 *
 * <code>
 *   agent.sources.r1.channels = c1<p>
 *   agent.sources.r1.type = SEQ<p>
 *   agent.sources.r1.interceptors = i1<p>
 *   agent.sources.r1.interceptors.i1.type = BODY_SPLICE<p>
 *   agent.sources.r1.interceptors.i1.attrex = (WARNING)|(ERROR)|(FATAL)<p>
 * </code>
 *
 */
public class BodySpliceInterceptor implements Interceptor {

  private static final Logger logger = LoggerFactory
      .getLogger(StaticInterceptor.class);

  private final String attrexString;

  /**
   * Only {@link BodySpliceInterceptor.Builder} can build me
   */
  private BodySpliceInterceptor(String attrexString) {
    this.attrexString = attrexString;
  }

  @Override
  public void initialize() {
    // no-op
  }


  @Override
  /**
   * Returns the event if it passes the regular expression filter and null
   * otherwise.
   */
  public Event intercept(Event event) {
	String bd = new String(event.getBody());	
	String[] strBody = bd.split("\\|");
	String[] strAttrEx = attrexString.split("\\|");
	int i = 0;
	for( String str : strAttrEx) {
	  if(str.equals("n") && i < strBody.length)
	    strBody[i] = null;
	  i++;
	}
    String newBody = StringUtils.join(strBody,"|");
	event.setBody(newBody.getBytes());
    return event;
  }

  /**
   * Returns the set of events which pass filters, according to
   * {@link #intercept(Event)}.
   * @param events
   * @return
   */
  @Override
  public List<Event> intercept(List<Event> events) {
    List<Event> out = Lists.newArrayList();
    for (Event event : events) {
      Event outEvent = intercept(event);
      if (outEvent != null) { out.add(outEvent); }
    }
    return out;
  }

  @Override
  public void close() {
    // no-op
  }

  /**
   * Builder which builds new instance of the StaticInterceptor.
   */
  public static class Builder implements Interceptor.Builder {

    private String attrexString;

    @Override
    public void configure(Context context) {
       attrexString = context.getString(ATTREX, DEFAULT_ATTREX);
    }

    @Override
    public Interceptor build() {
      logger.info(String.format(
          "Creating BodySpliceInterceptor: attrex=%s",
          attrexString));
      return new BodySpliceInterceptor(attrexString);
    }
  }

  public static class Constants {

    public static final String ATTREX = "attrex";
    public static final String DEFAULT_ATTREX = "S|S|S|S|S|S";
  }

}

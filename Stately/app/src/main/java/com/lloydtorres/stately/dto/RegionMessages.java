/**
 * Copyright 2016 Lloyd Torres
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lloydtorres.stately.dto;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.lloydtorres.stately.helpers.SparkleHelper;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.core.Persister;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lloyd on 2016-01-24.
 * This object holds a list of posts from the regional message board.
 */
@Root(name="REGION", strict=false)
public class RegionMessages implements Parcelable {

    public static final String QUERY = SparkleHelper.BASE_URI_NOSLASH + "/cgi-bin/api.cgi?region=%s&q=messages;offset=%d;limit=%d"
                                            + "&v=" + SparkleHelper.API_VERSION;
    public static final String QUERY_ID = SparkleHelper.BASE_URI_NOSLASH + "/cgi-bin/api.cgi?region=%s&q=messages;fromid=%d;limit=%d;"
                                            + "&v=" + SparkleHelper.API_VERSION;
    public static final String LIKE_QUERY = SparkleHelper.BASE_URI_NOSLASH + "/page=ajax3/a=%s/postid=%d";
    public static final String RAW_QUERY = SparkleHelper.BASE_URI_NOSLASH + "/page=display_region_rmb/region=%s/template-overall=none";
    public static final String POST_QUERY = SparkleHelper.BASE_URI_NOSLASH + "/page=lodgermbpost/region=%s/template-overall=none";
    public static final String EDIT_QUERY_CHK = SparkleHelper.BASE_URI_NOSLASH + "/page=editpost/postid=%s/template-overall=none";
    public static final String EDIT_QUERY = SparkleHelper.BASE_URI_NOSLASH + "/page=editpost/edit=%s/template-overall=none";
    public static final String DELETE_QUERY = SparkleHelper.BASE_URI_NOSLASH + "/page=ajax/a=rmbdelete/region=%s/postid=%d";
    public static final String SUPPRESS_QUERY = SparkleHelper.BASE_URI_NOSLASH + "/page=ajax/a=rmbsuppress/region=%s/postid=%d";
    public static final String UNSUPPRESS_QUERY = SparkleHelper.BASE_URI_NOSLASH + "/page=ajax/a=rmbunsuppress/region=%s/postid=%d";

    @ElementList(name="MESSAGES", required=false)
    public List<Post> posts;

    public RegionMessages() { super(); }

    protected RegionMessages(Parcel in) {
        if (in.readByte() == 0x01) {
            posts = new ArrayList<Post>();
            in.readList(posts, Post.class.getClassLoader());
        } else {
            posts = null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (posts == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(posts);
        }
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<RegionMessages> CREATOR = new Parcelable.Creator<RegionMessages>() {
        @Override
        public RegionMessages createFromParcel(Parcel in) {
            return new RegionMessages(in);
        }

        @Override
        public RegionMessages[] newArray(int size) {
            return new RegionMessages[size];
        }
    };

    public static RegionMessages parseRegionMessagesXML(Context c, Persister serializer, String response) throws Exception {
        RegionMessages messageResponse = serializer.read(RegionMessages.class, response);
        if (messageResponse.posts != null && messageResponse.posts.size() > 0) {
            for (int i=0; i < messageResponse.posts.size(); i++) {
                messageResponse.posts.get(i).messageRaw = messageResponse.posts.get(i).message;
                messageResponse.posts.get(i).message = SparkleHelper.transformBBCodeToHtml(c, messageResponse.posts.get(i).message);
            }
        }
        return messageResponse;
    }
}

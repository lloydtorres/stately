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

/**
 * Created by Lloyd on 2016-01-16.
 * A convenience DTO used to track the number of WA members and delegates in a RecyclerView adapter.
 */
public class DataIntPair {
    public int members;
    public int delegates;

    public DataIntPair(int m, int d) {
        this.members = m;
        this.delegates = d;
    }
}

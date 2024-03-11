/**
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.  The ASF licenses this file to you under the Apache License, Version
 * 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */

package org.apache.storm.trident.tuple;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

public class ConsList extends AbstractList<Object> {
    List<Object> elems;
    Object first;

    // for kryo
    private ConsList() {
        elems = new ArrayList<>();
    }

    public ConsList(Object o, List<Object> elems) {
        this.elems = elems;
        first = o;
    }

    @Override
    public Object get(int i) {
        if (i == 0) {
            return first;
        } else {
            return elems.get(i - 1);
        }
    }

    @Override
    public int size() {
        return first == null ? elems.size() : elems.size() + 1;
    }

    // for kryo
    @Override
    public void add(int index, Object element) {
        if (index == 0) {
            first = element;
        } else {
            elems.add(index - 1, element);
        }
    }
}

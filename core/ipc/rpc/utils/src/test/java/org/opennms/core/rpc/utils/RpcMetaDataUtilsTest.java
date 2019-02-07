/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.core.rpc.utils;

import java.util.Map;
import java.util.TreeMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class RpcMetaDataUtilsTest {
    final Map<String, Map<String, String>> metaData = new TreeMap<>();

    @Before
    public void setUp() {
        addMetaData("ctx1", "key1", "val1");
        addMetaData("ctx1", "key2", "val2");
        addMetaData("ctx2", "key3", "val3");
        addMetaData("ctx2", "key4", "val4");
    }

    private void addMetaData(final String context, final String key, final String value) {
        metaData.putIfAbsent(context, new TreeMap<>());
        metaData.get(context).put(key, value);
    }

    @Test
    public void testMetaDataInterpolation() {
        final RpcMetaDataUtils rpcMetaDataUtils = new RpcMetaDataUtils();
        final Map<String, Object> attributes = new TreeMap<>();

        attributes.put("attribute1", "aaa$[ctx1:key1|ctx2:key2|default]bbb");
        attributes.put("attribute2", "aaa$[ctx1:key3|ctx2:key3|default]bbb");
        attributes.put("attribute3", "aaa$[ctx1:key4|ctx2:key1|default]bbb");
        attributes.put("attribute4", "aaa$[ctx1:key4]bbb");
        attributes.put("attribute5", "aaa$[ctx1:key4|]bbb");
        attributes.put("attribute6", "aaa$[ctx1:key4|default]bbb");
        attributes.put("attribute7", new Integer(42));
        attributes.put("attribute8", new Long(42L));
        attributes.put("attribute9", "aaa$[ctx1:key4|${nodeLabel}]bbb");

        final Map<String, Object> interpolatedAttributes = rpcMetaDataUtils.matchAndReplaceMetaData(metaData, attributes);

        Assert.assertEquals(attributes.size(), interpolatedAttributes.size());
        Assert.assertEquals("aaaval1bbb", interpolatedAttributes.get("attribute1"));
        Assert.assertEquals("aaaval3bbb", interpolatedAttributes.get("attribute2"));
        Assert.assertEquals("aaadefaultbbb", interpolatedAttributes.get("attribute3"));
        Assert.assertEquals("aaabbb", interpolatedAttributes.get("attribute4"));
        Assert.assertEquals("aaabbb", interpolatedAttributes.get("attribute5"));
        Assert.assertEquals("aaadefaultbbb", interpolatedAttributes.get("attribute6"));
        Assert.assertTrue(interpolatedAttributes.get("attribute7") instanceof Integer);
        Assert.assertTrue(interpolatedAttributes.get("attribute8") instanceof Long);
        Assert.assertEquals(42, interpolatedAttributes.get("attribute7"));
        Assert.assertEquals(42L, interpolatedAttributes.get("attribute8"));
        Assert.assertEquals("aaa${nodeLabel}bbb", interpolatedAttributes.get("attribute9"));
    }
}

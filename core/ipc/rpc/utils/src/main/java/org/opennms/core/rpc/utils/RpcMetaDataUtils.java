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

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsNodeMetaData;
import org.springframework.beans.factory.annotation.Autowired;

public class RpcMetaDataUtils {
    private static final String OUTER_REGEXP = "\\$\\[([^\\[\\]]+?)\\]";
    private static final String INNER_REGEXP = "(?:([^\\|]+?:[^\\|]+)|([^\\|]+))";
    private static final Pattern OUTER_PATTERN = Pattern.compile(OUTER_REGEXP);
    private static final Pattern INNER_PATTERN = Pattern.compile(INNER_REGEXP);

    @Autowired(required = false)
    private NodeDao nodeDao;

    public Map<String, Object> interpolateObjects(final int nodeId, final Map<String, Object> attributesMap) {
        return attributesMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> matchAndReplaceObject(getMetaData(nodeId), e.getValue())));
    }

    public Map<String, String> interpolateStrings(final int nodeId, final Map<String, String> attributesMap) {
        return attributesMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> matchAndReplaceString(getMetaData(nodeId), e.getValue())));
    }

    public Map<String, Map<String, String>> getMetaData(final int nodeId) {
        final Map<String, Map<String, String>> metaDataMap;

        final OnmsNode onmsNode = nodeDao.get(nodeId);

        if (onmsNode != null) {
            metaDataMap = mergeMetaData(onmsNode.getMetaData());
        } else {
            metaDataMap = new TreeMap<>();
        }

        return metaDataMap;
    }

    private Map<String, Map<String, String>> mergeMetaData(Collection<OnmsNodeMetaData>... lists) {
        final Map<String, Map<String, String>> metaData = new TreeMap<>();

        for (final Collection<OnmsNodeMetaData> collection : lists) {
            for (final OnmsNodeMetaData onmsNodeMetaData : collection) {
                metaData.putIfAbsent(onmsNodeMetaData.getContext(), new TreeMap<>());
                metaData.get(onmsNodeMetaData.getContext()).put(onmsNodeMetaData.getKey(), onmsNodeMetaData.getValue());
            }
        }

        return metaData;
    }

    private Object matchAndReplaceObject(final Map<String, Map<String, String>> metaDataMap, Object value) {
        if (value instanceof String) {
            return matchAndReplaceString(metaDataMap, (String) value);
        }
        return value;
    }

    private String matchAndReplaceString(final Map<String, Map<String, String>> metaDataMap, String value) {
            final StringBuffer stringBuffer = new StringBuffer();
            final Matcher outerMatcher = OUTER_PATTERN.matcher(value);
            while (outerMatcher.find()) {
                final Matcher innerMatcher = INNER_PATTERN.matcher(outerMatcher.group(1));
                String replacementValue = "";
                while (innerMatcher.find()) {
                    if (innerMatcher.group(1) != null) {
                        final String[] arr = innerMatcher.group(1).split(":");
                        if (metaDataMap.containsKey(arr[0])) {
                            if (metaDataMap.get(arr[0]).containsKey(arr[1])) {
                                replacementValue = Matcher.quoteReplacement(metaDataMap.get(arr[0]).get(arr[1]));
                                break;
                            }
                        }
                    }
                    if (innerMatcher.group(2) != null) {
                        replacementValue = Matcher.quoteReplacement(innerMatcher.group(2));
                        break;
                    }
                }
                outerMatcher.appendReplacement(stringBuffer, replacementValue);
            }
            outerMatcher.appendTail(stringBuffer);
            return stringBuffer.toString();
    }

    Map<String, Object> matchAndReplaceMetaData(Map<String, Map<String, String>> metaDataMap, Map<String, Object> attributesMap) {
        final Map<String, Object> interpolatedAttributes = new TreeMap<>(attributesMap);
        for (Map.Entry<String, Object> entry : interpolatedAttributes.entrySet().stream()
                .filter(e -> e.getValue() instanceof String)
                .collect(Collectors.toSet())) {
            interpolatedAttributes.put(entry.getKey(), matchAndReplaceObject(metaDataMap, entry.getValue().toString()));
        }
        return interpolatedAttributes;
    }
}

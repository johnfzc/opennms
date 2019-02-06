/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.features.apilayer.pollers;

import java.util.Map;

import org.opennms.features.apilayer.utils.InterfaceMapper;
import org.opennms.integration.api.v1.pollers.ServicePollerFactory;
import org.opennms.netmgt.poller.ServiceMonitor;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

/**
 * Manager to plug the service pollers that implement integration-api to the default service poller registry.
 */
public class ServicePollerManager extends InterfaceMapper<ServicePollerFactory, ServiceMonitor> {

    private static final Logger LOG = LoggerFactory.getLogger(InterfaceMapper.class);

    public ServicePollerManager(BundleContext bundleContext) {
        super(ServiceMonitor.class, bundleContext);
    }

    @Override
    public ServiceMonitor map(ServicePollerFactory ext) {
        return new ServicePollerImpl(ext);
    }

    @Override
    public Map<String, Object> getServiceProperties(ServicePollerFactory extension) {
        return ImmutableMap.<String,Object>builder()
                // Registry needs type of poller class.
                .put("type", extension.getPollerClassName())
                .build();
    }

}
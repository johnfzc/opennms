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

package org.opennms.core.rpc.commands;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.core.rpc.utils.mate.ContextKey;
import org.opennms.core.rpc.utils.mate.EntityScopeProvider;
import org.opennms.core.rpc.utils.mate.FallbackScope;
import org.opennms.core.rpc.utils.mate.Interpolator;
import org.opennms.core.rpc.utils.mate.Scope;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsNode;

@Command(scope = "meta", name = "test", description = "Test Meta-Data replacement")
@Service
public class MetaCommand implements Action {
    @Reference
    public NodeDao nodeDao;

    @Reference
    public EntityScopeProvider entityScopeProvider;

    @Option(name = "-n", aliases = "--node-id", description = "Node Id", required = true, multiValued = false)
    private int nodeId;

    @Option(name = "-i", aliases = "--interface-address", description = "Ip Interface Address", required = false, multiValued = false)
    private String interfaceAddress;

    @Option(name = "-s", aliases = "--service-name", description = "Service name", required = false, multiValued = false)
    private String serviceName;

    @Argument(index = 0, name = "expression", description = "Expression to use", required = true, multiValued = false)
    private String expression;

    private void printScope(final Scope scope) {
        final Map<String, Set<ContextKey>> grouped = scope.keys().stream()
                .collect(Collectors.groupingBy(ContextKey::getContext, TreeMap::new, Collectors.toCollection(TreeSet::new)));

        for (final Map.Entry<String, Set<ContextKey>> group : grouped.entrySet()) {
            System.out.printf("%s:\n", group.getKey());
            for (final ContextKey contextKey : group.getValue()) {
                System.out.printf("  %s='%s'\n", contextKey.getKey(), scope.get(contextKey).get());
            }
        }
    }

    @Override
    public Object execute() throws Exception {
        try {
            final OnmsNode onmsNode = nodeDao.get(this.nodeId);
            if (onmsNode == null) {
                System.out.printf("Cannot find node with nodeId=%d.\n", this.nodeId);
                return null;
            }

            // Group by context and sort contexts and keys
            final Scope nodeScope = this.entityScopeProvider.getScopeForNode(this.nodeId);
            final Scope interfaceScope = this.entityScopeProvider.getScopeForInterface(this.nodeId, this.interfaceAddress);
            final Scope serviceScope = this.entityScopeProvider.getScopeForService(this.nodeId, InetAddressUtils.getInetAddress(this.interfaceAddress), this.serviceName);

            System.out.printf("---\nMeta-Data for node (id=%d)\n", this.nodeId);
            printScope(nodeScope);

            if (interfaceAddress != null) {
                System.out.printf("---\nMeta-Data for interface (ipAddress=%s):\n", interfaceAddress);
                printScope(interfaceScope);
            }

            if (serviceName != null) {
                System.out.printf("---\nMeta-Data for service (name=%s):\n", serviceName);
                printScope(serviceScope);
            }

            final String result = Interpolator.interpolate(this.expression, new FallbackScope(nodeScope, interfaceScope, serviceScope));
            System.out.printf("---\nInput: '%s'\nOutput: '%s'\n", expression, result);
        } catch (final Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}

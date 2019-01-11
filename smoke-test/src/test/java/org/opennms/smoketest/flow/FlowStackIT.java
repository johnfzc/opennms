/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.smoketest.flow;

import org.junit.Rule;
import org.junit.Test;
import org.opennms.smoketest.telemetry.FlowTestBuilder;
import org.opennms.smoketest.telemetry.FlowTester;
import org.opennms.smoketest.telemetry.Packets;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;

public class FlowStackIT {

    private static final String ES_SERVICE_NAME = "elasticsearch";
    private static final String OPENNMS_SERVICE_NAME = "opennms";
    private static final int ES_API_PORT = 9200;
    private static final int OPENNMS_WEB_PORT = 8980;
    private static final int MAX_TIMEOUT = 5;

    @Rule
    public final DockerComposeContainer environment = new DockerComposeContainer(new File("src/test/resources/flows/flowStack/docker-compose.yml"))
            .withLocalCompose(true)
            .withExposedService(ES_SERVICE_NAME, ES_API_PORT, Wait.forHealthcheck().withStartupTimeout(Duration.ofMinutes(MAX_TIMEOUT)))
            .withExposedService(OPENNMS_SERVICE_NAME, OPENNMS_WEB_PORT, Wait.forHealthcheck().withStartupTimeout(Duration.ofMinutes(MAX_TIMEOUT)));

    @Test
    public void verifyFlowStack() throws IOException {

        // The stack needs some time after start up before it's ready to process flows.
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        final InetSocketAddress elasticRestAddress = new InetSocketAddress(environment.getServiceHost(ES_SERVICE_NAME, ES_API_PORT), environment.getServicePort(ES_SERVICE_NAME, ES_API_PORT));
        final InetSocketAddress opennmsWebAddress = new InetSocketAddress(environment.getServiceHost(OPENNMS_SERVICE_NAME, OPENNMS_WEB_PORT), environment.getServicePort(OPENNMS_SERVICE_NAME, OPENNMS_WEB_PORT));

        // This is a workaround because I haven't figured out to create dynamically mapped UDP ports with .withExposedService()
        final InetSocketAddress opennmsNetflow5AdapterAddress = new InetSocketAddress("localhost", 50000);
        final InetSocketAddress opennmsNetflow9AdapterAddress = new InetSocketAddress("localhost", 50001);
        final InetSocketAddress opennmsIpfixAdapterAddress = new InetSocketAddress("localhost", 50002);
        final InetSocketAddress opennmsSflowAdapterAddress = new InetSocketAddress("localhost", 50003);

        final FlowTester flowTester = new FlowTestBuilder()
                .withFlowPacket(Packets.Netflow5, opennmsNetflow5AdapterAddress)
                .withFlowPacket(Packets.Netflow9, opennmsNetflow9AdapterAddress)
                .withFlowPacket(Packets.Ipfix, opennmsIpfixAdapterAddress)
                .withFlowPacket(Packets.SFlow, opennmsSflowAdapterAddress)
                .verifyOpennmsRestEndpoint(opennmsWebAddress)
                .build(elasticRestAddress);

        flowTester.verifyFlows();
    }
}


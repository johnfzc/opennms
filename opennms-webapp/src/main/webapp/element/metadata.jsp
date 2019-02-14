<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

--%>

<%@page language="java"
        contentType="text/html"
        session="true"
        import="
            java.util.*,
            org.opennms.netmgt.model.OnmsNode,
            org.opennms.web.api.Authentication,
            org.opennms.web.element.*,
            org.opennms.netmgt.model.OnmsMetaData"
%>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%
    final OnmsNode node = ElementUtil.getNodeByParams(request, getServletContext());

    String nodeBreadCrumb = "<a href='element/node.jsp?node=" + node.getId()  + "'>Node</a>";
%>

<jsp:include page="/includes/bootstrap.jsp" flush="false" >
    <jsp:param name="title" value="Metadata" />
    <jsp:param name="headTitle" value="<%= node.getLabel() %>" />
    <jsp:param name="headTitle" value="Metadata" />
    <jsp:param name="breadcrumb" value="<a href='element/index.jsp'>Search</a>" />
    <jsp:param name="breadcrumb" value="<%= nodeBreadCrumb %>" />
    <jsp:param name="breadcrumb" value="Metadata" />
</jsp:include>

<h4>Metadata for Node: <%= node.getLabel() %></h4>

<div class="row">
    <div class="col-md-6"> <!-- content-right -->
        <%
            final Map<String, Map<String, String>> metaData = new TreeMap<>();

            int switchColumns = metaData.size() / 2 + 1;

            for(final OnmsMetaData onmsNodeMetaData : node.getMetaData()) {
                metaData.putIfAbsent(onmsNodeMetaData.getContext(), new TreeMap<String, String>());
                metaData.get(onmsNodeMetaData.getContext()).put(onmsNodeMetaData.getKey(), onmsNodeMetaData.getValue());
            }

            if (metaData.size()>0) {
                for(final Map.Entry<String, Map<String, String>> entry1 : metaData.entrySet()) {
        %>
        <div class="panel panel-default">
            <div class="panel-heading">
                <h3 class="panel-title">Context <%= entry1.getKey() %></h3>
            </div>
            <!-- general info box -->
            <table class="table table-condensed">
        <%
                    for(final Map.Entry<String, String> entry2 : entry1.getValue().entrySet()) {
                        String value = entry2.getValue();

                        if (!request.isUserInRole(Authentication.ROLE_ADMIN) && entry2.getKey().matches(".*([pP]assword|[sS]ecret).*")) {
                            value = "***";
                        }
        %>
                <tr>
                    <th width="30%"><%= entry2.getKey() %></th>
                    <td><%= value %></td>
                </tr>
        <%
                    }
        %>
            </table>
        </div> <!-- panel -->
        <%
                    if (switchColumns-- == 0) {
        %>
    </div> <!-- content-left -->
    <div class="col-md-6">
        <%
                    }
                }
            } else {
        %>
        <b>No Metadata available for this node.</b><br/><br/>
        <%
            }
        %>
    </div> <!-- content-right -->
</div> <!-- row -->

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />

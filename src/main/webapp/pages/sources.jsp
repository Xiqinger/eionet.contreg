<%@page contentType="text/html;charset=UTF-8" import="java.util.*,java.io.*"%>

<%@ include file="/pages/common/taglibs.jsp"%>	

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Harvesting Sources">
	<stripes:layout-component name="errors"/>
	<stripes:layout-component name="messages"/>
	<stripes:layout-component name="contents">
	
	<jsp:useBean id="harvestSourceList" scope="page"
                     class="eionet.cr.web.action.HarvestSourceListActionBean"/>
                     
		<h1>Harvesting sources</h1>
	        <table class="datatable">
	        	<thead>
		        	<tr>
		        		<th scope="col">Name</th>
		        		<th scope="col">URL</th>
		        		<th scope="col">Type</th>
		        		<th scope="col">E-mails</th>
		        		<th scope="col">Date Created</th>
		        		<th scope="col">Creator</th>
		        		<th scope="col">Statements</th>
		        		<th scope="col"></th>
		        		<th scope="col"></th>
		        		<th scope="col"></th>
		        	</tr>
	        	</thead>
	        	<tbody>
	        		<c:forEach items="${harvestSourceList.harvestSources}" var="source" varStatus="loop">
		        		<tr>
		        			<td>${source.name}</td>
		        			<td>${source.url}</td>
		        			<td>${source.type}</td>
		        			<td>${source.emails}</td>
		        			<td>${source.dateCreated}</td>
		        			<td>${source.creator}</td>
		        			<td>${source.statements}</td>
		        			<td>
		        				<stripes:link href="/source.action" event="preView">
	                                <img src="${pageContext.request.contextPath}/images/view.gif" title="View"/>
	                                <stripes:param name="harvestSource.sourceId" value="${source.sourceId}"/>
	                            </stripes:link>
		        			</td>
		        			<td>
		        				<stripes:link href="/source.action" event="preEdit">
	                                <img src="${pageContext.request.contextPath}/images/edit.gif" title="Edit"/>
	                                <stripes:param name="harvestSource.sourceId" value="${source.sourceId}"/>
	                            </stripes:link>
		        			</td>
		        			<td>
		        				<stripes:link href="/source.action" event="delete" onclick="return confirm('Are you sure you want to delete this harvesting source');">
	                                <img src="${pageContext.request.contextPath}/images/delete.gif" title="Delete"/>
	                                <stripes:param name="harvestSource.sourceId" value="${source.sourceId}"/>
	                            </stripes:link>
		        			</td>
		        		</tr>
	        		</c:forEach>
	        	</tbody>
	        </table>
	</stripes:layout-component>
</stripes:layout-render>

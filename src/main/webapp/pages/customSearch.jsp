<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>	

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Custom search">

	<stripes:layout-component name="contents">
	
        <h1>Custom search</h1>
        <p>
        	Add a search filter and give it a value. For some filters the system provides a list of existing values.
        	Text inputs of such filters have a special icon to the right of them which opens the list. You can remove
        	added filters by using the removal icons that are displayed to the left of them.
        </p>

		<c:choose>        
        	<c:when test="${actionBean.availableFilters!=null && fn:length(actionBean.availableFilters)>0}">
        
		    	<div id="filterSelectionArea" style="margin-top:20px">
		    	
		    		<stripes:form name="customSearchForm" action="/customSearch.action" method="get" id="customSearchForm" acceptcharset="UTF-8">
		    		
		    			<c:if test="${fn:length(actionBean.selectedFilters)<fn:length(actionBean.availableFilters)}">
			    			<stripes:select name="addedFilter" id="filterSelect">
			    				<stripes:option value="" label=""/>
			    				<c:forEach var="availableFilter" items="${actionBean.availableFilters}">
			    					<c:if test="${actionBean.selectedFilters[availableFilter.key]==null}">
			    						<stripes:option value="${availableFilter.key}" label="${availableFilter.value.title}" title="${availableFilter.value.uri}"/>
			    					</c:if>
			    				</c:forEach>
			    			</stripes:select>&nbsp;
			    			<stripes:submit name="addFilter" value="Add filter"/>
			    		</c:if>
		    			
		    			<c:if test="${actionBean.selectedFilters!=null && fn:length(actionBean.selectedFilters)>0}">
			    			<table style="margin-top:20px;margin-bottom:20px">
			    				<c:forEach var="availableFilter" items="${actionBean.availableFilters}">
			    					<c:if test="${actionBean.selectedFilters[availableFilter.key]!=null}">
				    					<tr>
				    						<td style="padding-right:12px">
				    							<input type="image" name="removeFilter_${availableFilter.key}" src="${pageContext.request.contextPath}/images/delete_small.gif" title="Remove filter" alt="Remove filter"/>
				    						</td>
				    						<td style="text-align:right">${availableFilter.value.title}:</td>
				    						<td>
				    							<c:if test="${!actionBean.showPicklist || actionBean.picklistFilter!=availableFilter.key || actionBean.picklist==null || fn:length(actionBean.picklist)==0}">
				    								<input type="text" name="value_${availableFilter.key}" value="${fn:escapeXml(actionBean.selectedFilters[availableFilter.key])}" size="30"/>
				    							</c:if>
				    							<c:if test="${availableFilter.value.provideValues}">
					    							<c:if test="${actionBean.showPicklist && actionBean.picklistFilter==availableFilter.key && actionBean.picklist!=null && fn:length(actionBean.picklist)>0}">
														<select name="value_${availableFilter.key}" style="max-width:400px">
						                        			<option value="" selected="selected">- select a value -</option>
						                        			<c:if test="${actionBean.picklist!=null}">
							                        			<c:forEach var="picklistItem" items="${actionBean.picklist}">
							                        				<option value="${fn:escapeXml(crfn:addQuotesIfWhitespaceInside(picklistItem))}" title="${fn:escapeXml(picklistItem)}" style="max-width:400px">${fn:escapeXml(picklistItem)}</option>
							                        			</c:forEach>
							                        		</c:if>
														</select>
					    							</c:if>
					    							<c:if test="${!actionBean.showPicklist || actionBean.picklistFilter!=availableFilter.key}">
					    								<input type="image" name="showPicklist_${availableFilter.key}" src="${pageContext.request.contextPath}/images/list.gif" title="Get existing values" alt="Get existing values" style="position:absolute;padding-top:1px"/>
					    							</c:if>
					    							<c:if test="${actionBean.showPicklist && actionBean.picklistFilter==availableFilter.key && (actionBean.picklist==null || fn:length(actionBean.picklist)==0)}">
					    								No picklist found!
					    							</c:if>
					    						</c:if>
				    						</td>
				    					</tr>
				    				</c:if>
			    				</c:forEach>
			    			</table>
			    			<stripes:submit name="search" value="Search"/>
		    			</c:if>
		    			
		    			<c:if test="${(actionBean.resultList!=null && fn:length(actionBean.resultList)>0) || not empty param.search}">
				    		<stripes:layout-render name="/pages/common/subjectsResultList.jsp" tableClass="sortable"/>
				    	</c:if>
				    	
		    		</stripes:form>
			    </div>
			    
			</c:when>
			<c:otherwise>
				No available filters found!
			</c:otherwise>
		</c:choose>
				
	</stripes:layout-component>
</stripes:layout-render>

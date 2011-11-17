<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Resource properties">

    <stripes:layout-component name="contents">

        <cr:tabMenu tabs="${actionBean.tabs}" />

        <c:set var="operationsAvailable" value="${actionBean.usersDataset}" />

        <br style="clear:left" />
        <br />

        <ul id="dropdown-operations">
            <li><a href="#">Operations</a>
                <ul>
                    <c:if test="${operationsAvailable}">
                        <li>
                            <stripes:link class="link-plain" href="/compiledDataset.action" event="reload">
                                <stripes:param name="uri" value="${actionBean.uri}" />
                                Reload
                            </stripes:link>
                        </li>
                    </c:if>
                    <li>
                        <stripes:link class="link-plain" href="/sparql">
                            <stripes:param name="default-graph-uri" value="${actionBean.uri}" />
                            SPARQL endpoint
                        </stripes:link>
                    </li>
                </ul>
             </li>
         </ul>

        <h1>Compiled dataset sources</h1>

        <c:if test="${actionBean.currentlyReloaded}">
            <div class="advice-msg">Compiled dataset is currently being reloaded!</div>
        </c:if>

        <crfn:form action="/compiledDataset.action" method="post">
        <stripes:hidden name="uri" value="${actionBean.uri}" />
        <display:table name="${actionBean.sources}" class="sortable" id="item" sort="list" requestURI="${actionBean.urlBinding}">
            <c:if test="${operationsAvailable}">
                <display:column>
                    <stripes:checkbox name="selectedFiles" value="${item.uri}" />
                </display:column>
            </c:if>
            <display:column title="URL" sortable="true">
                <stripes:link href="/factsheet.action">
                    <stripes:param name="uri" value="${item.uri}" />
                    <c:out value="${item.uri}" />
                </stripes:link>
            </display:column>
            <display:column title="Last modified" sortable="true">
                <fmt:formatDate value="${item.lastModifiedDate}" pattern="yyyy-MM-dd"/>T<fmt:formatDate value="${item.lastModifiedDate}" pattern="HH:mm:ss"/>
            </display:column>
        </display:table>
        <c:if test="${operationsAvailable}">
            <br />
            <div>
                <stripes:submit name="removeFiles" value="Delete" title="Remove files from dataset"/>
            </div>
        </c:if>
        </crfn:form>

    </stripes:layout-component>

</stripes:layout-render>
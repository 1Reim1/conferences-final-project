<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@attribute name="page" required="true" type="java.lang.Integer" %>
<%@attribute name="pages" required="true" type="java.lang.Integer" %>
<%@attribute name="order" required="true" type="com.my.conferences.entity.Event.Order" %>

<nav aria-label="Page navigation example">
    <ul class="pagination">
        <li class="page-item <c:if test="${page == 1}">disabled</c:if>"><a class="page-link" href="?page=${page-1}&order=${order}">Previous</a>
        </li>
        <li class="page-item <c:if test="${page == 1}">active</c:if>"><a class="page-link" href="?page=1&order=${order}">1</a></li>

        <c:set var="pageStart" value="${page - 1}"/>
        <c:if test="${pageStart < 2}">
            <c:set var="pageStart" value="2"/>
        </c:if>

        <c:set var="pageEnd" value="${pageStart + 2}"/>
        <c:if test="${pageEnd > pages - 1}">
            <c:set var="pageEnd" value="${pages - 1}"/>

            <c:set var="pageStart" value="${pageEnd - 2}"/>
            <c:if test="${pageStart < 2}">
                <c:set var="pageStart" value="2"/>
            </c:if>
        </c:if>

        <c:if test="${pageStart > 2}">
            <li class="page-item disabled"><a class="page-link" href="#">...</a></li>
        </c:if>

        <c:forEach var="p" begin="${pageStart}" end="${pageEnd}">
            <li class="page-item <c:if test="${page == p}">active</c:if>">
                <a class="page-link" href="?page=${p}&order=${order}">${p}</a>
            </li>
        </c:forEach>

        <c:if test="${pageEnd < pages - 1}">
            <li class="page-item disabled"><a class="page-link" href="#">...</a></li>
        </c:if>

        <c:if test="${pages != 1}">
            <li class="page-item <c:if test="${page == pages}">active</c:if>"><a class="page-link"
                                                                                 href="?page=${pages}&order=${order}">${pages}</a></li>
        </c:if>
        <li class="page-item <c:if test="${page == pages}">disabled</c:if>"><a class="page-link" href="?page=${page+1}&order=${order}">Next</a>
        </li>
    </ul>
</nav>
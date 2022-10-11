<%@ tag pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setLocale value="${cookie['lang'].value}"/>
<fmt:setBundle basename="internationalization"/>
<sidebar class="col-3 bg-light">
    <div class="position-relative col-12">
        <h3 class="text-center">Conferences</h3>
        <hr>
        <c:choose>
            <c:when test="${pageContext.request.requestURL.indexOf('/home') != -1}">
                <button id="order-date" type="button" class="btn btn-dark col-12 text-start <c:if test="${order == 'DATE'}">disabled</c:if>"><fmt:message key="home.sort_by_date"/></button>
                <button id="order-reports" type="button" class="btn btn-dark col-12 text-start <c:if test="${order == 'REPORTS'}">disabled</c:if>"><fmt:message key="home.sort_by_reports"/></button>
                <button id="order-participants" type="button" class="btn btn-dark col-12 text-start <c:if test="${order == 'PARTICIPANTS'}">disabled</c:if>"><fmt:message key="home.sort_by_participants"/></button>
                <button id="order-reverse" type="button" class="btn btn-outline-primary col-12 text-start">
                    <fmt:message key="home.reverse_order"/>
                    <c:if test="${reverseOrder}">(<fmt:message key="home.enabled"/>)</c:if>
                </button>
                <div class="row">
                    <c:if test="${futureOrder}">
                        <button id="order-past" class="btn btn-dark col-4 offset-1"><fmt:message key="home.past"/></button>
                        <button id="order-future" class="btn btn-primary col-4 offset-2 disabled"><fmt:message key="home.future"/></button>
                    </c:if>
                    <c:if test="${!futureOrder}">
                        <button id="order-past" class="btn btn-primary col-4 offset-1 disabled"><fmt:message key="home.past"/></button>
                        <button id="order-future" class="btn btn-dark col-4 offset-2"><fmt:message key="home.future"/></button>
                    </c:if>
                </div>
                <div class="row">
                    <c:if test="${onlyMyEvents}">
                        <button id="order-my-events" class="btn btn-primary col-4 offset-1 disabled"><fmt:message key="home.my_events"/></button>
                        <button id="order-all-events" class="btn btn-dark col-4 offset-2"><fmt:message key="home.all_events"/></button>
                    </c:if>
                    <c:if test="${!onlyMyEvents}">
                        <button id="order-my-events" class="btn btn-dark col-4 offset-1"><fmt:message key="home.my_events"/></button>
                        <button id="order-all-events" class="btn btn-primary col-4 offset-2 disabled"><fmt:message key="home.all_events"/></button>
                    </c:if>
                </div>
            </c:when>
            <c:otherwise>
                <button id="home-btn" type="button" class="btn btn-dark col-12 text-start"><fmt:message key="sidebar.home_page"/></button>
            </c:otherwise>
        </c:choose>
        <c:if test="${sessionScope.user.role == 'MODERATOR' && pageContext.request.requestURL.indexOf('/create-event') == -1}">
            <button id="create-event-btn" type="button" class="btn btn-primary col-12 text-start"><fmt:message key="sidebar.create_event"/></button>
        </c:if>
        <c:if test="${sessionScope.user.role != 'USER' && pageContext.request.requestURL.indexOf('/new-reports') == -1}">
            <button id="new-reports-btn" type="button" class="btn btn-primary col-12 text-start"><fmt:message key="sidebar.new_reports"/></button>
        </c:if>
        <hr>
        <div class="col-10 offset-1">
            <div class="row">
                <c:choose>
                    <c:when test="${cookie['lang'].value == 'uk'}">
                        <button id="ukrainian-language-btn" class="btn btn-primary col-5 disabled">Українська</button>
                        <button id="english-language-btn" class="btn btn-dark col-5 offset-2">English</button>
                    </c:when>
                    <c:otherwise>
                        <button id="ukrainian-language-btn" class="btn btn-dark col-5">Українська</button>
                        <button id="english-language-btn" class="btn btn-primary col-5 offset-2 disabled">English</button>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
        <div class="down-menu col-12 position-absolute bottom-0 start-0">
            <p style="font-size: 12px"><fmt:message key="sidebar.you_are_logged_as"/> ${sessionScope.user.firstName} ${sessionScope.user.lastName}
                <c:if test="${sessionScope.user.role != 'USER'}">
                    <span class="text-lowercase">(${sessionScope.user.role})</span>
                </c:if>
            </p>
            <button type="button" class="btn btn-primary col-12 text-start" id="logout-btn"><fmt:message key="sidebar.logout"/></button>
        </div>
    </div>
</sidebar>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<sidebar class="col-3 bg-light">
    <div class="position-relative col-12">
        <h3 class="text-center">Conferences</h3>
        <hr>
        <c:choose>
            <c:when test="${pageContext.request.requestURL.indexOf('/home') != -1}">
                <button id="order-date" type="button" class="btn btn-dark col-12 text-start <c:if test="${order == 'DATE'}">disabled</c:if>">By date</button>
                <button id="order-reports" type="button" class="btn btn-dark col-12 text-start <c:if test="${order == 'REPORTS'}">disabled</c:if>">By reports</button>
                <button id="order-participants" type="button" class="btn btn-dark col-12 text-start <c:if test="${order == 'PARTICIPANTS'}">disabled</c:if>">By participants</button>
                <button id="order-reverse" type="button" class="btn btn-outline-primary col-12 text-start">Reverse order
                    <c:if test="${reverseOrder}">(enabled)</c:if>
                </button>
                <div class="row">
                    <c:if test="${futureOrder}">
                        <button id="order-past" class="btn btn-dark col-4 offset-1">Past</button>
                        <button id="order-future" class="btn btn-primary col-4 offset-2 disabled">Future</button>
                    </c:if>
                    <c:if test="${!futureOrder}">
                        <button id="order-past" class="btn btn-primary col-4 offset-1 disabled">Past</button>
                        <button id="order-future" class="btn btn-dark col-4 offset-2">Future</button>
                    </c:if>
                </div>
                <div class="row">
                    <c:if test="${onlyMyEvents}">
                        <button id="order-my-events" class="btn btn-primary col-4 offset-1 disabled">My events</button>
                        <button id="order-all-events" class="btn btn-dark col-4 offset-2">All events</button>
                    </c:if>
                    <c:if test="${!onlyMyEvents}">
                        <button id="order-my-events" class="btn btn-dark col-4 offset-1">My events</button>
                        <button id="order-all-events" class="btn btn-primary col-4 offset-2 disabled">All events</button>
                    </c:if>
                </div>
            </c:when>
            <c:otherwise>
                <button id="home-btn" type="button" class="btn btn-dark col-12 text-start">Home</button>
            </c:otherwise>
        </c:choose>
        <c:if test="${sessionScope.user.role == 'MODERATOR' && pageContext.request.requestURL.indexOf('/create-event') == -1}">
            <button id="create-event-btn" type="button" class="btn btn-primary col-12 text-start">Create event</button>
        </c:if>
        <c:if test="${sessionScope.user.role != 'USER' && pageContext.request.requestURL.indexOf('/new-reports') == -1}">
            <button id="new-reports-btn" type="button" class="btn btn-primary col-12 text-start">New reports</button>
        </c:if>
        <hr>
        <button type="button" class="btn btn-primary col-12 text-start">My profile</button>
        <div class="down-menu col-12 position-absolute bottom-0 start-0">
            <p style="font-size: 12px">You are logged as ${sessionScope.user.firstName} ${sessionScope.user.lastName}
                <c:if test="${sessionScope.user.role != 'USER'}">
                    <span class="text-lowercase">(${sessionScope.user.role})</span>
                </c:if>
            </p>
            <button type="button" class="btn btn-primary col-12 text-start" id="logout-btn">Logout</button>
        </div>
    </div>
</sidebar>
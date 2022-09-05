<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="my" tagdir="/WEB-INF/tags" %>
<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link href="css/bootstrap.min.css" rel="stylesheet">
    <link href="css/home.css" rel="stylesheet">
    <title>Home</title>
</head>
<body>

<sidebar class="col-3 bg-light">
    <div class="position-relative col-12">
        <h3 class="text-center">Conferences</h3>
        <hr>
        <button id="order-date" type="button" class="btn btn-dark col-12 text-start <c:if test="${order == 'DATE'}">disabled</c:if>">By date</button>
        <button id="order-reports" type="button" class="btn btn-dark col-12 text-start <c:if test="${order == 'REPORTS'}">disabled</c:if>">By reports</button>
        <button id="order-participants" type="button" class="btn btn-dark col-12 text-start <c:if test="${order == 'PARTICIPANTS'}">disabled</c:if>">By participants</button>
        <button id="order-reverse" type="button" class="btn btn-outline-primary col-12 text-start">Reverse order
            <c:if test="${reverseOrder}">(enabled)</c:if>
            </button>
        <hr>
        <button type="button" class="btn btn-dark col-12 text-start">My events</button>
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

<div class="container-fluid">
    <div class="row">
        <div class="events row col-8 offset-3">
            <c:forEach items="${events}" var="event">
                <my:event-card event="${event}"/>
            </c:forEach>
        </div>


        <div class="offset-3 col-8">
            <div class="offset-4 col-2">
                <my:pagination page="${page}" pages="${pages}"/>
            </div>
        </div>
    </div>
</div>


<script src="js/bootstrap.min.js"></script>
<script src="js/jquery-3.6.0.min.js"></script>
<script src="js/sidebar.js"></script>
<script src="js/home.js"></script>
</body>
</html>
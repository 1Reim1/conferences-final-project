<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<html>
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link href="css/bootstrap.min.css" rel="stylesheet">
    <link href="css/event.css" rel="stylesheet">
    <title>${event.title}</title>
</head>
<body>

<!-- Sidebar -->
<sidebar class="col-3 bg-light">
    <div class="position-relative col-12">
        <h3 class="text-center">Conferences</h3>
        <hr>
        <button id="home-btn" type="button" class="btn btn-dark col-12 text-start">Home</button>
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

<!-- Event -->

<div class="container-fluid">
    <div class="row">
        <div class="col-9 offset-3">
            <div class="container">
                <div class="row">
                    <h3 class="text-center">${event.title}
                        <c:if test="${sessionScope.user.role == 'MODERATOR'}">
                            <img class="modify-icon" src="svg/magic.svg" alt="modify" data-bs-toggle="modal" data-bs-target="#modify-title-modal">
                        </c:if>
                    </h3>
                    <p>${event.description}
                        <c:if test="${sessionScope.user.role == 'MODERATOR'}">
                            <img class="modify-icon" src="svg/magic.svg" alt="modify" data-bs-toggle="modal" data-bs-target="#modify-description-modal">
                        </c:if>
                    </p>
                    <span class="col-4 text-center <c:if test="${sessionScope.user.role == 'MODERATOR'}">control-element</c:if>" data-bs-toggle="modal" data-bs-target="#participants-modal">Participants: <b>44</b></span>
                    <span class="col-4 text-center event-description">Reports: <b>5</b></span>
                    <span class="col-4 text-center <c:if test="${sessionScope.user.role == 'MODERATOR'}">control-element</c:if>" data-bs-toggle="modal" data-bs-target="#modify-date-modal">Date: <b><fmt:formatDate value="${event.date}" pattern="dd-MM-yyyy HH:mm"/></b></span>
                    <hr class="col-12">
                    <p class="event-description">Place: <b>Lorem ipsum dolor sit amet, consectetur adipisicing elit. Necessitatibus, voluptatum.</b> <img class="modify-icon" src="svg/magic.svg" alt="modify" data-bs-toggle="modal" data-bs-target="#modify-place-modal"></p>
                    <hr>
                    <h4 class="text-center"> Reports: </h4>
                    <a class="list-group">
                        <a class="list-group-item list-group-item-action">
                            <div class="row">
                                <span class="col-5">Report 1 <img class="modify-icon" src="svg/magic.svg" alt="modify" data-bs-toggle="modal" data-bs-target="#modify-report-topic-modal"></span>
                                <button type="button" class="btn btn-outline-danger col-2">Cancel report</button>
                                <span class="col-5 text-end">Catlin Snow</span>
                            </div>
                        </a>
                        <a class="list-group-item list-group-item-action">
                            <div class="row">
                                <span class="col-5">Report 2 <img class="modify-icon" src="svg/magic.svg" alt="modify" data-bs-toggle="modal" data-bs-target="#modify-report-topic-modal"></span>
                                <button type="button" class="btn btn-outline-danger col-2">Cancel report</button>
                                <span class="col-5 text-end">Abraham Guild</span>
                            </div>
                        </a>
                        <a class="list-group-item list-group-item-action">
                            <div class="row">
                                <span class="col-5">Report 3 <img class="modify-icon" src="svg/magic.svg" alt="modify" data-bs-toggle="modal" data-bs-target="#modify-report-topic-modal"></span>
                                <button type="button" class="btn btn-outline-danger col-2">Cancel report</button>
                                <span class="col-5 text-end">Barry Alien</span>
                            </div>
                        </a>
                        <a class="list-group-item list-group-item-action">
                            <div class="row">
                                <span class="col-5">Report 4 <img class="modify-icon" src="svg/magic.svg" alt="modify" data-bs-toggle="modal" data-bs-target="#modify-report-topic-modal"></span>
                                <button type="button" class="btn btn-outline-danger col-2">Cancel report</button>
                                <span class="col-5 text-end">Scott Young</span>
                            </div>
                        </a>
                        <a class="list-group-item list-group-item-action">
                            <div class="row align-text-bottom">
                                <span class="col-4">Report 5 (Not confirmed) <img class="modify-icon" src="svg/magic.svg" alt="modify" data-bs-toggle="modal" data-bs-target="#modify-report-topic-modal"></span>
                                <div class="col-4">
                                    <div class="row">
                                        <button type="button" class="btn btn-outline-success col-5">Confirm report</button>
                                        <button type="button" class="btn btn-outline-danger col-5 offset-2">Cancel report</button>
                                    </div>
                                </div>
                                <span class="col-4 text-end">Adam James</span>
                            </div>
                        </a>
                    </a>
                    <button id="offer-report" type="button" class="btn btn-secondary mb-2" data-bs-toggle="modal" data-bs-target="#offer-report-modal">Offer report</button>
                    <button type="button" class="btn btn-danger mb-2">Hide the conference</button>
                    <button id="save-changes" type="button" class="btn btn-success">Save changes</button>
                </div>
            </div>
        </div>
    </div>
</div>

<script src="js/bootstrap.min.js"></script>
<script src="js/jquery-3.6.0.min.js"></script>
<script src="js/sidebar.js"></script>
</body>
</html>

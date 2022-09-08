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
            <div id="event" class="container" event-id="${event.id}">
                <div class="row">
                    <div class="alert alert-danger" id="error-alert" role="alert" style="text-align: center; display: none"></div>
                    <h3 class="text-center"><span id="event-title">${event.title}</span>
                        <c:if test="${event.hidden}">(hidden)</c:if>
                        <c:if test="${event.moderator.id == sessionScope.user.id}">
                            <img class="modify-icon" src="svg/magic.svg" alt="modify" data-bs-toggle="modal" data-bs-target="#modify-title-modal">
                        </c:if>
                    </h3>
                    <p><span id="event-description">${event.description}</span>
                        <c:if test="${event.moderator.id == sessionScope.user.id}">
                            <img class="modify-icon" src="svg/magic.svg" alt="modify" data-bs-toggle="modal" data-bs-target="#modify-description-modal">
                        </c:if>
                    </p>
                    <span class="col-4 text-center <c:if test="${event.moderator.id == sessionScope.user.id}">control-element</c:if>" data-bs-toggle="modal" data-bs-target="#participants-modal">
                        Participants: <b>${event.participants.size()}</b>
                    </span>
                    <span class="col-4 text-center event-description">
                        Reports: <b>${event.reports.size()}</b>
                    </span>
                    <span class="col-4 text-center <c:if test="${event.moderator.id == sessionScope.user.id}">control-element</c:if>" data-bs-toggle="modal" data-bs-target="#modify-date-modal">
                        Date:
                        <b id="event-date"><fmt:formatDate value="${event.date}" pattern="dd-MM-yyyy HH:mm"/></b>
                    </span>
                    <hr class="col-12">
                    <p class="event-description">
                        Place: <b id="event-place">${event.place}</b>
                        <c:if test="${event.moderator.id == sessionScope.user.id}">
                            <img class="modify-icon" src="svg/magic.svg" alt="modify" data-bs-toggle="modal" data-bs-target="#modify-place-modal">
                        </c:if>
                    </p>
                    <hr>
                    <h4 class="text-center"> Reports: </h4>
                    <a class="list-group">
                        <c:forEach items="${event.reports}" var="report">
                            <a class="list-group-item list-group-item-action">
                                <div class="row report" report-id="${report.id}">
                                    <span class="col-5">
                                            <span class="topic">${report.topic}</span>
                                            <c:if test="${event.moderator.id == sessionScope.user.id}">
                                                <img class="modify-icon" src="svg/magic.svg" alt="modify" data-bs-toggle="modal" data-bs-target="#modify-report-topic-modal">
                                            </c:if>
                                            <c:if test="${!report.confirmed}">
                                                <c:if test="${report.speaker.id == report.creator.id}">
                                                    (Not confirmed by moderator)
                                                </c:if>
                                                <c:if test="${report.speaker.id != report.creator.id}">
                                                    (Not confirmed by speaker)
                                                </c:if>
                                            </c:if>
                                    </span>
                                    <div class="col-4">
                                        <c:if test="${!report.confirmed}">
                                            <div class="row">
                                                <c:choose>
                                                    <c:when test="${report.speaker.id != report.creator.id && report.speaker.id == sessionScope.user.id}">
                                                        <button type="button" class="btn btn-outline-success col-5 confirm-report-btn">Accept report</button>
                                                        <button type="button" class="btn btn-outline-danger col-5 offset-2 cancel-report-btn">Reject report</button>
                                                    </c:when>
                                                    <c:when test="${report.speaker.id == report.creator.id && event.moderator.id == sessionScope.user.id}">
                                                        <button type="button" class="btn btn-outline-success col-5 confirm-report-btn">Confirm report</button>
                                                        <button type="button" class="btn btn-outline-danger col-5 offset-2 cancel-report-btn">Cancel report</button>
                                                    </c:when>
                                                    <c:when test="${event.moderator.id == sessionScope.user.id || (report.speaker.id == report.creator.id && report.speaker.id == sessionScope.user.id)}">
                                                            <button type="button" class="btn btn-outline-danger col-6 offset-3 cancel-report-btn">Cancel report</button>
                                                    </c:when>
                                                </c:choose>
                                            </div>
                                        </c:if>
                                        <c:if test="${report.confirmed && (event.moderator.id == sessionScope.user.id || report.speaker.id == sessionScope.user.id)}">
                                            <div class="row">
                                                <button type="button" class="btn btn-outline-danger col-6 offset-3 cancel-report-btn">Cancel report</button>
                                            </div>
                                        </c:if>
                                    </div>
                                    <span class="col-3 text-end">${report.speaker.firstName} ${report.speaker.lastName}</span>
                                </div>
                            </a>
                        </c:forEach>
                    </a>
                    <c:choose>
                        <c:when test="${sessionScope.user.role == 'USER'}">
                                <c:if test="${isParticipant}">
                                    <button id="leave-btn" type="button" class="btn btn-danger mb-2">Leave from the conference</button>
                                </c:if>
                                <c:if test="${!isParticipant}">
                                    <button id="join-btn" type="button" class="btn btn-primary">Join to the conference</button>
                                </c:if>
                        </c:when>
                        <c:when test="${sessionScope.user.role == 'SPEAKER'}">
                            <c:choose>
                                <c:when test="${isParticipant}">
                                    <button id="leave-btn" type="button" class="btn btn-danger mb-2">Leave from the conference</button>
                                </c:when>
                                <c:when test="${!isParticipant && !hasReport}">
                                    <button type="button" class="btn btn-secondary mb-2" data-bs-toggle="modal" data-bs-target="#offer-report-modal">Offer report</button>
                                    <button id="join-btn" type="button" class="btn btn-primary">Join to the conference</button>
                                </c:when>
                                <c:otherwise>
                                    <button type="button" class="btn btn-secondary mb-2" data-bs-toggle="modal" data-bs-target="#offer-report-modal">Offer report</button>
                                </c:otherwise>
                            </c:choose>
                        </c:when>
                        <c:when test="${sessionScope.user.role == 'MODERATOR'}">
                            <c:if test="${event.moderator.id == sessionScope.user.id}">
                                <button type="button" class="btn btn-secondary mb-2" data-bs-toggle="modal" data-bs-target="#offer-report-modal">Offer report</button>
                                <c:if test="${event.hidden}">
                                    <button id="show-event-btn" type="button" class="btn btn-success">Show the conference</button>
                                </c:if>
                                <c:if test="${!event.hidden}">
                                    <button id="hide-event-btn" type="button" class="btn btn-danger">Hide the conference</button>
                                </c:if>
                            </c:if>
                            <c:if test="${event.moderator.id != sessionScope.user.id}">
                                <c:if test="${isParticipant}">
                                    <button id="leave-btn" type="button" class="btn btn-danger mb-2">Leave from the conference</button>
                                </c:if>
                                <c:if test="${!isParticipant}">
                                    <button id="join-btn" type="button" class="btn btn-primary">Join to the conference</button>
                                </c:if>
                            </c:if>
                        </c:when>
                    </c:choose>
                </div>
            </div>
        </div>
    </div>
</div>

<c:choose>
    <%--Offer report modal (speaker)--%>
    <c:when test="${sessionScope.user.role == 'SPEAKER' && !isParticipant}">
        <div class="modal fade" id="offer-report-modal" tabindex="-1" aria-hidden="true">
            <div class="modal-dialog modal-dialog-centered">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title">New report</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <form>
                        <div class="modal-body" speaker-id="${sessionScope.user.id}">
                            <input type="text" id="report-topic" class="form-control is-invalid" placeholder="Topic">
                            <div class="invalid-feedback">
                                Min length: 3
                            </div>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Close</button>
                            <button id="offer-report-btn-speaker" type="submit" class="btn btn-primary">Offer report</button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </c:when>
    <%--Offer report modal (moderator)--%>
    <c:when test="${sessionScope.user.id == event.moderator.id}">
        <div class="modal fade" id="offer-report-modal" tabindex="-1" aria-hidden="true">
            <div class="modal-dialog modal-dialog-centered">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title">New report</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <form>
                        <div class="modal-body">
                            <input type="text" id="report-topic" class="form-control is-invalid mb-2" placeholder="Topic">
                            <div class="invalid-feedback mb-2">
                                Min length: 3
                            </div>
                            <input class="form-control is-invalid" list="speakers" id="speaker" user-id="-1">
                            <datalist id="speakers"></datalist>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Close</button>
                            <button id="offer-report-btn-moderator" type="submit" class="btn btn-primary">Offer report</button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
        <%--Modify title modal--%>
        <div class="modal fade" id="modify-title-modal" tabindex="-1"aria-hidden="true">
            <div class="modal-dialog modal-dialog-centered">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title">New title</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <form>
                        <div class="modal-body">
                            <input type="text" id="new-event-title" class="form-control" placeholder="Title" value="${event.title}">
                            <div class="invalid-feedback">
                                Min length: 3
                            </div>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Close</button>
                            <button id="save-title-btn" type="submit" class="btn btn-primary">Save title</button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
        <%--Modify description modal--%>
        <div class="modal fade" id="modify-description-modal" tabindex="-1" aria-hidden="true">
            <div class="modal-dialog modal-dialog-centered">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title">New description</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <textarea type="text" id="new-event-description" class="form-control" rows="10" placeholder="Description">${event.description}</textarea>
                        <div class="invalid-feedback">
                            Min length: 20
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Close</button>
                        <button id="save-description-btn" type="button" class="btn btn-primary">Save description</button>
                    </div>
                </div>
            </div>
        </div>
        <%--Modify date modal--%>
        <div class="modal fade" id="modify-date-modal" tabindex="-1" aria-hidden="true">
            <div class="modal-dialog modal-dialog-centered">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title">New date</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <form>
                        <div class="modal-body">
                            <input type="datetime-local" id="new-event-date" class="form-control" placeholder="Datetime" value="<fmt:formatDate value="${event.date}" pattern="yyyy-MM-dd HH:mm"/>" required>
                            <div class="invalid-feedback">
                                Required future date
                            </div>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Close</button>
                            <button id="save-date-btn" type="submit" class="btn btn-primary">Save date</button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
        <%--Modify place modal--%>
        <div class="modal fade" id="modify-place-modal" tabindex="-1"aria-hidden="true">
            <div class="modal-dialog modal-dialog-centered">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title">New place</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <form>
                        <div class="modal-body">
                            <input type="text" id="new-event-place" class="form-control" placeholder="Place" value="${event.place}">
                            <div class="invalid-feedback">
                                Min length: 5
                            </div>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Close</button>
                            <button id="save-place-btn" type="submit" class="btn btn-primary">Save place</button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
        <%--Participants modal--%>
        <div class="modal fade" id="participants-modal" tabindex="-1" aria-hidden="true">
            <div class="modal-dialog modal-dialog-centered modal-dialog-scrollable">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title">Participants</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <ul class="list-group">
                            <c:forEach items="${event.participants}" var="participant">
                                <li class="list-group-item">${participant.firstName} ${participant.lastName}
                                    <c:if test="${participant.role != 'USER'}">
                                        <span class="text-lowercase">(${participant.role})</span>
                                    </c:if>
                                    <br>
                                    <span class="fst-italic fw-light">${participant.email}</span>
                                </li>
                            </c:forEach>
                        </ul>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Close</button>
                    </div>
                </div>
            </div>
        </div>
        <div class="modal fade" id="modify-report-topic-modal" tabindex="-1" aria-hidden="true">
            <div class="modal-dialog modal-dialog-centered">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title">New topic</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <form>
                        <div class="modal-body">
                            <input type="text" id="new-report-topic" class="form-control" placeholder="Topic">
                            <div class="invalid-feedback">
                                Min length: 3
                            </div>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Close</button>
                            <button id="save-report-topic-btn" type="submit" class="btn btn-primary">Save topic</button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </c:when>
</c:choose>

<script src="js/bootstrap.min.js"></script>
<script src="js/jquery-3.6.0.min.js"></script>
<script src="js/sidebar.js"></script>
<script src="js/event.js"></script>
<c:if test="${sessionScope.user.role != 'USER'}">
    <script src="js/event-speaker.js"></script>
</c:if>
<c:if test="${sessionScope.user.id == event.moderator.id}">
    <script src="js/event-moderator.js"></script>
</c:if>
</body>
</html>

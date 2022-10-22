<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="my" tagdir="/WEB-INF/tags" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <link href="css/bootstrap.min.css" rel="stylesheet">
    <link href="css/sidebar.css" rel="stylesheet">
    <title>Users</title>
</head>
<body>
<my:sidebar/>

<div class="container-fluid">
    <div class="row">
        <div class="col-9 offset-3">
            <div class="row">
                <div class="col-10 offset-1 mt-3">
                    <div class="input-group">
                        <input type="email" id="email-input" class="form-control" placeholder="Email@example.com">
                        <button id="search-by-email" class="btn btn-outline-dark" type="button">Search</button>
                    </div>
                    <table class="table align-middle table-striped table-borderless mt-2">
                        <thead class="table-dark">
                        <th>ID</th>
                        <th>Email</th>
                        <th>First name</th>
                        <th>Last name</th>
                        <th>Role</th>
                        </thead>
                        <tbody id="table-body">
                        <c:forEach items="${users}" var="user">
                            <tr>
                                <th>${user.id}</th>
                                <td>${user.email}</td>
                                <td>${user.firstName}</td>
                                <td>${user.lastName}</td>
                                <td>
                                    <button type="button" class="btn btn-dark btn-sm col-8 table-role-btn"
                                            data-bs-toggle="modal" data-bs-target="#role-modal">
                                            <span class="text-lowercase text-capitalize">${user.role.toString().toLowerCase()}</span>
                                    </button>
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                    <button type="button" id="load-more-btn" class="btn btn-dark col-12">Load more</button>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- New role modal -->
<div class="modal fade" id="role-modal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">New role</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <div class="modal-body">
                <table class="table align-middle table-striped table-borderless mt-2">
                    <thead class="table-dark">
                    <th>ID</th>
                    <th>Email</th>
                    <th>First name</th>
                    <th>Last name</th>
                    <th>Role</th>
                    </thead>
                    <tbody>
                    <tr>
                        <th>1</th>
                        <td>timereim@gmail.com</td>
                        <td>Rostyslav</td>
                        <td>Yavorskiy</td>
                        <td>
                            <select id="modal-role-select" class="form-select">
                                <option value="USER">User</option>
                                <option value="SPEAKER">Speaker</option>
                                <option value="MODERATOR">Moderator</option>
                            </select>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Close</button>
                <button type="button" id="save-role-btn" class="btn btn-primary" data-bs-dismiss="modal">Save role</button>
            </div>
        </div>
    </div>
</div>

<!-- Moderator events -->
<div class="modal fade" id="moderator-events" tabindex="-1">
    <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">Moderator has events</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <div class="modal-body">
                <table class="table align-middle table-striped table-borderless mt-2">
                    <h6>You will become the moderator of these events (<span id="events-count">1</span>)</h6>
                    <thead class="table-dark">
                    <th>ID</th>
                    <th>Title</th>
                    <th>Date</th>
                    </thead>
                    <tbody>
                    <tr>
                        <th>1</th>
                        <td><a href="event?id=4" target="_blank">What is programming?</a></td>
                        <td>15-11-2022 16:00</td>
                    </tr>
                    </tbody>
                </table>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                <button type="button" class="btn btn-primary okay-btn" data-bs-dismiss="modal">Okay</button>
            </div>
        </div>
    </div>
</div>

<!-- Speaker reports -->
<div class="modal fade" id="speaker-reports" tabindex="-1">
    <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">Speaker has reports</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <div class="modal-body">
                <table class="table align-middle table-striped table-borderless mt-2">
                    <h6>These future reports will be deleted (<span id="reports-count">1</span>)</h6>
                    <thead class="table-dark">
                    <th>ID</th>
                    <th>Topic</th>
                    <th>Event title</th>
                    <th>Date</th>
                    </thead>
                    <tbody>
                    <tr>
                        <th>1</th>
                        <td>Modern programming</td>
                        <td><a href="event?id=4" target="_blank">What is programming?</a></td>
                        <td>15-11-2022 16:00</td>
                    </tr>
                    </tbody>
                </table>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                <button type="button" class="btn btn-primary okay-btn" data-bs-dismiss="modal">Okay</button>
            </div>
        </div>
    </div>
</div>

<script src="js/bootstrap.min.js"></script>
<script src="js/jquery-3.6.0.min.js"></script>
<script src="js/sidebar.js"></script>
<script src="js/users.js"></script>
</body>
</html>
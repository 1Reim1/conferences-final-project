<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link href="css/bootstrap.min.css" rel="stylesheet">
    <link href="css/event.css" rel="stylesheet">
    <title>Create event</title>
</head>
<body>

<sidebar class="col-3 bg-light">
    <div class="position-relative col-12">
        <h3 class="text-center">Conferences</h3>
        <hr>
        <button type="button" class="btn btn-dark col-12 text-start">Home</button>
        <button type="button" class="btn btn-dark col-12 text-start">My events</button>
        <button type="button" class="btn btn-primary col-12 text-start">My profile</button>
        <div class="down-menu col-12 position-absolute bottom-0 start-0">
            <p style="font-size: 12px">You are logged as
                <span class="text-lowercase">(${sessionScope.user.role})</span>
            </p>
            <button type="button" class="btn btn-primary col-12 text-start" id="logout-btn">Logout</button>
        </div>
    </div>
</sidebar>

<div class="container-fluid">
    <div class="row">
        <div class="col-9 offset-3">
            <div class="container">
                <div class="row">
                    <div class="alert alert-danger" id="error-alert" role="alert" style="text-align: center; display: none"></div>
                    <div class="col-10 offset-1">
                        <h2 class="text-center mb-3 mt-2">New event</h2>
                        <form>
                            <div class="mb-3">
                                <label for="title" class="form-label">Title</label>
                                <input type="text" class="form-control" id="title">
                                <div class="invalid-feedback">
                                    Min length: 3
                                </div>
                            </div>
                            <div class="mb-3">
                                <label for="description" class="form-label">Description</label>
                                <textarea type="text" id="description" class="form-control" rows="6"></textarea>
                                <div class="invalid-feedback">
                                    Min length: 20
                                </div>
                            </div>
                            <div class="mb-3">
                                <label for="place" class="form-label">Place</label>
                                <input type="text" class="form-control" id="place">
                                <div class="invalid-feedback">
                                    Min length: 5
                                </div>
                            </div>
                            <div class="mb-3">
                                <label for="date" class="form-label">Date</label>
                                <input type="datetime-local" id="date" class="form-control">
                                <div class="invalid-feedback">
                                    Required future date
                                </div>
                            </div>
                            <button id="create-btn" type="submit" class="btn btn-primary col-12">Create event</button>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<script src="js/jquery-3.6.0.min.js"></script>
<script src="js/bootstrap.min.js"></script>
<script src="js/validate-functions.js"></script>
<script src="js/new-event.js"></script>
</body>
</html>
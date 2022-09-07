function validateSpeakerSelect() {
    let select = $("#speaker")
    let datalist = $("#speakers")

    let valid = false
    datalist.children().each(function () {
        if ($(this).val().toLowerCase() === select.val().toLowerCase()) {
            valid = true
            select.attr("user-id", $(this).attr("user-id"))
            return false
        }
    })

    if (!valid) {
        select.removeClass("is-valid")
        select.addClass("is-invalid")
        select.attr("user-id", -1)
        return false
    }
    select.addClass("is-valid")
    select.removeClass("is-invalid")
    return true
}

$("#speaker").on("autocompletechange keyup", function (e) {
    if($(this).val().length > 0) {
        let datalist = $("#speakers")
        $.ajax({
            type: "POST",
            url: window.location.href,
            data: {
                command: "search-speaker",
                eventId: $("#event").attr("event-id"),
                searchQuery: $(this).val()
            },
            success: function (data, status, xhr) {
                let speakers = $.parseJSON(data)
                console.log("speakers loaded")
                datalist.empty()
                speakers.forEach((speaker) => {
                    datalist.append('<option user-id="' + speaker.id + '" value="' + speaker.email + '">' + speaker.firstName + ' ' + speaker.lastName + '</option>')
                })
                validateSpeakerSelect()
            },
            error: function (jqXhr, textStatus, errorMessage) {
                datalist.empty()
                validateSpeakerSelect()
            }
        })
    }
})

$("#offer-report-btn-moderator").on("click", function (e) {
    e.preventDefault()
    if(validateTopic("#report-topic") && validateSpeakerSelect()) {
        $.ajax({
            type: "POST",
            url: window.location.href,
            data: {
                command: "offer-report",
                eventId: $("#event").attr("event-id"),
                topic: $("#report-topic").val(),
                speakerId: $("#speaker").attr("user-id")
            },
            success: function (data, status, xhr) {
                location.reload()
            },
            error: function (jqXhr, textStatus, errorMessage) {
                console.log(jqXhr.responseText)
                $("#error-alert").text(jqXhr.responseText)
                $("#error-alert").fadeIn("slow")
            }
        })
    }
})

$("#hide-event-btn").on("click", function (e) {
    $.ajax({
        type: "POST",
        url: window.location.href,
        data: {
            command: "hide",
            eventId: $("#event").attr("event-id"),
        },
        success: function (data, status, xhr) {
            location.reload()
        },
        error: function (jqXhr, textStatus, errorMessage) {
            console.log(jqXhr.responseText)
            $("#error-alert").text(jqXhr.responseText)
            $("#error-alert").fadeIn("slow")
        }
    })
})

$("#show-event-btn").on("click", function (e) {
    $.ajax({
        type: "POST",
        url: window.location.href,
        data: {
            command: "show",
            eventId: $("#event").attr("event-id"),
        },
        success: function (data, status, xhr) {
            location.reload()
        },
        error: function (jqXhr, textStatus, errorMessage) {
            console.log(jqXhr.responseText)
            $("#error-alert").text(jqXhr.responseText)
            $("#error-alert").fadeIn("slow")
        }
    })
})

function validateTitle(element) {
    if ($.trim(element.val()).length < 3) {
        element.removeClass("is-valid")
        element.addClass("is-invalid")
        return false
    }
    element.addClass("is-valid")
    element.removeClass("is-invalid")
    return true
}

$("#new-event-title").on("autocompletechange keyup", function (e) {
    validateTitle($(this))
})

$("#save-title-btn").on("click", function (e) {
    e.preventDefault()
    let newEventTitle = $("#new-event-title")
    if (validateTitle(newEventTitle)) {
        $.ajax({
            type: "POST",
            url: window.location.href,
            data: {
                command: "modify-title",
                eventId: $("#event").attr("event-id"),
                title: $.trim(newEventTitle.val())
            },
            success: function (data, status, xhr) {
                $("#event-title").text($.trim(newEventTitle.val()))
            },
            error: function (jqXhr, textStatus, errorMessage) {
                console.log(jqXhr.responseText)
                $("#error-alert").text(jqXhr.responseText)
                $("#error-alert").fadeIn("slow")
            }
        })
    }
})

function validateDescription(element) {
    if ($.trim(element.val()).length < 20) {
        element.removeClass("is-valid")
        element.addClass("is-invalid")
        return false
    }
    element.addClass("is-valid")
    element.removeClass("is-invalid")
    return true
}

$("#new-event-description").on("autocompletechange keyup", function (e) {
    validateDescription($(this))
})

$("#save-description-btn").on("click", function (e) {
    e.preventDefault()
    let newEventDescription = $("#new-event-description")
    if (validateDescription(newEventDescription)) {
        $.ajax({
            type: "POST",
            url: window.location.href,
            data: {
                command: "modify-description",
                eventId: $("#event").attr("event-id"),
                description: $.trim(newEventDescription.val())
            },
            success: function (data, status, xhr) {
                $("#event-description").text($.trim(newEventDescription.val()))
            },
            error: function (jqXhr, textStatus, errorMessage) {
                console.log(jqXhr.responseText)
                $("#error-alert").text(jqXhr.responseText)
                $("#error-alert").fadeIn("slow")
            }
        })
    }
})

$("#new-event-date").ready(function (e) {
    let now = new Date();
    now.setMinutes(now.getMinutes() - now.getTimezoneOffset());
    $("#new-event-date").attr("min", now.toISOString().slice(0, 16));
})

$("#save-date-btn").on("click", function (e) {
    e.preventDefault()
    let newEventDateInput = $("#new-event-date")
    let newEventDate = new Date(newEventDateInput.val()).getTime()
    let now = new Date().getTime()
    if (newEventDate < now) {
        newEventDateInput.removeClass("is-valid")
        newEventDateInput.addClass("is-invalid")
        return false
    }
    newEventDateInput.addClass("is-valid")
    newEventDateInput.removeClass("is-invalid")
    $.ajax({
        type: "POST",
        url: window.location.href,
        data: {
            command: "modify-date",
            eventId: $("#event").attr("event-id"),
            date: newEventDate
        },
        success: function (data, status, xhr) {
            let d = new Date(newEventDateInput.val())
            let dateString = ("0" + d.getDate()).slice(-2) + "-" + ("0"+(d.getMonth()+1)).slice(-2) + "-" +
                d.getFullYear() + " " + ("0" + d.getHours()).slice(-2) + ":" + ("0" + d.getMinutes()).slice(-2);
            $("#event-date").text(dateString)
            $("#error-alert").fadeOut("fast")
        },
        error: function (jqXhr, textStatus, errorMessage) {
            console.log(jqXhr.responseText)
            $("#error-alert").text(jqXhr.responseText)
            $("#error-alert").fadeIn("slow")
        }
    })
})
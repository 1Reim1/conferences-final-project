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

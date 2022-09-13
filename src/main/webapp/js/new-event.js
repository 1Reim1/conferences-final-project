$("#title").on("autocompletechange keyup", function (e) {
    validateTitle($(this))
})

$("#description").on("autocompletechange keyup", function (e) {
    validateDescription($(this))
})

$("#place").on("autocompletechange keyup", function (e) {
    validatePlace($(this))
})

$("#create-btn").on("click", function (e) {
    e.preventDefault()
    let title = $("#title")
    let description = $("#description")
    let place = $("#place");
    let date = $("#date")

    if (validateTitle(title) && validateDescription(description) && validatePlace(place) && validateDate(date)) {
        $.ajax({
            type: "POST",
            url: window.location.href,
            data: {
                title: $.trim(title.val()),
                description: $.trim(description.val()),
                place: $.trim(place.val()),
                date: new Date(date.val()).getTime()
            },
            success: function (data, status, xhr) {
                window.location.href = "event?id=" + data
            },
            error: function (jqXhr, textStatus, errorMessage) {
                console.log(jqXhr.responseText)
                $("#error-alert").text(jqXhr.responseText)
                $("#error-alert").fadeIn("slow")
            }
        })
    }
})
$("#logout-btn").on("click", function (e) {
    window.location.href = "auth"
})

$("#home-btn").on("click", function (e) {
    window.location.href = "home"
})

$("#create-event-btn").on("click", function (e) {
    window.location.href = "create-event"
})

$("#new-reports-btn").on("click", function (e) {
    window.location.href = "new-reports"
})

$("#ukrainian-language-btn").on("click", function (e) {
    document.cookie = "lang=uk"
    $.ajax({
        type: "POST",
        url: "select-language",
        data: {
            language: "uk"
        },
        success: function (data, status, xhr) {
            location.reload()
        },
        error: function (jqXhr, textStatus, errorMessage) {
            console.log(jqXhr.responseText)
        }
    })
})

$("#english-language-btn").on("click", function (e) {
    document.cookie = "lang=en"
    $.ajax({
        type: "POST",
        url: "select-language",
        data: {
            language: "en"
        },
        success: function (data, status, xhr) {
            location.reload()
        },
        error: function (jqXhr, textStatus, errorMessage) {
            console.log(jqXhr.responseText)
        }
    })
})
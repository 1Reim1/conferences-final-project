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
    location.reload()
})

$("#english-language-btn").on("click", function (e) {
    document.cookie = "lang=en"
    location.reload()
})
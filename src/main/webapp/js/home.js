
$("#logout-btn").on("click", function (e) {
    window.location.href = "auth"
})

$("#order-date").on("click", function (e) {
    document.cookie = "event-order=DATE"
    location.reload()
})

$("#order-reports").on("click", function (e) {
    document.cookie = "event-order=REPORTS"
    location.reload()
})

$("#order-participants").on("click", function (e) {
    document.cookie = "event-order=PARTICIPANTS"
    location.reload()
})
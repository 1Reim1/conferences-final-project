
$("#logout-btn").on("click", function (e) {
    window.location.href = "auth"
})

$("#order-date").on("click", function (e) {
    window.location.href = "?order=DATE"
})

$("#order-reports").on("click", function (e) {
    window.location.href = "?order=REPORTS"
})

$("#order-participants").on("click", function (e) {
    window.location.href = "?order=PARTICIPANTS"
})
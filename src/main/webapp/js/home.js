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

$("#order-past").on("click", function (e) {
    document.cookie = "event-order-time-past=true"
    location.reload()
})

$("#order-future").on("click", function (e) {
    document.cookie = "event-order-time-past=; expires=Thu, 01 Jan 1970 00:00:00 UTC;"
    location.reload()
})

$("#order-reverse").on("click", function (e) {
    let cookie = getCookie("event-order-reverse")
    if (cookie === "") {
        document.cookie = "event-order-reverse=true"
    }   else {
        document.cookie = "event-order-reverse=; expires=Thu, 01 Jan 1970 00:00:00 UTC;"
    }
    location.reload()
})

$("#order-my-events").on("click", function (e) {
    document.cookie = "event-order-my-events=true"
    location.reload()
})

$("#order-all-events").on("click", function (e) {
    document.cookie = "event-order-my-events=; expires=Thu, 01 Jan 1970 00:00:00 UTC;"
    location.reload()
})

function getCookie(cname) {
    let name = cname + "=";
    let ca = document.cookie.split(';');
    for(let i = 0; i < ca.length; i++) {
        let c = ca[i];
        while (c.charAt(0) == ' ') {
            c = c.substring(1);
        }
        if (c.indexOf(name) == 0) {
            return c.substring(name.length, c.length);
        }
    }
    return "";
}

$(".event-card").on("click", function (e) {
    window.location.href = "event?id=" + $(this).attr("event-id")
})
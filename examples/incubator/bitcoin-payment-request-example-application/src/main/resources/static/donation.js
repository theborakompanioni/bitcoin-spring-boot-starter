
const httpGetAsync = (url, onSuccess, onError) => {
    const xmlHttp = new XMLHttpRequest();
    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState === 4) {
            if (xmlHttp.status === 200) {
                onSuccess(xmlHttp.responseText);
            } else {
                onError(xmlHttp);
            }
        }
    }

    xmlHttp.open("GET", url, true); // true for asynchronous
    xmlHttp.send(null);
};

const errorIndicatorElement = document.getElementById("error-indicator");
const loadingIndicatorElement = document.getElementById("loading-indicator");
loadingIndicatorElement.textContent = "loading...";

let queryParams = new URLSearchParams(window.location.search);
let donationId = queryParams.get("donation_id");

httpGetAsync("/api/v1/donation/" + donationId, (donationJson) => {
    loadingIndicatorElement.textContent = "";

    const donationMap = JSON.parse(donationJson);

    document.getElementById("payment-amount").textContent = donationMap.displayPrice;
    document.getElementById("payment-to").textContent = donationMap.paymentUrl;
    document.getElementById("payment-seconds-left").textContent = '3600';

    document.getElementById("tbk-btc-payment-info-container").removeAttribute("hidden");

    // generate qr code from the plain payment request
    var qr = new VanillaQR({
        url: donationMap.paymentUrl,
        size: 250
    });

    const elemQrCodeContainer = document.getElementById("qrcode-container");

    // remove all previous generated payment request qrcodes
    while (elemQrCodeContainer.firstChild) {
        elemQrCodeContainer.removeChild(elemQrCodeContainer.lastChild);
    }

    // add plain payment request as qrcode
    elemQrCodeContainer.prepend(qr.domElement);
    elemQrCodeContainer.removeAttribute("hidden");

    /*
    const rawDataElement = document.getElementById("tbk-btc-data-container");
    rawDataElement.textContent = JSON.stringify(donationMap);
    rawDataElement.removeAttribute("hidden");
    */
}, (e) => {
    loadingIndicatorElement.textContent = "";

    errorIndicatorElement.textContent = "error : (";
    errorIndicatorElement.removeAttribute("hidden");
});
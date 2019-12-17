var map;
var storeArray = new Array;
var storeCount = 0;
var shopArray = new Array();
var shopCount = 0;
function getCookie(cname) {
    var name = cname + "=";
    var decodedCookie = decodeURIComponent(document.cookie);
    var ca = decodedCookie.split(';');
    for(var i = 0; i <ca.length; i++) {
        var c = ca[i];
        while (c.charAt(0) == ' ') {
            c = c.substring(1);
        }
        if (c.indexOf(name) == 0) {
            return c.substring(name.length, c.length);
        }
    }
    return "";
}

function load(){
    storesShopsMarkers();

    for(var i=0; i<storeCount; i++)
    {
        addMarkerStore(storeArray[i].data1, storeArray[i].data2, " ");
    }
    for(var i=0; i<shopCount; i++)
    {
        addMarkerShop(shopArray[i].data1, shopArray[i].data2, " ");
    }
}
function addMarkerStore(lat, lng, txt) {
    var opcjeMarkera =
        {
            position: new google.maps.LatLng(lat, lng),
            map: map,
            label: {

                text: txt,
                fontWeight: "bold",
                fontSize: "12px",
                color: "black",

            },
            icon: {
                url: "http://maps.google.com/mapfiles/kml/pal3/icon49.png",
                scaledSize: new google.maps.Size(20, 20)
            }
        }
    var marker = new google.maps.Marker(opcjeMarkera);
    marker.txt = txt;

    return marker;
}

function addMarkerShop(lat, lng, txt) {
    var opcjeMarkera =
        {
            position: new google.maps.LatLng(lat, lng),
            map: map,
            label: {

                text: txt,
                fontWeight: "bold",
                fontSize: "12px",
                color: "black",

            },
            icon: {
                url: "http://maps.google.com/mapfiles/kml/pal2/icon26.png",
                scaledSize: new google.maps.Size(20, 20),
            }
        }
    var marker = new google.maps.Marker(opcjeMarkera);
    marker.txt = txt;

    return marker;
}
function storesShopsMarkers(){
    var headerText = $('h1').html();
    if(headerText == "Magazyny"){
        if(getCookie("storeCount")==""){
            noStores();
            shops();
        }
        else{
            stores();
            shops();
        }
    }
    else{
        if(getCookie("shopCount")==""){
            noShops();
            stores();
        }
        else{
            shops();
            stores();
        }
    }
}

function noStores(){
    var n = document.getElementsByClassName('name');
    var x = document.getElementsByClassName('lon');
    var y = document.getElementsByClassName('lat');
    var i = 0;
    $('.wiersz').each(function () {
        var data1 = x[i].innerHTML;
        var data2 = y[i].innerHTML;
        var data3 = n[i].innerHTML;
        storeArray.push({data1, data2, data3});
        document.cookie = "storeArray" + i + "data1=" + storeArray[i].data1;
        document.cookie = "storeArray" + i + "data2=" + storeArray[i].data2;
        document.cookie = "storeArray" + i + "data3=" + storeArray[i].data3;
        i++;
        storeCount++;
    });
    document.cookie = "storeCount=" + storeCount;
}
function stores(){
    storeCount = getCookie("storeCount");
    for(var i=0; i<storeCount; i++){
        var data1 = getCookie("storeArray" + i + "data1");
        var data2 = getCookie("storeArray" + i + "data2");
        var data3 = getCookie("storeArray" + i + "data3");
        storeArray.push({data1, data2, data3});

    }
}
function noShops(){
    var n = document.getElementsByClassName('name');
    var x = document.getElementsByClassName('lon');
    var y = document.getElementsByClassName('lat');
    var i = 0;
    $('.wiersz').each(function () {
        var data1 = x[i].innerHTML;
        var data2 = y[i].innerHTML;
        var data3 = n[i].innerHTML;
        shopArray.push({data1, data2, data3});
        document.cookie = "shopArray" + i + "data1=" + shopArray[i].data1;
        document.cookie = "shopArray" + i + "data2=" + shopArray[i].data2;
        document.cookie = "shopArray" + i + "data3=" + shopArray[i].data3;
        shopCount++;
        i++;
    });
    document.cookie = "shopCount=" + shopCount;
}
function shops(){
    shopCount = getCookie("shopCount");
    for(var i=0; i<shopCount; i++){
        var data1 = getCookie("shopArray" + i + "data1");
        var data2 = getCookie("shopArray" + i + "data2");
        var data3 = getCookie("shopArray" + i + "data3");
        shopArray.push({data1, data2, data3});
    }
}


function initMap(){
    var directionsService = new google.maps.DirectionsService;
    var directionsDisplay = new google.maps.DirectionsRenderer;
    map = new google.maps.Map(document.getElementById('map'), {
        center: {lat: 54.371765, lng: 18.611966},
        zoom: 13,
        mapTypeId: 'roadmap'
    });
    directionsDisplay.setMap(map);

    var onChangeHandler = function() {
        calculateAndDisplayRoute(directionsService, directionsDisplay);
    };
    document.getElementById("myBtn").addEventListener("click", onChangeHandler);
}
function calculateAndDisplayRoute(directionsService, directionsDisplay) {
    var rows = document.getElementById("tabela_dostawy").getElementsByTagName("tr").length;
    var nr = document.getElementById("nr_dostawy").value;

    var storelat = document.getElementById("tabela_dostawy").rows[nr].cells[4].innerHTML;
    var storelng = document.getElementById("tabela_dostawy").rows[nr].cells[5].innerHTML;
    var magazyn = new google.maps.LatLng(storelat, storelng);
    var przystanki = [];
    var przystankiMarker = [];

    for(var i=nr;i<rows;i++)
    {
        var trasa = document.getElementById("tabela_dostawy").rows[i].cells[11].innerHTML;
        if(trasa == nr)
        {
            var shoplat = document.getElementById("tabela_dostawy").rows[i].cells[2].innerHTML;
            var shoplng = document.getElementById("tabela_dostawy").rows[i].cells[3].innerHTML;
            var sklep = new google.maps.LatLng(shoplat, shoplng);
            przystanki.push({location: sklep, stopover: true});
            przystankiMarker.push({shoplat, shoplng, nr});
        }

    }

    directionsService.route({
        origin: magazyn,
        destination: magazyn,
        waypoints: przystanki,
        travelMode: 'DRIVING'
    }, function(response, status) {
        if (status === 'OK') {
            directionsDisplay.setOptions({suppressMarkers: true});
            directionsDisplay.setDirections(response);
        } else {
            window.alert('Directions request failed due to ' + status);
        }
    });

    load();
    addMarkerStore(storelat, storelng, "X");
    for(var i=0;i<przystankiMarker.length;i++){
        addMarkerShop(przystankiMarker[i].shoplat, przystankiMarker[i].shoplng,String.fromCharCode(65+i));
    }
}
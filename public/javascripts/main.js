var mymap = L.map('mapid').setView([50.06, 19.94], 13);

L.tileLayer('http://{s}.tile.openstreetmap.de/tiles/osmde/{z}/{x}/{y}.png', {
           	maxZoom: 18,
           	attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>'
           }).addTo(mymap);

var markers = []

function update() {
    var request = new XMLHttpRequest();
    request.onreadystatechange = function() {
        if (request.readyState == 4) {
            if(request.status == 200) {
                /* */
                console.log(request.responseText);
                var table = JSON.parse(request.responseText);
                for(var i = 0; i < table.trams.length; i++){
                    if(markers[table.trams[i].id] !== undefined && table.trams[i].isDeleted){
                        mymap.removeLayer(markers[table.trams[i].id]);
                        delete markers[table.trams[i].id];
                    }
                    if(typeof markers[table.trams[i].id] === 'undefined')
                        markers[table.trams[i].id] = new L.Marker.movingMarker([[table.trams[i].lat, table.trams[i].lon]], []).bindPopup(table.trams[i].name).addTo(mymap);
                    else
                        markers[table.trams[i].id].moveTo([table.trams[i].lat,table.trams[i].lon], 5000);
                }
                for(var i = 0; i < table.deleted.length; i++){
                    if(markers[table.deleted[i]] != undefined){
                        mymap.removeLayer(markers[table.deleted[i]]);
                        delete markers[table.deleted[i]];
                    }
                }
            } else {
                /* Something went wrong */
                alert("NOT GREAT");
            }
        }
    }
    request.open('GET', "/data-request", true);
    request.send();
    setTimeout(update, 50); //bo on ten timeout liczy od zakończenia się ruchu więc powinno 'prawie nie być' a dla mniejszych nie działa : (
}

update();
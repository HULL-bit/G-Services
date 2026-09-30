/* G-SERVICES — cartographie (Leaflet + OpenStreetMap, sans clé API).
   Chargé sur les écrans Géographie. Tolère l'absence de Leaflet. */
(function () {
    "use strict";

    var TILES = "https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png";
    var ATTR = "&copy; OpenStreetMap";

    function ready() {
        return typeof window.L !== "undefined";
    }

    /**
     * Pin de marque G-SERVICES (DivIcon CSS/PrimeIcons — aucune image externe).
     * @param {string} [icone]  classe PrimeIcons affichée dans le pin (ex. "pi pi-shopping-bag")
     * @param {boolean}[gros]   pin plus grand (fiche prestataire)
     */
    window.gsIcon = function (icone, gros) {
        var taille = gros ? 40 : 32;
        return L.divIcon({
            className: "gs-pin" + (gros ? " gs-pin--lg" : ""),
            html: '<span class="gs-pin__dot"><i class="' + esc(icone || "pi pi-map-marker") + '"></i></span><span class="gs-pin__tip"></span>',
            iconSize: [taille, taille + 10],
            iconAnchor: [taille / 2, taille + 8],
            popupAnchor: [0, -taille]
        });
    };

    /**
     * Carte de consultation : place un marqueur par ville géolocalisée.
     * @param {string} elId   id du conteneur
     * @param {Array}  points [{lat,lng,libelle,pays,icone,url}]
     */
    window.gsMapView = function (elId, points) {
        if (!ready()) { return; }
        var el = document.getElementById(elId);
        if (!el) { return; }
        if (el._gsmap) { el._gsmap.remove(); }

        var map = L.map(el, { scrollWheelZoom: false });
        el._gsmap = map;
        L.tileLayer(TILES, { attribution: ATTR, maxZoom: 19 }).addTo(map);

        var group = [];
        (points || []).forEach(function (p) {
            var m = L.marker([p.lat, p.lng], { icon: gsIcon(p.icone, points.length === 1) }).addTo(map);
            var html = "<strong>" + esc(p.libelle) + "</strong>"
                + (p.pays ? "<br/>" + esc(p.pays) : "");
            if (p.url) {
                html += '<br/><a href="' + esc(p.url) + '" class="gs-map-link">Voir la fiche →</a>';
            }
            m.bindPopup(html);
            group.push([p.lat, p.lng]);
        });
        if (group.length > 1) {
            map.fitBounds(group, { padding: [40, 40], maxZoom: 14 });
        } else if (group.length === 1) {
            map.setView(group[0], 15);
        } else {
            map.setView([14.5, -3], 3);
        }
        setTimeout(function () { map.invalidateSize(); }, 200);
    };

    /**
     * Carte de saisie : un marqueur déplaçable ; chaque déplacement/clic
     * recopie les coordonnées dans les champs .js-lat / .js-lng du conteneur parent.
     */
    window.gsMapPicker = function (elId, lat, lng) {
        if (!ready()) { return; }
        var el = document.getElementById(elId);
        if (!el) { return; }
        if (el._gsmap) { el._gsmap.remove(); }

        var start = [
            isFinite(lat) && lat !== 0 ? lat : 14.6928,
            isFinite(lng) && lng !== 0 ? lng : -17.4467
        ];
        var map = L.map(el);
        el._gsmap = map;
        L.tileLayer(TILES, { attribution: ATTR, maxZoom: 18 }).addTo(map);
        map.setView(start, (isFinite(lat) && lat !== 0) ? 12 : 5);

        var marker = L.marker(start, { draggable: true, icon: gsIcon("pi pi-map-marker", true) }).addTo(map);
        function sync(ll) {
            marker.setLatLng(ll);
            setField(el, "js-lat", ll.lat.toFixed(6));
            setField(el, "js-lng", ll.lng.toFixed(6));
        }
        marker.on("dragend", function () { sync(marker.getLatLng()); });
        map.on("click", function (e) { sync(e.latlng); });
        setTimeout(function () { map.invalidateSize(); }, 200);
    };

    /**
     * Carte « itinéraire » : affiche le point du prestataire, géolocalise le
     * visiteur (API navigateur) puis trace le trajet réel sur les tuiles OSM
     * via OSRM (moteur de routage libre, aucune clé, aucun renvoi vers un
     * service tiers type Google Maps). Se dégrade proprement si l'utilisateur
     * refuse la géolocalisation ou si le service de routage est indisponible.
     * @param {string} elId  id du conteneur
     * @param {{lat:number,lng:number,libelle:string,icone:string,pays:string}} dest
     */
    window.gsItineraire = function (elId, dest) {
        if (!ready() || !dest) { return; }
        var el = document.getElementById(elId);
        if (!el) { return; }
        if (el._gsmap) { el._gsmap.remove(); }

        var map = L.map(el, { scrollWheelZoom: false });
        el._gsmap = map;
        L.tileLayer(TILES, { attribution: ATTR, maxZoom: 19 }).addTo(map);
        map.setView([dest.lat, dest.lng], 14);

        var destMarker = L.marker([dest.lat, dest.lng], { icon: gsIcon(dest.icone, true) }).addTo(map);
        destMarker.bindPopup("<strong>" + esc(dest.libelle) + "</strong>" + (dest.pays ? "<br/>" + esc(dest.pays) : ""));

        var note = document.createElement("div");
        note.className = "gs-itineraire-note";
        note.textContent = "Localisation en cours…";
        el.parentNode.insertBefore(note, el.nextSibling);

        function tracerLigneDirecte(origine) {
            L.polyline([origine, [dest.lat, dest.lng]], { color: "#c9a227", weight: 3, dashArray: "6 8" }).addTo(map);
            map.fitBounds([origine, [dest.lat, dest.lng]], { padding: [40, 40] });
        }

        function poserPosition(origine) {
            L.marker(origine, {
                icon: L.divIcon({
                    className: "gs-pin gs-pin--moi",
                    html: '<span class="gs-pin__dot gs-pin__dot--moi"><i class="pi pi-user"></i></span>',
                    iconSize: [26, 26], iconAnchor: [13, 13]
                })
            }).addTo(map).bindPopup("Vous êtes ici");
        }

        function ok(position) {
            var origine = [position.coords.latitude, position.coords.longitude];
            poserPosition(origine);

            if (typeof L.Routing === "undefined") {
                note.remove();
                tracerLigneDirecte(origine);
                return;
            }

            var ctrl = L.Routing.control({
                waypoints: [L.latLng(origine), L.latLng(dest.lat, dest.lng)],
                router: L.Routing.osrmv1({ serviceUrl: "https://router.project-osrm.org/route/v1", language: "fr" }),
                addWaypoints: false,
                draggableWaypoints: false,
                fitSelectedRoutes: true,
                show: true,
                lineOptions: { styles: [{ color: "#1f3a5f", weight: 5, opacity: .85 }] },
                createMarker: function () { return null; }
            }).addTo(map);

            ctrl.on("routesfound", function (e) {
                var r = e.routes[0];
                var km = (r.summary.totalDistance / 1000).toFixed(1);
                var min = Math.round(r.summary.totalTime / 60);
                note.textContent = km + " km • environ " + min + " min en voiture";
            });
            ctrl.on("routingerror", function () {
                map.removeControl(ctrl);
                note.textContent = "Itinéraire indicatif (service de routage indisponible)";
                tracerLigneDirecte(origine);
            });
        }

        function ko() {
            note.textContent = "Activez la localisation pour afficher votre itinéraire.";
        }

        if (navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(ok, ko, { timeout: 8000, maximumAge: 60000 });
        } else {
            ko();
        }
        setTimeout(function () { map.invalidateSize(); }, 200);
    };

    function setField(scope, cls, value) {
        var host = scope.closest(".gs-map-wrap") || scope.parentNode;
        var input = host.querySelector("." + cls + " input") || host.querySelector("." + cls);
        if (input) {
            input.value = value;
            input.dispatchEvent(new Event("change", { bubbles: true }));
        }
    }

    function esc(s) {
        return String(s == null ? "" : s).replace(/[&<>"]/g, function (c) {
            return { "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;" }[c];
        });
    }
})();

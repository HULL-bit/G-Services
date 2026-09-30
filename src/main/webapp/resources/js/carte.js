/* ============================================================================
   G-SERVICES — page publique « Carte » : carte Leaflet + filtres client + liste.
   Données injectées par CarteBean dans window.GS_CARTE (tableau de services).
   ========================================================================== */
(function (window, document) {
  "use strict";

  var DATA = window.GS_CARTE || [];
  var map, layer, markersById = {};
  var f = { texte: "", categorie: "", note: 0, dispo: false };

  function ready() { return typeof L !== "undefined"; }

  function esc(s) {
    return String(s == null ? "" : s).replace(/[&<>"]/g, function (c) {
      return { "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;" }[c];
    });
  }

  function etoiles(n) {
    var p = Math.round(n);
    var out = "";
    for (var i = 1; i <= 5; i++) out += i <= p ? "★" : "☆";
    return out;
  }

  function match(s) {
    if (f.texte) {
      var q = f.texte.toLowerCase();
      if ((s.libelle + " " + s.categorie + " " + s.adresse).toLowerCase().indexOf(q) === -1) return false;
    }
    if (f.categorie && s.categorie !== f.categorie) return false;
    if (f.note && s.note < f.note) return false;
    if (f.dispo && !s.dispo) return false;
    return true;
  }

  function render() {
    var visibles = DATA.filter(match);

    // ---- carte
    if (layer) { layer.clearLayers(); } else { layer = L.layerGroup().addTo(map); }
    var bounds = [];
    markersById = {};
    visibles.forEach(function (s) {
      var icon = (window.gsIcon ? window.gsIcon(s.icone) : undefined);
      var m = L.marker([s.lat, s.lng], icon ? { icon: icon } : {}).addTo(layer);
      markersById[s.id] = m;
      m.bindPopup(
        '<strong>' + esc(s.libelle) + '</strong><br/>' +
        '<span style="color:#C9A227">' + etoiles(s.note) + '</span> ' + s.note.toFixed(1) +
        ' (' + s.avis + ')<br/>' + esc(s.adresse) +
        '<br/><a class="gs-map-link" href="' + esc(s.url) + '">Voir la fiche &#8594;</a>');
      m.on("mouseover", function () { highlight(s.id, true); });
      m.on("mouseout", function () { highlight(s.id, false); });
      bounds.push([s.lat, s.lng]);
    });
    if (bounds.length) map.fitBounds(bounds, { padding: [40, 40], maxZoom: 14 });

    // ---- liste
    var list = document.getElementById("carteList");
    if (list) {
      list.innerHTML = visibles.length ? "" :
        '<div class="gs-empty -mini"><p>Aucun prestataire ne correspond à ces filtres.</p></div>';
      visibles.forEach(function (s) {
        var el = document.createElement("article");
        el.className = "gs-carte-item";
        el.setAttribute("data-sid", s.id);
        el.innerHTML =
          '<span class="gs-carte-item__icon"><i class="' + esc(s.icone || "pi pi-map-marker") + '"></i></span>' +
          '<div class="gs-carte-item__body">' +
            '<h3>' + esc(s.libelle) + '</h3>' +
            '<p class="gs-hint">' + esc(s.categorie) + ' · ' + esc(s.adresse) + '</p>' +
            '<p class="gs-carte-item__meta">' +
              '<span class="gs-stars">' + etoiles(s.note) + '</span> ' + s.note.toFixed(1) + ' (' + s.avis + ')' +
              '<span class="gs-badge ' + (s.dispo ? "-ok" : "-off") + '">' + (s.dispo ? "Ouvert" : "Fermé") + '</span>' +
            '</p>' +
          '</div>' +
          '<a class="gs-btn -sm" href="' + esc(s.url) + '">Voir</a>';
        el.addEventListener("mouseenter", function () { flyTo(s); highlight(s.id, true); });
        el.addEventListener("mouseleave", function () { highlight(s.id, false); });
        list.appendChild(el);
      });
    }
    var count = document.getElementById("carteCount");
    if (count) count.textContent = visibles.length;
  }

  function highlight(id, on) {
    var item = document.querySelector('.gs-carte-item[data-sid="' + id + '"]');
    if (item) item.classList.toggle("is-hover", on);
    var m = markersById[id];
    if (m && m._icon) m._icon.classList.toggle("gs-pin--active", on);
  }
  function flyTo(s) {
    if (map) map.setView([s.lat, s.lng], Math.max(map.getZoom(), 14), { animate: true });
  }

  function bindFilters() {
    var t = document.getElementById("fTexte");
    var c = document.getElementById("fCategorie");
    var n = document.getElementById("fNote");
    var d = document.getElementById("fDispo");
    var reset = document.getElementById("fReset");
    if (t) t.addEventListener("input", function () { f.texte = t.value; render(); });
    if (c) c.addEventListener("change", function () { f.categorie = c.value; render(); });
    if (n) n.addEventListener("change", function () {
      f.note = parseInt(n.value, 10) || 0;
      document.querySelectorAll("#fNoteStars i").forEach(function (st, i) {
        st.className = (i < f.note) ? "pi pi-star-fill" : "pi pi-star";
      });
      render();
    });
    if (d) d.addEventListener("change", function () { f.dispo = d.checked; render(); });
    if (reset) reset.addEventListener("click", function () {
      f = { texte: "", categorie: "", note: 0, dispo: false };
      if (t) t.value = ""; if (c) c.value = ""; if (n) n.value = "0"; if (d) d.checked = false;
      document.querySelectorAll("#fNoteStars i").forEach(function (st) { st.className = "pi pi-star"; });
      render();
    });
    // étoiles cliquables
    document.querySelectorAll("#fNoteStars i").forEach(function (st, i) {
      st.addEventListener("click", function () {
        f.note = (f.note === i + 1) ? 0 : i + 1;
        if (n) n.value = String(f.note);
        document.querySelectorAll("#fNoteStars i").forEach(function (x, j) {
          x.className = (j < f.note) ? "pi pi-star-fill" : "pi pi-star";
        });
        render();
      });
    });
  }

  function boot() {
    var el = document.getElementById("carteMap");
    if (!el || !ready()) { if (!ready()) { setTimeout(boot, 150); } return; }
    map = L.map(el, { scrollWheelZoom: true });
    L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png",
      { attribution: "&copy; OpenStreetMap", maxZoom: 19 }).addTo(map);
    map.setView([14.72, -17.46], 12);
    bindFilters();
    render();
    setTimeout(function () { map.invalidateSize(); }, 250);
  }

  if (document.readyState === "loading") document.addEventListener("DOMContentLoaded", boot);
  else boot();
})(window, document);

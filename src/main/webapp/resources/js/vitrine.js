/* ============================================================================
   G-SERVICES — couche cinématique du front public (catalogue / fiche service).
   Complète effects.js. Vanilla, sans dépendance. Respecte prefers-reduced-motion.
   ========================================================================== */
(function (window, document) {
  "use strict";

  var REDUCED = window.matchMedia("(prefers-reduced-motion: reduce)").matches;

  /* ---------------------------------------------------- Transition de page - */
  //  Entrée : le <main> monte en fondu. Sortie : fondu avant navigation interne.
  function initPageTransition() {
    var main = document.getElementById("gs-main");
    if (!main || main.__gsTrans) return;
    main.__gsTrans = true;

    if (!REDUCED) {
      main.classList.add("gs-page-enter");
      requestAnimationFrame(function () {
        requestAnimationFrame(function () { main.classList.add("is-in"); });
      });
    }

    document.addEventListener("click", function (ev) {
      if (REDUCED || ev.defaultPrevented || ev.button !== 0 || ev.metaKey || ev.ctrlKey) return;
      var a = ev.target.closest('a[href]');
      if (!a) return;
      var url = a.getAttribute("href") || "";
      if (a.target === "_blank" || url.charAt(0) === "#" || url.indexOf("javascript:") === 0) return;
      if (a.hostname && a.hostname !== window.location.hostname) return;
      // navigation interne : on lance le fondu de sortie puis on suit le lien
      ev.preventDefault();
      document.body.classList.add("gs-leaving");
      setTimeout(function () { window.location.href = a.href; }, 220);
    });
  }
  // au retour arrière navigateur (bfcache) : on ré-affiche
  window.addEventListener("pageshow", function (e) {
    if (e.persisted) document.body.classList.remove("gs-leaving");
  });

  /* ---------------------------------------------------- Parallaxe du hero -- */
  function initHeroParallax() {
    if (REDUCED) return;
    var hero = document.querySelector(".gs-vitrine-hero");
    if (!hero || hero.__gsPara) return;
    hero.__gsPara = true;
    var ticking = false;
    function update() {
      var y = window.scrollY;
      if (y < window.innerHeight) {
        hero.style.backgroundPositionY = "calc(50% + " + (y * 0.18).toFixed(1) + "px)";
        var inner = hero.querySelector(".gs-container");
        if (inner) inner.style.transform = "translateY(" + (y * 0.06).toFixed(1) + "px)";
        if (inner) inner.style.opacity = String(Math.max(0, 1 - y / (window.innerHeight * 0.7)));
      }
      ticking = false;
    }
    window.addEventListener("scroll", function () {
      if (!ticking) { requestAnimationFrame(update); ticking = true; }
    }, { passive: true });
    update();
  }

  /* ---------------------------------------------------- Étoiles animées ---- */
  //  Remplit la barre dorée de gauche à droite quand elle entre dans l'écran.
  function initStars() {
    var stars = document.querySelectorAll(".gs-stars[data-note]:not([data-stars-done])");
    if (!stars.length) return;
    function fill(el) {
      el.setAttribute("data-stars-done", "");
      var pct = Math.max(0, Math.min(100, (parseFloat(el.getAttribute("data-note")) || 0) / 5 * 100));
      el.style.setProperty("--gs-star-fill", REDUCED ? pct + "%" : "0%");
      if (!REDUCED) {
        requestAnimationFrame(function () {
          el.style.transition = "none";
          requestAnimationFrame(function () { el.style.setProperty("--gs-star-fill", pct + "%"); });
        });
      }
    }
    if (REDUCED || !("IntersectionObserver" in window)) { stars.forEach(fill); return; }
    var io = new IntersectionObserver(function (entries) {
      entries.forEach(function (e) { if (e.isIntersecting) { fill(e.target); io.unobserve(e.target); } });
    }, { threshold: 0.5 });
    stars.forEach(function (el) { io.observe(el); });
  }

  /* ---------------------------------------------------- Barres de notes ---- */
  function initBars() {
    var bars = document.querySelectorAll(".gs-avis-bar__fill[data-bar]:not([data-bar-done])");
    if (!bars.length) return;
    function grow(el) {
      el.setAttribute("data-bar-done", "");
      var pct = (parseFloat(el.getAttribute("data-bar")) || 0) + "%";
      if (REDUCED) { el.style.width = pct; return; }
      el.style.transition = "width .9s var(--gs-ease)";
      requestAnimationFrame(function () { el.style.width = pct; });
    }
    if (REDUCED || !("IntersectionObserver" in window)) { bars.forEach(grow); return; }
    var io = new IntersectionObserver(function (entries) {
      entries.forEach(function (e, i) {
        if (e.isIntersecting) { setTimeout(function () { grow(e.target); }, i * 60); io.unobserve(e.target); }
      });
    }, { threshold: 0.4 });
    bars.forEach(function (el) { io.observe(el); });
  }

  /* ---------------------------------------------------- Ripple sur les chips */
  function initChipRipple() {
    if (document.__gsChipRipple || REDUCED) return;
    document.__gsChipRipple = true;
    document.addEventListener("click", function (ev) {
      var chip = ev.target.closest(".gs-cat-chip");
      if (!chip) return;
      var d = Math.max(chip.clientWidth, chip.clientHeight) * 1.4;
      var r = chip.getBoundingClientRect();
      var s = document.createElement("span");
      s.className = "gs-ripple";
      s.style.width = s.style.height = d + "px";
      s.style.left = (ev.clientX - r.left - d / 2) + "px";
      s.style.top = (ev.clientY - r.top - d / 2) + "px";
      chip.appendChild(s);
      setTimeout(function () { s.remove(); }, 600);
    });
  }

  /* ---------------------------------------------------- Squelette au switch  */
  //  Exposé pour les p:ajax onstart / oncomplete du catalogue.
  window.gsCatalogueLoading = function (on) {
    var list = document.getElementById("catList") ||
               document.querySelector('[id$=":catList"], .gs-service-grid');
    if (!list) return;
    if (on) {
      list.classList.add("gs-loading");
    } else {
      list.classList.remove("gs-loading");
      initStars();
      initBars();
      if (window.GS && window.GS.init) window.GS.init();
    }
  };

  /* ---------------------------------------------------- Orchestration ------ */
  function boot() {
    initPageTransition();
    initHeroParallax();
    initStars();
    initBars();
    initChipRipple();
  }
  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", boot);
  } else {
    boot();
  }
  if (window.faces && window.faces.ajax) {
    window.faces.ajax.addOnEvent(function (d) { if (d.status === "success") { initStars(); initBars(); } });
  }
})(window, document);

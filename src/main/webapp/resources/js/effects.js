/* ============================================================================
   G-SERVICES — couche d'animation / micro-interactions (vanilla, sans dépendance)
   Tout est idempotent : GS.init() peut être rappelé après un rendu AJAX JSF.
   ========================================================================== */
(function (window, document) {
  "use strict";

  var REDUCED = window.matchMedia("(prefers-reduced-motion: reduce)").matches;
  var GS = { version: "lot0" };

  /* ---------------------------------------------------- Apparition au scroll */
  function initReveal() {
    var els = document.querySelectorAll("[data-aos]:not([data-aos-done])");
    if (!els.length) return;

    if (REDUCED || !("IntersectionObserver" in window)) {
      els.forEach(function (el) { el.classList.add("is-visible"); el.setAttribute("data-aos-done", ""); });
      return;
    }
    var io = new IntersectionObserver(function (entries) {
      entries.forEach(function (e) {
        if (!e.isIntersecting) return;
        var el = e.target;
        var delay = parseInt(el.getAttribute("data-aos-delay") || "0", 10);
        el.style.transitionDelay = delay + "ms";
        el.classList.add("is-visible");
        el.setAttribute("data-aos-done", "");
        io.unobserve(el);
      });
    }, { threshold: 0.15, rootMargin: "0px 0px -8% 0px" });
    els.forEach(function (el) { io.observe(el); });
  }

  /* ---------------------------------------------------- Compteurs animés ---- */
  function easeOutCubic(t) { return 1 - Math.pow(1 - t, 3); }

  function runCountup(el) {
    var target = parseFloat(el.getAttribute("data-countup")) || 0;
    var decimals = parseInt(el.getAttribute("data-decimals") || "0", 10);
    var suffix = el.getAttribute("data-suffix") || "";
    var duration = 1600;

    if (REDUCED) { el.textContent = target.toFixed(decimals) + suffix; return; }

    var start = performance.now();
    function frame(now) {
      var p = Math.min((now - start) / duration, 1);
      var val = target * easeOutCubic(p);
      el.textContent = val.toFixed(decimals) + suffix;
      if (p < 1) requestAnimationFrame(frame);
      else el.textContent = target.toFixed(decimals) + suffix;
    }
    requestAnimationFrame(frame);
  }

  function initCountup() {
    var els = document.querySelectorAll("[data-countup]:not([data-countup-done])");
    if (!els.length) return;
    if (!("IntersectionObserver" in window)) { els.forEach(runCountup); return; }
    var io = new IntersectionObserver(function (entries) {
      entries.forEach(function (e) {
        if (!e.isIntersecting) return;
        e.target.setAttribute("data-countup-done", "");
        runCountup(e.target);
        io.unobserve(e.target);
      });
    }, { threshold: 0.6 });
    els.forEach(function (el) { io.observe(el); });
  }

  /* ---------------------------------------------------- Machine à écrire --- */
  function initTypewriter() {
    var el = document.querySelector("[data-typewriter]:not([data-tw-done])");
    if (!el) return;
    el.setAttribute("data-tw-done", "");
    var words = (el.getAttribute("data-words") || "").split("|").filter(Boolean);
    if (!words.length) return;

    if (REDUCED) { el.textContent = words[0]; return; }

    var wi = 0, ci = 0, deleting = false;
    function tick() {
      var word = words[wi];
      el.textContent = word.substring(0, ci);
      if (!deleting && ci < word.length) { ci++; setTimeout(tick, 70); }
      else if (!deleting && ci === word.length) { deleting = true; setTimeout(tick, 1500); }
      else if (deleting && ci > 0) { ci--; setTimeout(tick, 35); }
      else { deleting = false; wi = (wi + 1) % words.length; setTimeout(tick, 350); }
    }
    tick();
  }

  /* ---------------------------------------------------- Header au scroll --- */
  function initHeader() {
    var header = document.getElementById("gs-header");
    if (!header || header.__gsBound) return;
    header.__gsBound = true;
    var onScroll = function () {
      header.classList.toggle("-scrolled", window.scrollY > 40);
    };
    onScroll();
    window.addEventListener("scroll", onScroll, { passive: true });
  }

  /* ---------------------------------------------------- Parallaxe légère --- */
  function initParallax() {
    if (REDUCED) return;
    var els = Array.prototype.slice.call(document.querySelectorAll("[data-parallax]"));
    if (!els.length || window.__gsParallax) return;
    window.__gsParallax = true;
    var ticking = false;
    function update() {
      var vh = window.innerHeight;
      els.forEach(function (el) {
        var speed = parseFloat(el.getAttribute("data-parallax")) || 0.15;
        var rect = el.parentElement.getBoundingClientRect();
        var offset = (rect.top + rect.height / 2 - vh / 2) * -speed;
        el.style.transform = "translate3d(0," + offset.toFixed(1) + "px,0)";
      });
      ticking = false;
    }
    window.addEventListener("scroll", function () {
      if (!ticking) { requestAnimationFrame(update); ticking = true; }
    }, { passive: true });
    update();
  }

  /* ---------------------------------------------------- Ripple sur boutons - */
  function initRipple() {
    if (document.__gsRipple) return;          // délégué : on ne l'attache qu'une fois
    document.__gsRipple = true;
    document.addEventListener("click", function (ev) {
      var btn = ev.target.closest(".gs-btn");
      if (!btn || REDUCED) return;
      var circle = document.createElement("span");
      var d = Math.max(btn.clientWidth, btn.clientHeight);
      var r = btn.getBoundingClientRect();
      circle.className = "gs-ripple";
      circle.style.width = circle.style.height = d + "px";
      circle.style.left = (ev.clientX - r.left - d / 2) + "px";
      circle.style.top = (ev.clientY - r.top - d / 2) + "px";
      btn.appendChild(circle);
      setTimeout(function () { circle.remove(); }, 600);
    });
  }

  /* ---------------------------------------------------- Thème clair/sombre - */
  function applyThemeIcon() {
    var btn = document.getElementById("gs-theme-toggle");
    if (!btn) return;
    var dark = document.documentElement.getAttribute("data-theme") === "dark";
    var icon = btn.querySelector("i");
    if (icon) icon.className = dark ? "pi pi-sun" : "pi pi-moon";
    btn.setAttribute("aria-pressed", String(dark));
  }
  function initTheme() {
    var btn = document.getElementById("gs-theme-toggle");
    if (btn && !btn.__gsBound) {
      btn.__gsBound = true;
      btn.addEventListener("click", function () {
        var cur = document.documentElement.getAttribute("data-theme");
        var next = cur === "dark" ? "light" : "dark";
        document.documentElement.setAttribute("data-theme", next);
        try { localStorage.setItem("gs-theme", next); } catch (e) {}
        applyThemeIcon();
      });
    }
    applyThemeIcon();
  }

  /* ---------------------------------------------------- Vidéos de fond ---- */
  function initVideos() {
    var vids = document.querySelectorAll("video[data-bg-video]");
    vids.forEach(function (v) {
      if (v.__gsBound) return;
      v.__gsBound = true;
      v.muted = true;
      v.setAttribute("muted", "");
      if (REDUCED) { try { v.pause(); } catch (e) {} return; }
      var tryPlay = function () { var p = v.play(); if (p && p.catch) p.catch(function () {}); };
      if (v.readyState >= 2) tryPlay();
      v.addEventListener("canplay", tryPlay, { once: true });
      // Économie de ressources : on coupe la lecture quand la vidéo sort de l'écran.
      if ("IntersectionObserver" in window) {
        new IntersectionObserver(function (entries) {
          entries.forEach(function (e) { e.isIntersecting ? tryPlay() : v.pause(); });
        }, { threshold: 0.05 }).observe(v);
      }
    });
  }

  /* ---------------------------------------------------- Carrousel vitrine -- */
  function initCarousel() {
    var root = document.querySelector("[data-carousel]");
    if (!root || root.__gsBound) return;
    root.__gsBound = true;

    var track = root.querySelector(".gs-carousel__track");
    var slides = Array.prototype.slice.call(track.children);
    var dotsWrap = root.querySelector(".gs-carousel__nav");
    var prev = root.querySelector('[data-carousel-prev]');
    var next = root.querySelector('[data-carousel-next]');
    var timer = null;
    var current = 0;

    var dots = slides.map(function (_, i) {
      var d = document.createElement("button");
      d.type = "button";
      d.className = "gs-carousel__dot" + (i === 0 ? " is-active" : "");
      d.setAttribute("aria-label", "Diapositive " + (i + 1));
      d.addEventListener("click", function () { goTo(i, true); });
      dotsWrap.insertBefore(d, dotsWrap.querySelector('[data-carousel-next]'));
      return d;
    });

    function centerOf(el) { return el.offsetLeft + el.offsetWidth / 2 - track.clientWidth / 2; }

    function goTo(i, user) {
      current = (i + slides.length) % slides.length;
      track.scrollTo({ left: centerOf(slides[current]), behavior: REDUCED ? "auto" : "smooth" });
      updateUi();
      if (user) restart();
    }
    function updateUi() {
      dots.forEach(function (d, i) { d.classList.toggle("is-active", i === current); });
      var mid = track.scrollLeft + track.clientWidth / 2;
      slides.forEach(function (s) {
        var c = s.offsetLeft + s.offsetWidth / 2;
        s.classList.toggle("-dim", Math.abs(c - mid) > s.offsetWidth * 0.7);
      });
    }
    function restart() {
      if (timer) clearInterval(timer);
      if (REDUCED) return;
      timer = setInterval(function () { goTo(current + 1); }, 5000);
    }

    if (prev) prev.addEventListener("click", function () { goTo(current - 1, true); });
    if (next) next.addEventListener("click", function () { goTo(current + 1, true); });
    root.addEventListener("mouseenter", function () { if (timer) clearInterval(timer); });
    root.addEventListener("mouseleave", restart);
    var scrolling = false;
    track.addEventListener("scroll", function () {
      if (scrolling) return;
      scrolling = true;
      window.requestAnimationFrame(function () { updateUi(); scrolling = false; });
    }, { passive: true });
    window.addEventListener("resize", function () { goTo(current); });

    updateUi();
    restart();
  }

  /* ---------------------------------------------------- Orchestration ----- */
  GS.init = function () {
    initHeader();
    initTheme();
    initReveal();
    initCountup();
    initTypewriter();
    initParallax();
    initRipple();
    initVideos();
    initCarousel();
  };

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", GS.init);
  } else {
    GS.init();
  }

  /* Re-jouer les révélations après un rendu partiel JSF/PrimeFaces. */
  if (window.faces && window.faces.ajax) {
    window.faces.ajax.addOnEvent(function (data) {
      if (data.status === "success") { initReveal(); initCountup(); initTheme(); }
    });
  } else if (window.jsf && window.jsf.ajax) {
    window.jsf.ajax.addOnEvent(function (data) {
      if (data.status === "success") { initReveal(); initCountup(); initTheme(); }
    });
  }

  window.GS = GS;
})(window, document);

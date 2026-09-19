// PathPulse bridge between WebView JS and Kotlin.
// The Kotlin side sends JSON commands via evaluateJavascript; this module routes them.
// Each command targets the currently-active engine ("leaflet" or "maplibre") unless a
// target is explicitly named.

window.PathPulseBridge = (function () {
  "use strict";

  // ---------- shared helpers ----------
  function postResult(id, payload) {
    if (window.PathPulseJava) {
      // Gson will call this with {id, result, error} from the JS -> Kotlin path.
      window.PathPulseJava.onJsResult(id, payload);
    }
  }

  function errorResult(id, message) {
    postResult(id, { error: message });
  }

  // Each engine exposes its own module; we keep a dict of named-engine handlers.
  var engines = {};

  // ---------- match-and-dispatch ----------
  function dispatch(cmd) {
    var payload = JSON.parse(cmd);
    // { id, method, engine?, params }
    var engName = payload.engine || activeEngine();
    var fn = engines[engName] && engines[engName][payload.method];
    if (!fn) {
      return errorResult(payload.id, "No method " + payload.method + " on engine " + engName);
    }
    try {
      var res = fn(payload.params || {});
      // If the engine returns a thenable (Promise), we await it and report.
      if (res && typeof res.then === "function") {
        res.then(function (v) { postResult(payload.id, v); })
           .catch(function (e) { errorResult(payload.id, String(e)); });
        return;
      }
      postResult(payload.id, res);
    } catch (e) {
      errorResult(payload.id, String(e));
    }
  }

  // ---------- engine registry ----------
  function register(name, api) {
    engines[name] = api;
  }

  function activeEngine() {
    var leaflet = document.querySelector('[data-engine="leaflet"]');
    var maplibre = document.querySelector('[data-engine="maplibre"]');
    if (!maplibre || !maplibre.classList.contains("active")) {
      if (leaflet && leaflet.classList.contains("active")) return "leaflet";
    }
    if (maplibre && maplibre.classList.contains("active")) return "maplibre";
    // fallback: first engine that has a map instance ready
    if (engines["leaflet"] && engines["leaflet"].map) return "leaflet";
    if (engines["maplibre"] && engines["maplibre"].map) return "maplibre";
    return "leaflet";
  }

  // ---------- command channel from Kotlin ----------
  function receive(cmd) {
    try { dispatch(cmd); }
    catch (e) { errorResult("__bridge__", String(e)); }
  }

  // ---------- start / stop ----------
  function start(engineName) {
    // hide/show the two div engines
    var leafletEl = document.querySelector('[data-engine="leaflet"]');
    var maplibreEl = document.querySelector('[data-engine="maplibre"]');
    if (engineName === "leaflet") {
      leafletEl.classList.add("active");
      maplibreEl.classList.remove("active");
    } else {
      maplibreEl.classList.add("active");
      leafletEl.classList.remove("active");
    }
    // The engine itself takes care of initializing its map on first show; if already
    // initialized, we ensure it is visible/attribution settable.
  }

  function stop() {
    // nothing heavy; the map div visibility change is enough.
  }

  return {
    register: register,
    receive: receive,
    start: start,
    stop: stop,
    activeEngine: activeEngine,
  };
})();


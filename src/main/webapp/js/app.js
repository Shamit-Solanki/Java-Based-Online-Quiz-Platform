/* JavaQuiz Arena - shared UI behaviour (no dependencies) */
(function () {
  'use strict';
  var $ = function (s, r) { return (r || document).querySelector(s); };
  var $$ = function (s, r) { return Array.prototype.slice.call((r || document).querySelectorAll(s)); };

  /* ---- toasts ---------------------------------------------------- */
  window.toast = function (text, type) {
    var box = $('#toasts'); if (!box) return;
    var t = document.createElement('div');
    t.className = 'toast ' + (type || 'info');
    t.innerHTML = '<span></span><button aria-label="Dismiss">×</button>';
    t.firstChild.textContent = text;
    t.lastChild.onclick = function () { t.remove(); };
    box.appendChild(t);
    setTimeout(function () { t.classList.add('out'); setTimeout(function () { t.remove(); }, 400); }, 5000);
  };
  $$('.flash-data').forEach(function (f) { toast(f.dataset.text, f.dataset.type); });

  /* ---- theme + mobile menu --------------------------------------- */
  var themeBtn = $('#themeBtn');
  if (themeBtn) themeBtn.onclick = function () {
    var next = document.documentElement.dataset.theme === 'dark' ? 'light' : 'dark';
    document.documentElement.dataset.theme = next;
    try { localStorage.setItem('theme', next); } catch (e) {}
  };
  var menuBtn = $('#menuBtn'), sidebar = $('#sidebar'), scrim = $('#scrim');
  function toggleMenu(open) { if (sidebar) { sidebar.classList.toggle('open', open); scrim.classList.toggle('show', open); } }
  if (menuBtn) { menuBtn.onclick = function () { toggleMenu(!sidebar.classList.contains('open')); }; scrim.onclick = function () { toggleMenu(false); }; }

  /* ---- CSRF token on every POST form ----------------------------- */
  var meta = $('meta[name=csrf]');
  if (meta && meta.content) $$('form').forEach(function (f) {
    if ((f.method || '').toLowerCase() === 'post' && !f.querySelector('input[name=_csrf]')) {
      var i = document.createElement('input'); i.type = 'hidden'; i.name = '_csrf'; i.value = meta.content; f.appendChild(i);
    }
  });

  /* ---- modals (<dialog>) ----------------------------------------- */
  $$('[data-open]').forEach(function (b) {
    b.addEventListener('click', function () {
      var d = document.getElementById(b.dataset.open); if (!d) return;
      $$('[data-fill]', d).forEach(function (el) {
        var v = b.dataset[el.dataset.fill]; if (v === undefined) return;
        if (el.type === 'checkbox') el.checked = v === 'true'; else el.value = v;
      });
      var t = $('[data-title]', d); if (t && b.dataset.title) t.textContent = b.dataset.title;
      d.showModal();
    });
  });
  $$('dialog').forEach(function (d) {
    d.addEventListener('click', function (e) { if (e.target === d) d.close(); });
    $$('[data-close]', d).forEach(function (b) { b.onclick = function () { d.close(); }; });
  });

  /* ---- confirm on destructive / important forms ------------------ */
  $$('form[data-confirm]').forEach(function (f) {
    f.addEventListener('submit', function (e) { if (!confirm(f.dataset.confirm)) e.preventDefault(); });
  });
  $$('a[data-confirm]').forEach(function (a) {
    a.addEventListener('click', function (e) { if (!confirm(a.dataset.confirm)) e.preventDefault(); });
  });

  /* ---- disable submit buttons after click (no double submit) ----- */
  $$('form').forEach(function (f) {
    f.addEventListener('submit', function (e) {
      if (e.defaultPrevented) return;
      var s = e.submitter; if (!s || f.hasAttribute('data-multi')) return;
      setTimeout(function () { $$('button[type=submit],button:not([type])', f).forEach(function (b) { b.disabled = true; }); }, 0);
      if (s.name) { var h = document.createElement('input'); h.type = 'hidden'; h.name = s.name; h.value = s.value; f.appendChild(h); }
    });
  });

  /* ---- live table filter + sortable headers ---------------------- */
  $$('[data-filter-table]').forEach(function (input) {
    var table = document.getElementById(input.dataset.filterTable); if (!table) return;
    var sel = $$('[data-filter-col]').filter(function (s) { return s.dataset.for === table.id; });
    function apply() {
      var q = input.value.toLowerCase(), shown = 0;
      $$('tbody tr', table).forEach(function (tr) {
        var ok = tr.textContent.toLowerCase().indexOf(q) > -1;
        sel.forEach(function (s) { if (s.value && tr.dataset[s.dataset.filterCol] !== s.value) ok = false; });
        tr.hidden = !ok; if (ok) shown++;
      });
      var empty = $('.empty-row', table.parentNode); if (empty) empty.hidden = shown > 0;
    }
    input.addEventListener('input', apply); sel.forEach(function (s) { s.addEventListener('change', apply); });
  });
  $$('table.sortable th[data-sort]').forEach(function (th) {
    th.addEventListener('click', function () {
      var table = th.closest('table'), idx = th.cellIndex, asc = th.dataset.dir !== 'asc';
      $$('th', table).forEach(function (x) { delete x.dataset.dir; }); th.dataset.dir = asc ? 'asc' : 'desc';
      var rows = $$('tbody tr', table);
      rows.sort(function (a, b) {
        var x = a.cells[idx].dataset.v || a.cells[idx].textContent.trim(), y = b.cells[idx].dataset.v || b.cells[idx].textContent.trim();
        var nx = parseFloat(x), ny = parseFloat(y);
        var r = (!isNaN(nx) && !isNaN(ny)) ? nx - ny : x.localeCompare(y);
        return asc ? r : -r;
      });
      rows.forEach(function (r) { r.parentNode.appendChild(r); });
    });
  });

  /* ---- tabs (status filters) ------------------------------------- */
  $$('[data-tabs]').forEach(function (bar) {
    var table = document.getElementById(bar.dataset.tabs);
    $$('button', bar).forEach(function (b) {
      b.addEventListener('click', function () {
        $$('button', bar).forEach(function (x) { x.classList.remove('on'); }); b.classList.add('on');
        var v = b.dataset.value, shown = 0;
        $$('tbody tr, .card-item', table).forEach(function (tr) { var ok = !v || tr.dataset.status === v; tr.hidden = !ok; if (ok) shown++; });
        var empty = $('.empty-row', table.parentNode); if (empty) empty.hidden = shown > 0;
      });
    });
  });

  /* ---- count-up numbers and animated rings ----------------------- */
  $$('[data-count]').forEach(function (el) {
    var end = parseFloat(el.dataset.count), dec = (el.dataset.count.split('.')[1] || '').length, t0 = null;
    if (isNaN(end) || window.matchMedia('(prefers-reduced-motion: reduce)').matches) return;
    function step(ts) { t0 = t0 || ts; var p = Math.min(1, (ts - t0) / 700); el.textContent = (end * (1 - Math.pow(1 - p, 3))).toFixed(dec); if (p < 1) requestAnimationFrame(step); }
    requestAnimationFrame(step);
  });
  requestAnimationFrame(function () { $$('.ring').forEach(function (r) { r.style.setProperty('--p', r.dataset.value); }); });

  /* ---- chat: scroll to latest ------------------------------------ */
  var thread = $('.thread'); if (thread) thread.scrollTop = thread.scrollHeight;
  $$('textarea[data-counter]').forEach(function (ta) {
    var c = document.getElementById(ta.dataset.counter);
    function u() { if (c) c.textContent = ta.value.length + ' / ' + ta.maxLength; } ta.addEventListener('input', u); u();
  });
  $$('.quick-login').forEach(function (b) {
    b.onclick = function () { $('#email').value = b.dataset.email; $('#password').value = b.dataset.pw; $('#password').focus(); };
  });
  $$('.pw-toggle').forEach(function (b) {
    b.onclick = function () { var i = document.getElementById(b.dataset.target); i.type = i.type === 'password' ? 'text' : 'password'; };
  });

  /* ================================================================
     Quiz builder (creator)
     ================================================================ */
  var builder = $('#questionList');
  if (builder) {
    var tpl = $('#questionTpl'), max = parseInt(builder.dataset.max, 10) || 50;
    function renumber() {
      var cards = $$('.q-card', builder);
      cards.forEach(function (c, i) { $('.q-no', c).textContent = i + 1; });
      var n = $('#qCount'); if (n) n.textContent = cards.length + ' question' + (cards.length === 1 ? '' : 's');
      $('#addQ').disabled = cards.length >= max;
      $$('.q-remove', builder).forEach(function (b) { b.disabled = cards.length === 1; });
    }
    function addCard(afterEl) {
      var node = tpl.content.firstElementChild.cloneNode(true);
      if (afterEl) afterEl.after(node); else builder.appendChild(node);
      renumber(); $('textarea', node).focus(); return node;
    }
    $('#addQ').onclick = function () { addCard(); };
    builder.addEventListener('click', function (e) {
      var card = e.target.closest('.q-card'); if (!card) return;
      if (e.target.closest('.q-remove')) { if ($$('.q-card', builder).length > 1) { card.remove(); renumber(); } }
      if (e.target.closest('.q-dup')) {
        var copy = addCard(card);
        $$('textarea,input[type=text]', card).forEach(function (el, i) { $$('textarea,input[type=text]', copy)[i].value = el.value; });
        $('select', copy).value = $('select', card).value;
      }
      if (e.target.closest('.q-up') && card.previousElementSibling) { card.parentNode.insertBefore(card, card.previousElementSibling); renumber(); }
      if (e.target.closest('.q-down') && card.nextElementSibling) { card.parentNode.insertBefore(card.nextElementSibling, card); renumber(); }
      var pick = e.target.closest('.opt-pick');
      if (pick) { $('select', card).value = pick.dataset.l; $$('.opt-pick', card).forEach(function (p) { p.classList.toggle('on', p === pick); }); }
    });
    // keep the visual "correct" marker in sync with the saved value
    $$('.q-card', builder).forEach(function (c) { var v = $('select', c).value; $$('.opt-pick', c).forEach(function (p) { p.classList.toggle('on', p.dataset.l === v); }); });
    if (!$$('.q-card', builder).length) addCard();
    renumber();
  }

  /* ================================================================
     Quiz taking (participant)
     ================================================================ */
  var form = $('#quizForm');
  if (form) {
    var attempt = form.dataset.attempt, remaining = parseInt(form.dataset.remaining, 10);
    var slides = $$('.q-slide', form), total = slides.length, cur = 0, submitted = false;
    var key = 'quiz-' + attempt, store = {};
    try { store = JSON.parse(sessionStorage.getItem(key) || '{}'); } catch (e) {}
    $$('input[type=radio]', form).forEach(function (r) { if (store[r.name] === r.value) r.checked = true; });

    var palette = $('#palette'), bar = $('#progressBar'), timerEl = $('#timer'), ringEl = $('#timeRing');
    slides.forEach(function (s, i) {
      var b = document.createElement('button'); b.type = 'button'; b.textContent = i + 1; b.onclick = function () { go(i); }; palette.appendChild(b);
    });
    function answered(i) { return !!$('input:checked', slides[i]); }
    function refresh() {
      var done = 0;
      slides.forEach(function (s, i) { var a = answered(i); if (a) done++; palette.children[i].className = (a ? 'done ' : '') + (i === cur ? 'cur' : ''); });
      bar.style.width = (done / total * 100) + '%'; $('#answeredCount').textContent = done + ' of ' + total + ' answered';
      return done;
    }
    function go(i) {
      cur = Math.max(0, Math.min(total - 1, i));
      slides.forEach(function (s, k) { s.hidden = k !== cur; });
      $('#prev').disabled = cur === 0; $('#next').hidden = cur === total - 1; $('#finish').hidden = cur !== total - 1;
      $('#qPos').textContent = 'Question ' + (cur + 1) + ' of ' + total; refresh();
    }
    form.addEventListener('change', function (e) {
      if (e.target.type === 'radio') { store[e.target.name] = e.target.value; try { sessionStorage.setItem(key, JSON.stringify(store)); } catch (x) {} refresh(); }
    });
    $('#prev').onclick = function () { go(cur - 1); }; $('#next').onclick = function () { go(cur + 1); };
    document.addEventListener('keydown', function (e) {
      if (e.target.tagName === 'TEXTAREA' || e.ctrlKey || e.metaKey || $('dialog[open]')) return;
      var k = e.key.toLowerCase();
      if (k === 'arrowright') go(cur + 1); else if (k === 'arrowleft') go(cur - 1);
      else if ('abcd'.indexOf(k) > -1 && k.length === 1) { var r = $('input[value=' + k.toUpperCase() + ']', slides[cur]); if (r) { r.checked = true; r.dispatchEvent(new Event('change', { bubbles: true })); } }
    });

    var totalSecs = parseInt(form.dataset.total, 10), deadline = Date.now() + remaining * 1000;
    function fmt(s) { var m = Math.floor(s / 60), x = s % 60; return (m < 10 ? '0' : '') + m + ':' + (x < 10 ? '0' : '') + x; }
    function send() { if (submitted) return; submitted = true; try { sessionStorage.removeItem(key); } catch (e) {} form.submit(); }
    var warned = false;
    var tick = setInterval(function () {
      var left = Math.max(0, Math.round((deadline - Date.now()) / 1000));
      timerEl.textContent = fmt(left); ringEl.style.setProperty('--p', (left / totalSecs * 100).toFixed(1));
      if (left <= 60) { ringEl.parentNode.classList.add('urgent'); if (!warned) { warned = true; toast('One minute left!', 'error'); } }
      if (left <= 0) { clearInterval(tick); toast("Time's up - submitting your answers…", 'info'); send(); }
    }, 250);

    var dlg = $('#confirmDlg');
    form.addEventListener('submit', function (e) { if (!submitted) e.preventDefault(); });
    $('#finish').onclick = function () {
      var left = total - refresh(); $('#unansweredMsg').textContent = left ? left + ' question' + (left > 1 ? 's are' : ' is') + ' still unanswered.' : 'You answered every question.';
      dlg.showModal();
    };
    $('#confirmYes').onclick = function () { dlg.close(); send(); };
    window.addEventListener('beforeunload', function (e) { if (!submitted) { e.preventDefault(); e.returnValue = ''; } });
    go(0);
  }
})();

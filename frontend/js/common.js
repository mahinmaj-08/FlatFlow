// Small helpers shared by every page.
const $ = (id) => document.getElementById(id);
const esc = (s) => String(s == null ? "" : s).replace(/[&<>"']/g, (c) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]));
const pad2 = (n) => String(n).padStart(2, "0");
const todayIso = () => { const d = new Date(); return d.getFullYear() + "-" + pad2(d.getMonth() + 1) + "-" + pad2(d.getDate()); };
const money = (cents, signed) => (cents < 0 ? "-" : signed && cents > 0 ? "+" : "") + "$" + (Math.abs(cents) / 100).toFixed(2);
const dollarsToCents = (text) => Math.round(Number(text) * 100);   // the API only ever sees integer cents
const emptyRow = (cols, html) => `<tr><td class="empty" colspan="${cols}">${html}</td></tr>`;

function showMsg(id, text, ok) { const el = $(id); el.textContent = text; el.className = "msg" + (ok ? " ok" : ""); }
function requireLogin() { if (!Auth.token) { location.href = "index.html"; return false; } return true; }
function nameMap(members) { const m = {}; members.forEach((u) => (m[u.id] = u.displayName)); return m; }
function dueBadge(chore) {
  if (chore.status === "DONE") return '<span class="pill done">Done</span>';
  return chore.dueDate < todayIso() ? '<span class="pill overdue">Overdue</span>' : '<span class="pill">Pending</span>';
}
// Pages other than login/register need a household; send people to set one up.
async function loadHousehold() {
  try { return await api("GET", "/household"); }
  catch (err) { location.href = "household.html"; return null; }
}

const logoutLink = $("logout-link");
if (logoutLink) logoutLink.addEventListener("click", () => { Auth.token = null; });

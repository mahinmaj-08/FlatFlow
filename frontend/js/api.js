// Talks to the backend. While USE_MOCK is true, a fake backend kept in the
// browser (localStorage) answers instead, so every page works with no server.
// When the real API is running: set USE_MOCK to false and check API_BASE.
const API_BASE = "/api";
const USE_MOCK = true;

const Auth = {
  get token() { return localStorage.getItem("ff_token"); },
  set token(t) { if (t) localStorage.setItem("ff_token", t); else localStorage.removeItem("ff_token"); }
};

async function api(method, path, body) {
  return USE_MOCK ? mockApi(method, path, body) : realApi(method, path, body);
}

// The real thing: JSON over fetch, with the sign-in token in a header.
async function realApi(method, path, body) {
  const headers = { "Content-Type": "application/json" };
  if (Auth.token) headers.Authorization = "Bearer " + Auth.token;
  const res = await fetch(API_BASE + path, {
    method, headers, body: body ? JSON.stringify(body) : undefined
  });
  const data = await res.json().catch(() => ({}));
  if (!res.ok) throw new Error(data.error || "Request failed (" + res.status + ")");  // fetch does not throw on 4xx/5xx
  return data;
}

// ---------- Mock backend (delete once the real API works) ----------
const MOCK_KEY = "ff_mock_db";
const fail = (msg) => { throw new Error(msg); };
const loadDb = () => JSON.parse(localStorage.getItem(MOCK_KEY) ||
  '{"n":1,"users":[],"households":[],"chores":[],"expenses":[],"sessions":{}}');

// Same rule as the cent-split spike: leftover cents go to the lowest user ids.
function splitEqually(totalCents, ids) {
  const sorted = [...ids].sort((a, b) => a - b);
  const base = Math.floor(totalCents / sorted.length), extra = totalCents % sorted.length;
  return sorted.map((id, i) => ({ userId: id, cents: base + (i < extra ? 1 : 0) }));
}

async function mockApi(method, path, body) {
  body = body || {};
  const db = loadDb();
  const [route, query] = path.split("?");
  const params = new URLSearchParams(query || "");
  const me = db.users.find(u => u.id === db.sessions[Auth.token]);
  const need = () => me || fail("Please log in again");
  const household = () => db.households.find(h => h.id === need().householdId) || fail("You are not in a household yet");
  const today = todayIso();
  const key = method + " " + route;
  const netBalances = (h) => {
    const net = {};
    db.users.filter(u => u.householdId === h.id).forEach(u => net[u.id] = 0);
    db.expenses.filter(e => e.householdId === h.id).forEach(e => {
      net[e.payerUserId] += e.totalAmountCents;
      e.shares.forEach(s => net[s.userId] -= s.cents);
    });
    return net;
  };
  let out;

  if (key === "POST /register") {
    if (!body.displayName || !body.loginName || !body.password) fail("Fill in every field");
    if (db.users.some(u => u.loginName === body.loginName)) fail("That login name is already taken");
    const u = { id: db.n++, displayName: body.displayName, loginName: body.loginName, password: body.password, householdId: null };
    db.users.push(u); out = { id: u.id };
  } else if (key === "POST /login") {
    const u = db.users.find(x => x.loginName === body.loginName && x.password === body.password);
    if (!u) fail("Wrong login name or password");
    const token = "mock-" + Math.random().toString(36).slice(2);
    db.sessions[token] = u.id; out = { token, householdId: u.householdId };
  } else if (key === "POST /households") {
    need(); if (me.householdId) fail("You are already in a household");
    if (!body.name) fail("Give the household a name");
    const h = { id: db.n++, name: body.name, organizerUserId: me.id, inviteCode: Math.random().toString(36).slice(2, 8).toUpperCase() };
    db.households.push(h); me.householdId = h.id; out = { id: h.id, inviteCode: h.inviteCode };
  } else if (key === "POST /households/join") {
    need(); const h = db.households.find(x => x.inviteCode === String(body.inviteCode || "").trim().toUpperCase());
    if (!h) fail("No household has that invite code");
    me.householdId = h.id; out = { householdId: h.id };
  } else if (key === "GET /household") {
    const h = household();
    out = { name: h.name, inviteCode: h.inviteCode,
      members: db.users.filter(u => u.householdId === h.id).map(u => ({ id: u.id, displayName: u.displayName, role: u.id === h.organizerUserId ? "Organizer" : "Member" })) };
  } else if (key === "GET /chores") {
    const h = household(); let list = db.chores.filter(c => c.householdId === h.id);
    const a = params.get("assignee"), s = params.get("status");
    if (a) list = list.filter(c => c.assigneeUserId === Number(a));
    if (s === "OVERDUE") list = list.filter(c => c.status === "PENDING" && c.dueDate < today);
    else if (s) list = list.filter(c => c.status === s);
    out = list.sort((x, y) => x.dueDate.localeCompare(y.dueDate));
  } else if (key === "POST /chores") {
    const h = household();
    if (!body.title || !body.assigneeUserId || !body.dueDate) fail("Title, assignee and due date are required");
    const c = { id: db.n++, householdId: h.id, title: body.title, description: body.description || "",
      assigneeUserId: Number(body.assigneeUserId), dueDate: body.dueDate, status: body.status || "PENDING" };
    db.chores.push(c); out = c;
  } else if (method === "PATCH" && route.startsWith("/chores/")) {
    const h = household(); const c = db.chores.find(x => x.id === Number(route.split("/")[2]) && x.householdId === h.id);
    if (!c) fail("Chore not found");
    c.status = body.status; out = c;
  } else if (key === "GET /expenses") {
    const h = household();
    out = db.expenses.filter(e => e.householdId === h.id).sort((a, b) => b.date.localeCompare(a.date) || b.id - a.id)
      .map(e => ({ id: e.id, description: e.description, date: e.date, totalAmountCents: e.totalAmountCents, payerUserId: e.payerUserId,
        yourShareCents: (e.shares.find(s => s.userId === me.id) || { cents: 0 }).cents }));
  } else if (key === "POST /expenses") {
    const h = household();
    if (!body.description || !body.date || !body.payerUserId) fail("Description, date and payer are required");
    if (!Number.isInteger(body.totalAmountCents) || body.totalAmountCents <= 0) fail("Enter an amount above zero");
    if (!body.shareUserIds || !body.shareUserIds.length) fail("Pick at least one member to split with");
    const e = { id: db.n++, householdId: h.id, description: body.description, date: body.date, totalAmountCents: body.totalAmountCents,
      payerUserId: Number(body.payerUserId), shares: splitEqually(body.totalAmountCents, body.shareUserIds.map(Number)) };
    db.expenses.push(e); out = e;
  } else if (key === "GET /dashboard") {
    const h = household(), net = netBalances(h);
    const pending = db.chores.filter(c => c.householdId === h.id && c.status === "PENDING").sort((x, y) => x.dueDate.localeCompare(y.dueDate));
    out = { yourBalanceCents: net[me.id], pendingCount: pending.length,
      overdueCount: pending.filter(c => c.dueDate < today).length, chores: pending,
      balances: db.users.filter(u => u.householdId === h.id).map(u => ({ userId: u.id, displayName: u.displayName, balanceCents: net[u.id] })) };
  } else fail("Not found: " + key);

  localStorage.setItem(MOCK_KEY, JSON.stringify(db));
  return out;
}

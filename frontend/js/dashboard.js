(async function () {
  if (!requireLogin()) return;
  const h = await loadHousehold(); if (!h) return;
  const d = await api("GET", "/dashboard");
  const names = nameMap(h.members);
  $("household-name").textContent = h.name + " · " + h.members.length + " members";
  $("your-balance").textContent = money(d.yourBalanceCents, true);
  $("pending-count").textContent = d.pendingCount;
  $("overdue-count").textContent = d.overdueCount;
  $("dashboard-chore-list").innerHTML = d.chores.length
    ? d.chores.map((c) => `<tr><td>${esc(c.title)}</td><td>${esc(names[c.assigneeUserId])}</td><td>${esc(c.dueDate)} ${c.dueDate < todayIso() ? '<span class="pill overdue">Overdue</span>' : ""}</td></tr>`).join("")
    : emptyRow(3, 'No pending chores. <a href="chores.html">Add one</a>.');
  $("balance-list").innerHTML = d.balances.map((b) => `<tr><td>${esc(b.displayName)}</td><td class="amt">${money(b.balanceCents, true)}</td></tr>`).join("");
})();

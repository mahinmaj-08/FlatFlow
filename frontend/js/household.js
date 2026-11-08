(async function () {
  if (!requireLogin()) return;
  async function refresh() {
    try {
      const h = await api("GET", "/household");
      $("household-invite-code").textContent = h.inviteCode;
      $("member-list").innerHTML = h.members.map((m) => `<tr><td>${esc(m.displayName)}</td><td>${esc(m.role)}</td></tr>`).join("");
    } catch (err) { /* not in a household yet: keep the placeholder text */ }
  }
  $("create-household-form").addEventListener("submit", async (e) => {
    e.preventDefault();
    try {
      const r = await api("POST", "/households", { name: $("household_name").value.trim() });
      showMsg("create-message", "Household created. Invite code: " + r.inviteCode, true);
      refresh();
    } catch (err) { showMsg("create-message", err.message); }
  });
  $("join-household-form").addEventListener("submit", async (e) => {
    e.preventDefault();
    try {
      await api("POST", "/households/join", { inviteCode: $("invite_code").value.trim() });
      showMsg("join-message", "Joined. Taking you to the dashboard...", true);
      setTimeout(() => (location.href = "dashboard.html"), 700);
    } catch (err) { showMsg("join-message", err.message); }
  });
  refresh();
})();

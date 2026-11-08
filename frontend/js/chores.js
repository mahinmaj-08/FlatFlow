(async function () {
  if (!requireLogin()) return;
  const h = await loadHousehold(); if (!h) return;
  const names = nameMap(h.members);
  const options = h.members.map((m) => `<option value="${m.id}">${esc(m.displayName)}</option>`).join("");
  $("chore_assignee").innerHTML += options;
  $("filter_assignee").innerHTML += options;
  $("chore_due_date").value = todayIso();

  async function load() {
    const q = new URLSearchParams();
    if ($("filter_assignee").value) q.set("assignee", $("filter_assignee").value);
    if ($("filter_status").value) q.set("status", $("filter_status").value);
    const chores = await api("GET", "/chores?" + q.toString());
    $("chore-list").innerHTML = chores.length ? chores.map((c) => `<tr><td>${esc(c.title)}</td><td>${esc(names[c.assigneeUserId])}</td><td>${esc(c.dueDate)}</td>
      <td>${dueBadge(c)}${c.status === "PENDING" ? ` <button type="button" class="secondary" data-done="${c.id}" style="margin:0 0 0 .5rem;padding:.15rem .6rem">Mark done</button>` : ""}</td></tr>`).join("")
      : emptyRow(4, "No chores match these filters.");
  }

  $("chore-form").addEventListener("submit", async (e) => {
    e.preventDefault();
    try {
      await api("POST", "/chores", { title: $("chore_title").value.trim(), description: $("chore_description").value.trim(),
        assigneeUserId: Number($("chore_assignee").value), dueDate: $("chore_due_date").value, status: $("chore_status").value });
      showMsg("form-message", "Chore added.", true);
      $("chore_title").value = ""; $("chore_description").value = "";
      load();
    } catch (err) { showMsg("form-message", err.message); }
  });
  $("filter_assignee").addEventListener("change", load);
  $("filter_status").addEventListener("change", load);
  $("chore-list").addEventListener("click", async (e) => {
    const id = e.target.getAttribute("data-done");
    if (id) { await api("PATCH", "/chores/" + id, { status: "DONE" }); load(); }
  });
  load();
})();

(async function () {
  if (!requireLogin()) return;
  const h = await loadHousehold(); if (!h) return;
  const names = nameMap(h.members);
  $("expense_payer").innerHTML += h.members.map((m) => `<option value="${m.id}">${esc(m.displayName)}</option>`).join("");
  $("expense_shares").innerHTML = "<legend>Split between</legend>" +
    h.members.map((m) => `<label><input type="checkbox" value="${m.id}" checked> ${esc(m.displayName)}</label>`).join("");
  $("expense_date").value = todayIso();

  async function load() {
    const list = await api("GET", "/expenses");
    $("expense-list").innerHTML = list.length ? list.map((x) => `<tr><td>${esc(x.date)}</td><td>${esc(x.description)}</td><td>${esc(names[x.payerUserId])}</td>
      <td class="amt">${money(x.totalAmountCents)}</td><td class="amt">${money(x.yourShareCents)}</td></tr>`).join("")
      : emptyRow(5, "No expenses yet.");
  }

  $("expense-form").addEventListener("submit", async (e) => {
    e.preventDefault();
    const shareUserIds = [...$("expense_shares").querySelectorAll("input:checked")].map((i) => Number(i.value));
    try {
      await api("POST", "/expenses", { description: $("expense_description").value.trim(), date: $("expense_date").value,
        totalAmountCents: dollarsToCents($("expense_amount").value), payerUserId: Number($("expense_payer").value), shareUserIds });
      showMsg("form-message", "Expense recorded.", true);
      $("expense_description").value = ""; $("expense_amount").value = "";
      load();
    } catch (err) { showMsg("form-message", err.message); }
  });
  load();
})();

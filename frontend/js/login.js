$("login-form").addEventListener("submit", async (e) => {
  e.preventDefault();
  try {
    const r = await api("POST", "/login", { loginName: $("login_name").value.trim(), password: $("password").value });
    Auth.token = r.token;
    location.href = r.householdId ? "dashboard.html" : "household.html";
  } catch (err) { showMsg("form-message", err.message); }
});

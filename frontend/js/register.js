$("register-form").addEventListener("submit", async (e) => {
  e.preventDefault();
  try {
    await api("POST", "/register", { displayName: $("display_name").value.trim(), loginName: $("login_name").value.trim(), password: $("password").value });
    showMsg("form-message", "Account created. Taking you to log in...", true);
    setTimeout(() => (location.href = "index.html"), 900);
  } catch (err) { showMsg("form-message", err.message); }
});

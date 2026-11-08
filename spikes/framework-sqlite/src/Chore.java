/** A chore, using the column names from the D0 proposal (subset for this spike). */
public record Chore(Integer id, String title, int assigneeUserId, String dueDate, String status) {}

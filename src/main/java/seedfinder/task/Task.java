package seedfinder.task;

import java.util.Optional;

public abstract class Task {

	private static Task currentTask;

	public static void setCurrentTask(Task task) {
		currentTask = task;
	}

	@SuppressWarnings("unchecked")
	public static <T extends Task> Optional<T> getCurrentTask(Type type) {
		if (currentTask.type == type) {
			return Optional.of((T) currentTask);
		} else {
			return Optional.empty();
		}
	}

	public static boolean isCurrentTaskOfType(Type type) {
		return currentTask.type == type;
	}

	private final Type type;

	public Task(Type type) {
		this.type = type;
	}

	public final Type getType() {
		return type;
	}

	public static enum Type {
		COUNT_EYES
	}

}

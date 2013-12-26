package me.offluffy.carbonkit.utils;

public class DuplicateModuleException extends Exception {
	private static final long serialVersionUID = 5613010469360142443L;
	public enum DupeType {
		DUPE_NAME("A module with the given name already exists!"),
		DUPE_ALIAS("A module with a matching alias already exists!");
		public String errmsg;
		DupeType(String msg) {
			errmsg = msg;
		}
	}
	public DuplicateModuleException(DupeType dt) {
		super(dt.errmsg);
	}
}

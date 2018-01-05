package com.github.feroult.gapi;

public enum BatchOptions {

	SHRINK;

	public boolean on(BatchOptions[] options) {
		for (BatchOptions option : options) {
			if (option.equals(this)) {
				return true;
			}
		}

		return false;
	}

}

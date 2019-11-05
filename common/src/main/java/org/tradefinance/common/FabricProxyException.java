package org.tradefinance.common;

public class FabricProxyException extends Exception {
    private static final long serialVersionUID = 1L;

    public FabricProxyException(String message) {
		super(message);
	}

	public FabricProxyException(Throwable cause) {
		super(cause);
	}

	public FabricProxyException(String message, Throwable cause) {
		super(message, cause);
	}
}

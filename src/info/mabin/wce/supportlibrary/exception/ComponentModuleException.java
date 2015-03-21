package info.mabin.wce.supportlibrary.exception;

import info.mabin.wce.supportlibrary.Constant;

public class ComponentModuleException extends Exception {
	private static final long serialVersionUID = Constant.VERSION_COMPONENTSUPPORT_VERSIONCODE;
	private static final String defaultMessage = "Module Error";
	
	public ComponentModuleException() {
		super();
	}
	
	public ComponentModuleException(String message) {
		super(defaultMessage + " - " + message);
	}
	
	public ComponentModuleException(Throwable cause){
		super(defaultMessage, cause);
	}
	
	public ComponentModuleException(String message, Throwable cause){
		super(defaultMessage + " - " + message, cause);
	}
}
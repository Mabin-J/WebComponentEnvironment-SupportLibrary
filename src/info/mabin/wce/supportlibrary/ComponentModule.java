package info.mabin.wce.supportlibrary;


import info.mabin.wce.manager.Logger;
import info.mabin.wce.manager.ComponentAbstract.ComponentContext;
import info.mabin.wce.manager.ComponentAbstract.ComponentManifest;
import info.mabin.wce.supportlibrary.exception.ComponentModuleException;

import javax.servlet.ServletContext;


/**
 * Module for Component
 * @since 1
 */
public abstract class ComponentModule {
	protected ComponentContext context;
	protected ServletContext servletContext;
	protected ComponentManifest manifest;
	protected Logger logger;

	void setComponentContext(ComponentContext context){
		this.context = context;
	}

	void setServletContext(ServletContext context){
		this.servletContext = context;
	}
	
	void setManifest(ComponentManifest manifest){
		this.manifest = manifest;
	}
	
	void setLogger(Logger logger){
		this.logger = logger;
	}

	/**
	 * for Initializing Module
	 * @throws ComponentModuleException
	 */
	public void init() throws ComponentModuleException{}

	/**
	 * for Destroying Module
	 * @throws ComponentModuleException
	 */
	public void destroy() throws ComponentModuleException{}
}
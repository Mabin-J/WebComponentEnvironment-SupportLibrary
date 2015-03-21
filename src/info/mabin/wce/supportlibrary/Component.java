package info.mabin.wce.supportlibrary;

import info.mabin.wce.manager.ComponentAbstract;
import info.mabin.wce.manager.FilenameFilterFactory;
import info.mabin.wce.manager.Logger;
import info.mabin.wce.manager.exception.ComponentException;
import info.mabin.wce.supportlibrary.EventListener.EventPair;
import info.mabin.wce.supportlibrary.ServletDispatcher.MappingNode;
import info.mabin.wce.supportlibrary.annotation.Autowired;
import info.mabin.wce.supportlibrary.annotation.Controller;
import info.mabin.wce.supportlibrary.annotation.EventChangedConfiguration;
import info.mabin.wce.supportlibrary.annotation.EventRegisteredComponent;
import info.mabin.wce.supportlibrary.annotation.EventRegisteredIcm;
import info.mabin.wce.supportlibrary.annotation.EventUnregisteredComponent;
import info.mabin.wce.supportlibrary.annotation.EventUnregisteredIcm;
import info.mabin.wce.supportlibrary.annotation.Initializer;
import info.mabin.wce.supportlibrary.annotation.Model;
import info.mabin.wce.supportlibrary.annotation.RequestMapping;
import info.mabin.wce.supportlibrary.annotation.RequestMethod;
import info.mabin.wce.supportlibrary.exception.ComponentModuleException;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Component Proxy
 */
public class Component extends ComponentAbstract implements ServletContextListener{
	private ServletContext servletContext;

	private List<String> listScanTargetClassname = new ArrayList<String>(100);

	private List<ComponentModule> listModule = new ArrayList<ComponentModule>(100);

	private Map<Class<?>, ComponentModule> mapModule = new HashMap<Class<?>, ComponentModule>(100);







	@Override
	public void contextInitialized(ServletContextEvent event) {
		// ======== Read & Set Context Location of Servlet
		servletContext = event.getServletContext();
		try {
			loadComponent(servletContext, this, "info.mabin.webcomponent.manager.SupportLibrary");
		} catch (ComponentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent event) {
		try {
			unloadComponent();
		} catch (ComponentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected void initComponent(){
		try {
			logger = Logger.newInstance(manifest.getPackageName() + ".SupportLibrary", manifest.getPackageName(), manifest.getComponentName());
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		MappingNode tmpMappingTree = new MappingNode();
		List<EventPair> listEventRegisteredComponent = new ArrayList<EventPair>();
		List<EventPair> listEventUnregisteredComponent = new ArrayList<EventPair>();
		List<EventPair> listEventChangedConfiguration = new ArrayList<EventPair>();
		List<EventPair> listEventRegisteredIcm = new ArrayList<EventPair>();
		List<EventPair> listEventUnregisteredIcm = new ArrayList<EventPair>();
		
		
		// Scanning and Initializing Module
		
		logger.info("Scanning Module...");

		makeListScanTargetClass(context.getResourceContext(
				"WEB-INF/classes/" + manifest.getPackageName().replace(".", "/") + "/"), 
				manifest.getPackageName());
		StringBuilder sbMappingLog = new StringBuilder();
		sbMappingLog.append("\n=== Mapping Information ===\n");

		for(String targetClassname: listScanTargetClassname){
			try {
				Class<?> targetClass = this.getClass().getClassLoader().loadClass(targetClassname);

				Class<?> targetSuperclass = targetClass.getSuperclass();

				if(targetSuperclass == ComponentModule.class){
					String moduleName = targetClass.getCanonicalName().replace(manifest.getPackageName() + ".", "");
					Controller annotationController = targetClass.getAnnotation(Controller.class);
					Model annotationModel = targetClass.getAnnotation(Model.class);
					Initializer annotationInitializer = targetClass.getAnnotation(Initializer.class);
					// TODO: Security, etc

					ComponentModule targetModule = null;
					for(String classCanonicalName: manifest.getListIcmClassName()){
						if(targetClass == Class.forName(classCanonicalName)){
							targetModule = (ComponentModule) context.getMapIcm().get(classCanonicalName);
						} else {
							targetModule = (ComponentModule) targetClass.newInstance();
						}
					}

					targetModule.setManifest(manifest);
					targetModule.setComponentContext(context);
					try {
						targetModule.setLogger(Logger.getInstance(targetModule.getClass()));
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					targetModule.setServletContext(servletContext);

					//Module Initializing
					try {
						targetModule.init();
					} catch (ComponentModuleException e) {
						// TODO Auto-generated catch block
						logger.e(e);
						return;
						//throw new ServletException(e);
					}

					listModule.add(targetModule);
					mapModule.put(targetModule.getClass(), targetModule);

					if(annotationController != null){
						logger.i("\t" + moduleName + ": Controller");
						//						listModuleController.add(targetModule);

						RequestMapping annotationRequestMappingClass = targetClass.getAnnotation(RequestMapping.class);
						
						String urlPatternClass = annotationRequestMappingClass.value();

						sbMappingLog.append("Pattern: " + urlPatternClass + "\tto: '" + targetClass.getCanonicalName() + "' Class\n");

						Method[] targetMethods = targetModule.getClass().getMethods();

						for(Method targetMethod: targetMethods){
							RequestMapping annotationRequestMappingMethod = targetMethod.getAnnotation(RequestMapping.class);
							
							if(annotationRequestMappingMethod != null){
								String tmpHttpMethod = "";
								for(RequestMethod httpMethod: annotationRequestMappingMethod.method()){
									tmpHttpMethod += httpMethod + ", ";
								}
								tmpHttpMethod = tmpHttpMethod.substring(0, tmpHttpMethod.length() - 2);
								
								try {
									addNodeInMappingTree(
											tmpMappingTree, 
											urlPatternClass + annotationRequestMappingMethod.value(), 
											annotationRequestMappingMethod.method(), 
											targetModule, targetMethod);
									
									sbMappingLog.append("Pattern: " + urlPatternClass + annotationRequestMappingMethod.value() + "\tHttpMethod: {" + tmpHttpMethod + "}\tto: '" + targetMethod.getName() + "' Method\n");
								} catch (Exception e) {
									sbMappingLog.append("(ERR) Pattern: " + urlPatternClass + annotationRequestMappingMethod.value() + "\tHttpMethod: {" + tmpHttpMethod + "}\tto: '" + targetMethod.getName() + "' Method\n");
									logger.e(e);
								}
							}
						}
					} else if(annotationModel != null){
						logger.i("\t" + moduleName + ": Model");
						// TODO?
					} else if (annotationInitializer != null){
						logger.i("\t" + moduleName + ": Initializer");
						// TODO?
					}
					
					// ======== for Event
					Method[] targetMethods = targetModule.getClass().getMethods();

					for(Method targetMethod: targetMethods){
						EventRegisteredComponent annotationEventRegisteredComponent = targetMethod.getAnnotation(EventRegisteredComponent.class);
						EventUnregisteredComponent annotationEventUnregisteredComponent = targetMethod.getAnnotation(EventUnregisteredComponent.class);
						EventChangedConfiguration annotationEventChangeConfiguration = targetMethod.getAnnotation(EventChangedConfiguration.class);
						EventRegisteredIcm annotationEventRegisteredIcm = targetMethod.getAnnotation(EventRegisteredIcm.class);
						EventUnregisteredIcm annotationEventUnregisteredIcm = targetMethod.getAnnotation(EventUnregisteredIcm.class);

						if(annotationEventRegisteredComponent != null){
							EventPair tmpEventPair;
							String[] targetValue = annotationEventRegisteredComponent.value();
							if(annotationEventRegisteredComponent.value().length != 0){
								tmpEventPair = new EventPair(targetModule, targetMethod, targetValue);
							} else {
								tmpEventPair = new EventPair(targetModule, targetMethod);
							}
							listEventRegisteredComponent.add(tmpEventPair);
						}
						if(annotationEventUnregisteredComponent != null){
							EventPair tmpEventPair;
							String[] targetValue = annotationEventUnregisteredComponent.value();
							if(annotationEventUnregisteredComponent.value().length != 0){
								tmpEventPair = new EventPair(targetModule, targetMethod, targetValue);
							} else {
								tmpEventPair = new EventPair(targetModule, targetMethod);
							}
							listEventUnregisteredComponent.add(tmpEventPair);
						}
						if(annotationEventChangeConfiguration != null){
							EventPair tmpEventPair = new EventPair(targetModule, targetMethod);
							listEventChangedConfiguration.add(tmpEventPair);
						}
						if(annotationEventRegisteredIcm != null){
							EventPair tmpEventPair;
							String[] targetValue = annotationEventRegisteredIcm.value();
							if(annotationEventRegisteredIcm.value().length != 0){
								tmpEventPair = new EventPair(targetModule, targetMethod, targetValue);
							} else {
								tmpEventPair = new EventPair(targetModule, targetMethod);
							}
							listEventRegisteredIcm.add(tmpEventPair);
						}
						if(annotationEventUnregisteredIcm != null){
							EventPair tmpEventPair;
							String[] targetValue = annotationEventUnregisteredIcm.value();
							if(annotationEventUnregisteredIcm.value().length != 0){
								tmpEventPair = new EventPair(targetModule, targetMethod, targetValue);
							} else {
								tmpEventPair = new EventPair(targetModule, targetMethod);
							}
							listEventUnregisteredIcm.add(tmpEventPair);
						}
					}
				}
			} catch (ClassNotFoundException e) {
				logger.e(e);
			} catch (InstantiationException e) {
				logger.e(e);
			} catch (IllegalAccessException e) {
				logger.e(e);
			}	
		}

		logger.i(sbMappingLog.toString());


		// ========== Autowiring
		for(ComponentModule targetModule: listModule){
			Field[] arrayField = targetModule.getClass().getDeclaredFields();

			for(Field targetField: arrayField){
				Autowired annotationAutowired = targetField.getAnnotation(Autowired.class);

				if(annotationAutowired != null){
					ComponentModule targetModel = mapModule.get(targetField.getType());

					try {
						boolean bakAccessible = targetField.isAccessible(); 
						if(!targetField.isAccessible()){
							targetField.setAccessible(true);
						}
						targetField.set(targetModule, targetModel);
						targetField.setAccessible(bakAccessible);
					} catch (IllegalArgumentException e) {
						logger.e(e);
						return;
					} catch (IllegalAccessException e) {
						logger.e(e);
						return;
					}
				}
			}
		}
		
		ServletDispatcher.setVariables(context, tmpMappingTree, logger);
		
		EventListener eventListener = (EventListener) context.getListEventListenerComponent().get(0);
		eventListener.setListEventChangedConfiguration(listEventChangedConfiguration);
		eventListener.setListEventRegisteredComponent(listEventRegisteredComponent);
		eventListener.setListEventUnregisteredComponent(listEventUnregisteredComponent);
		eventListener.setListEventRegisteredIcm(listEventRegisteredIcm);
		eventListener.setListEventUnregisteredIcm(listEventUnregisteredIcm);
	}

	/**
	 * Component 종료
	 */
	protected void destroyComponent(){
		// TODO Auto-generated method stub
		logger.i("Destroy");
		// Destroy Logic in All Module
		for(ComponentModule targetModule: listModule){
			try {
				targetModule.destroy();
			} catch (ComponentModuleException e) {
				// TODO Auto-generated catch block
				logger.e(e);
			}

		}
	}







	private void makeListScanTargetClass(File folderClasspath, String packageName){
		String path = folderClasspath.getAbsolutePath() + "/";
		//		File folderClasspath = new File(path);

		String[] arrayAnyfile = folderClasspath.list();
		String[] arrayClassfile = folderClasspath.list(FilenameFilterFactory.getFilenameFilterWithExtension("class"));

		for(String classFilename: arrayClassfile){
			listScanTargetClassname.add(packageName + "." + classFilename.substring(0, classFilename.length() - 6));
		}

		for(String targetFilename: arrayAnyfile){
			if(targetFilename.equals("."))
				continue;
			if(targetFilename.equals(".."))
				continue;
			File tmpFile = new File(path + targetFilename);
			if(tmpFile.isDirectory()){
				makeListScanTargetClass(new File(path + targetFilename), packageName + "." + targetFilename);
			}
		}
	}

	private void addNodeInMappingTree(MappingNode mappingTree, String pattern, RequestMethod[] httpMethods,
			ComponentModule targetModule, Method targetMethod) throws Exception{
		String[] parsedPattern = pattern.split("/");

		MappingNode handler = mappingTree;

		for(String fragment: parsedPattern){
			if(fragment.equals("*")){
				if(handler.variableNode == null){
					handler.variableNode = new MappingNode();
				}
				handler = handler.variableNode;

			} else if(fragment.equals("**")){
				if(handler.fallBackNode == null){
					handler.fallBackNode = new MappingNode();
				}
				handler = handler.fallBackNode;
				break;
			} else {
				MappingNode tmpHandler = handler.childNodeFixed.get(fragment);
				if(tmpHandler == null){
					tmpHandler = new MappingNode();
					handler.childNodeFixed.put(fragment, tmpHandler);

				}
				handler = tmpHandler;
			}
		}

		handler.targetModule = targetModule;

		for(RequestMethod reqMethod: httpMethods){
			if(reqMethod == RequestMethod.GET){
				if(handler.targetMethodGet != null){
					logger.e("Already Mapped (GET)");
					logger.e("\tAdded: " + pattern + ", " + handler.targetModule.getClass().getName() + "#" + handler.targetMethodGet.getName());
					logger.e("\tTarget: " + pattern + ", " + targetModule.getClass().getName() + "#" + targetMethod.getName());
					throw new Exception("Already Registered (GET)");
				}

				handler.targetMethodGet = targetMethod;
			} else if(reqMethod == RequestMethod.POST){
				if(handler.targetMethodPost != null){
					logger.e("Already Mapped (POST)");
					logger.e("\tAdded: " + pattern + ", " + handler.targetModule.getClass().getName() + "#" + handler.targetMethodPost.getName());
					logger.e("\tTarget: " + pattern + ", " + targetModule.getClass().getName() + "#" + targetMethod.getName());
					throw new Exception("Already Registered (POST)");
				}

				handler.targetMethodPost = targetMethod;
			} else {
				// TODO: Other HTTP Methods
			}
		}
	}
}
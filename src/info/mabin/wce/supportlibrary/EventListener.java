package info.mabin.wce.supportlibrary;

import info.mabin.wce.manager.ComponentAbstract.ComponentManifest;
import info.mabin.wce.manager.eventlistener.EventListenerComponentImpl;
import info.mabin.wce.manager.eventlistener.EventListenerConfigurationImpl;
import info.mabin.wce.manager.eventlistener.EventListenerIcmImpl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.w3c.dom.NodeList;

/**
 * Event Proxy
 */
public class EventListener implements EventListenerComponentImpl, EventListenerConfigurationImpl, EventListenerIcmImpl{
	private List<EventPair> listEventRegisteredComponent = new ArrayList<EventPair>();
	private List<EventPair> listEventUnregisteredComponent = new ArrayList<EventPair>();
	private List<EventPair> listEventChangedConfiguration = new ArrayList<EventPair>();
	private List<EventPair> listEventRegisteredIcm = new ArrayList<EventPair>();
	private List<EventPair> listEventUnregisteredIcm = new ArrayList<EventPair>();

	@Override
	public void eventChangedConfiguration(NodeList configuration) {
		for(EventPair eventPair: listEventChangedConfiguration){
			try {
				eventPair.method.invoke(eventPair.object, configuration);
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void eventRegisteredComponent(ComponentManifest manifest) {
		try {
			for(EventPair eventPair: listEventRegisteredComponent){
				if(eventPair.target.length == 0){
					eventPair.method.invoke(eventPair.object, manifest);
				} else {
					for(String targetName: eventPair.target){
						if(targetName.equals(manifest.getPackageName())){
							eventPair.method.invoke(eventPair.object, manifest);
						}
					}
				}
			}
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void eventRegisteredComponent(List<ComponentManifest> listManifest) {
		for(ComponentManifest manifest: listManifest){
			eventRegisteredComponent(manifest);
		}
	}


	
	@Override
	public void eventUnregisteredComponent(ComponentManifest manifest) {
		try {
			for(EventPair eventPair: listEventUnregisteredComponent){
				if(eventPair.target.length == 0){
					eventPair.method.invoke(eventPair.object, manifest);
				} else {
					for(String targetName: eventPair.target){
						if(targetName.equals(manifest.getPackageName())){
							eventPair.method.invoke(eventPair.object, manifest);
						}
					}
				}
			}
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void eventRegisteredIcm(String canonicalName) {
		try {
			for(EventPair eventPair: listEventRegisteredIcm){
				if(eventPair.target.length == 0){
					eventPair.method.invoke(eventPair.object);
				} else {
					for(String targetName: eventPair.target){
						if(targetName.equals(canonicalName)){
							eventPair.method.invoke(eventPair.object);
						}
					}
				}
			}
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void eventRegisteredIcm(Set<String> setCanonicalName) {
		for(String canonicalName: setCanonicalName){
			eventRegisteredIcm(canonicalName);
		}
	}

	@Override
	public void eventUnregisteredIcm(String canonicalName) {
		try {
			for(EventPair eventPair: listEventUnregisteredIcm){
				if(eventPair.target.length == 0){
					eventPair.method.invoke(eventPair.object);
				} else {
					for(String targetName: eventPair.target){
						if(targetName.equals(canonicalName)){
							eventPair.method.invoke(eventPair.object);
						}
					}
				}
			}
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static class EventPair{
		protected ComponentModule object;
		protected Method method;
		protected String[] target = new String[0];

		public EventPair(ComponentModule object, Method method){
			this.object = object;
			this.method = method;
		}

		public EventPair(ComponentModule object, Method method, String[] target){
			this.object = object;
			this.method = method;
			this.target = target;
		}
	}

	void setListEventRegisteredComponent(List<EventPair> listEventRegisteredComponent){
		this.listEventRegisteredComponent = listEventRegisteredComponent;
	}

	void setListEventUnregisteredComponent(List<EventPair> listEventUnregisteredComponent){
		this.listEventUnregisteredComponent = listEventUnregisteredComponent;
	}

	void setListEventChangedConfiguration(List<EventPair> listEventChangedConfiguration){
		this.listEventChangedConfiguration = listEventChangedConfiguration;
	}
	
	void setListEventRegisteredIcm(List<EventPair> listEventRegisteredIcm){
		this.listEventRegisteredIcm = listEventRegisteredIcm;
	}

	void setListEventUnregisteredIcm(List<EventPair> listEventUnregisteredIcm){
		this.listEventUnregisteredIcm = listEventUnregisteredIcm;
	}
}
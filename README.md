Web Component Environment - Support Library
===========================================
This provides more easy way of developing component.

This provides...
- Seperate Component to Modules.
- Dispatch Servlet
- Autowire
- Event Handle


-------------------------------------
Develop Component with SupportLibrary
-------------------------------------
### Create 'web.xml' in 'WEB-INF'
'web.xml' must be this XML. (but you can modify 'display-name')
```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://java.sun.com/xml/ns/javaee" xsi:schemaLocation="http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd" id="WebApp_ID" version="2.5">
  <display-name>Component</display-name>
  <listener>
    <listener-class>info.mabin.wce.supportlibrary.Component</listener-class>
  </listener>
  <servlet>
    <description></description>
    <display-name>ServletDispatcher</display-name>
    <servlet-name>ServletDispatcher</servlet-name>
    <servlet-class>info.mabin.wce.supportlibrary.ServletDispatcher</servlet-class>
  </servlet>
  <servlet-mapping>
    <servlet-name>ServletDispatcher</servlet-name>
    <url-pattern>/</url-pattern>
  </servlet-mapping>
</web-app>
```

### Create 'WCManifest.xml' in 'WEB-INF'
```xml
<?xml version="1.0" encoding="UTF-8"?>
<WCManifest Package="package.name.of.component">
  <ComponentName>Component Name</ComponentName>
  <UseManagerVersion>1</UseManagerVersion><!-- Same as Original Set -->
  <IcmClasses>
    <!-- Same as Original Set -->
  </IcmClasses>
  <EventListenerClasses>
    <!-- This Must be fixed -->
    <EventListenerClass>info.mabin.wce.supportlibrary.EventListener</EventListenerClass>
  </EventListenerClasses>
  <VersionCode>1</VersionCode><!-- Component Version (Long type) -->
  <VersionName>0.1.0</VersionName><!-- Component Version (String type) -->
</WCManifest>
```

### Create 'Module' Class
SupportLibrary provides some types of module.
- Controller
- Model
- etc. (planned)

#### Controller
```java
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import info.mabin.wce.supportlibrary.ComponentModule;
import info.mabin.wce.supportlibrary.annotation.Controller;
import info.mabin.wce.supportlibrary.annotation.RequestMapping;
import info.mabin.wce.supportlibrary.annotation.RequestMethod;

@Controller		// Required
@RequestMapping("/")	// Required
public class ControllerSample extends ComponentModule{
	@RequestMapping(value = "", method = RequestMethod.GET)		// Required
	public void defaultUri(HttpServletRequest request, HttpServletResponse response){
		// Write Code something...
	}
}
```
'ServletDispatcher' Invoke method when component is requested 'RequestMapping' of Class + 'RequestMapping' of Method.

Value of 'RequestMapping' of method can use '*' or '**' character.
- '*'
  - It can located between '/' and '/'.
  - Invoked method when requested with URL that any characters can located this character.
  - Example: /path1/*/path2
- '**'
  - It can located next to '/'.
  - It means 'fallback'.
  - Invoked method when requested with URL that any characters or any path can located this character.

#### Model
For 'Model' but nothing any features, yet. Just comment.


------------------------------------
Provided Methods/Variables in Module
------------------------------------
- context: ComponentContext
  - Same as Original
- manifest: ComponentManifest
  - Same as Original
- logger: Logger
  - Same as Original


---------------
Define Autowire
---------------
It can be autowiring module to module.
```java
    @Autowired
    ModelSample modelSample;
```

-------------
Define Events
-------------
It can invoke method for some events in module.

Provide Annotation of Events
- EventRegisteredComponent
  - You can set PackageName of Target Component.
- EventUnregisteredComponent
  - You can set PackageName of Target Component.
- EventChangedConfiguration
- EventRegisteredIcm
  - You can set PackageName of Target Inter Component Method.
- EventUnregisteredIcm
  - You can set PackageName of Target Inter Component Method.

```java
	@EventRegisteredComponent("package.name.of.target.Component")
	public void eventRegisteredTargetComponent(){
	  // Code Something
	}
	
	@EventRegisteredIcm
	public void eventRegisteredAnyComponent(){
	  // Code Something
	}
	
	@EventUnregisteredIcm("canonical.name.of.target.Icm")
	public void eventUnregisteredTargetIcm(){
		// Code Something
	}
	
	@EventChangedConfiguration
	public void eventChangedConfiguration(NodeList config){
	  // Code Something
	}
```
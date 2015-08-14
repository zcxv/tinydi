# TinyDI - A lightweight Java dependency framework with javax.inject support (through @Inject, @Named, @Singleton) #

## Goals: ##
  * minimal footprint (6 classes and a few KBytes alltogether)
  * high performance (no AOP, no XML)
  * restricted to java member variable injection (via setters only)
  * primarily for Servlet environment (support via Servlet Listener and a base Servlet class, including Google App Engine)

## Maven ##
```
<dependency>
  <groupId>com.googlecode.tinydi</groupId>
  <artifactId>tinydi</artifactId>
  <version>1.2</version>
</dependency>
```

## How does it work? ##

Definition of your managed bean (the subject of injection). The annotations below are from the `javax.inject` package.

```
@Named
public class ManagedBean {

}
```

Definition of your bean with alias:

```
@Named("alias")
public class AliasedManagedBean {

}
```

Definition of a singleton bean:

```
@Named
@Singleton
public class SingletonManagedBean {

}
```

Injecting dependencies as class members:

```
public class BeanTest {
  @Inject 
  private ManagedBean bean; // injected by java type
  
  @Inject 
  private SingletonManagedBean singleton;
  
  @Inject
  @Named("alias")
  private Object aliasedManagedBean; // injected by alias

  // setter functions ommitted here for simplicity
}
```

## Initialization of the Dependency Injection Framework ##

Before make use of dependency injection, make sure, that your subjects (managed beans) to be injected are scanned by the TinyDI framework.
Either use the following code snippet, or utilize `TinyDependencyInjectionServletContextListener` in Servlet environment (see later).

```
  // scans classes of the specified package recursively for Annotations:
  ClassfileDependencyScanner scanner = new ClassfileDependencyScanner();
  scanner.scan("com.mycompany.myapp"); 
```

Performing the injection of dependencies of a class (transitively):
```
  BeanTest bean = new BeanTest();
  Injector.inject(bean);
```

or

```
  public class BeanTest {

    public BeanTest() {
      Injector.inject(this);  // not preferred, due to dependency to the Framework
    }

    // Injected members and their setters ommitted here for simplicity    
  }
```

Injection automated in a Servet (implemented in `AbstractDIServlet`'s `init()`):
```
public class MeasurementController extends AbstractDIServlet {

  @Inject
  private MeasurementService service;

  // setter ommitted here for simplicity
}
```

web.xml: (notice the `TinyDiServletContextListener` and the configuration parameter of the java package root)

```
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://java.sun.com/xml/ns/javaee" xmlns:web="http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd" xsi:schemaLocation="http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd" id="WebApp_ID" version="2.5">
  <servlet>
    <servlet-name>measurement</servlet-name>
    <servlet-class>com.perfaction.sitemonitor.controller.MeasurementController</servlet-class>
  </servlet>
  
  <servlet-mapping>
    <servlet-name>measurement</servlet-name>
    <url-pattern>/measure/</url-pattern>
  </servlet-mapping>
  
  <context-param>
    <param-name>tinyDI.packageRoot</param-name>
    <param-value>com.perfaction</param-value>
  </context-param>
    
  <listener>
    <description>Tiny Dependency Injector repository</description>
    <listener-class>com.tinydi.TinyDiServletContextListener</listener-class>
  </listener>

</web-app>
```

## Example for injecting static instances ##

Here a JDO `PersistenceManagerFactory` instance is wrapped up into a `javax.inject.Provider<T>` pattern.

```
@Named
@Singleton
public final class PMF implements Provider<PersistenceManagerFactory> {
  
  private static final PersistenceManagerFactory pmfInstance =
    JDOHelper.getPersistenceManagerFactory("google-appengine");

  public PersistenceManagerFactory get() {
    return pmfInstance;
  }
} 
```

Notice, that the `JdoDaoImpl` class got independent from the static object creation using the dependency injection.

```
public class JdoDaoImpl {

  @Inject
  @Named("PMF")
  protected Provider<PersistenceManagerFactory> pmf;
  ...
  public void delete(Object entity) {
    PersistenceManager pm = pmf.get().getPersistenceManager();
    pm.deletePersistent(entity);
  }
}
```

## Releases+Functionality ##
1.2 Fix: scanner should ignore non-class files on the scanned classpath (e.g. property files, etc.)

1.1 Dependency injector scans beans in jar files too (e.g. jar in /WEB-INF/lib)

1.0 First release (Dependency Injection through standard javax.inject Annotations, Servlet support)
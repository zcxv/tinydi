package com.googlecode.tinydi.servlet;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.tinydi.ClassfileDependencyScanner;
import com.googlecode.tinydi.DependencyRepository;

/** Servlet listener, which scans java packages for injectable (annotated) classes 
 * (performs the initialization of the Dependency Injection framework)
 * 
 * Sample web.xml snippet:
 * <pre>
 * &lt;context-param&gt;
 *   &lt;param-name&gt;tinyDI.packageRoot&lt;/param-name&gt;
 *   &lt;param-value&gt;com.mycompany.myapp&lt;/param-value&gt;
 * &lt;/context-param&gt;
 *   
 * &lt;listener&gt;
 *   &lt;description&gt;Initializes Tiny Dependency Injector repository&lt;/description&gt;
 *   &lt;listener-class&gt;com.googlecode.tinydi.servlet.TinyDependencyInjectionServletContextListener&lt;/listener-class&gt;
 * &lt;/listener&gt;
 * <pre>
 * @author Richard Pal
 */
public class TinyDependencyInjectionServletContextListener implements ServletContextListener {

  protected Logger logger = LoggerFactory.getLogger(getClass());

  private DependencyRepository repository = DependencyRepository.getInstance();

  private static final String CONF_PACKAGEROOT = "tinyDI.packageRoot";
  
  public void contextDestroyed(ServletContextEvent arg0) {
    logger.info("TinyDi is shutting down, releasing references to managed objects");
    repository.release();
  }

  public void contextInitialized(ServletContextEvent event) {
    final String packageRoot = event.getServletContext().getInitParameter(CONF_PACKAGEROOT);
    logger.info("TinyDi ServletContext initialization starts.");
    try {
      ClassfileDependencyScanner scanner = new ClassfileDependencyScanner();
      scanner.scan(packageRoot);

    } catch (Exception e) {
      throw new RuntimeException("failed to carry out DI", e);
    }
  }

}

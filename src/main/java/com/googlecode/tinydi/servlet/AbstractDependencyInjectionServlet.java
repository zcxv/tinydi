package com.googlecode.tinydi.servlet;

import java.text.MessageFormat;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import com.googlecode.tinydi.Injector;

/** 
 * Convenience base class to implement HTTP Servlets which require dependency injection.
 * 
 * Example:
 * 
 * <pre>
 * public class MeasurementController extends AbstractDependencyInjectionServlet {
 *
 *   @Inject
 *   private MeasurementService service;
 *
 *   // setter ommitted here for simplicity
 * }
 * </pre>
 *  
 * @author Richard Pal
 *
 */
@SuppressWarnings("serial")
public abstract class AbstractDependencyInjectionServlet extends HttpServlet {
  
  @Override
  public void init() throws ServletException {
    super.init();
    try {
      Injector.inject(this);
    } catch (Exception e) {
      e.printStackTrace();
      throw new ServletException(
          MessageFormat.format("Unable to resolve dependencies for class {0}. See the original exception for details.",
          this.getClass().getCanonicalName()));
    }
  }
  
  class DIServletInitializationException extends ServletException {

    private static final long serialVersionUID = -1513143007394946772L;
    
  }
}



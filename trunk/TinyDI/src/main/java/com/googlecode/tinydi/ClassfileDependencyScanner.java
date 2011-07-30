package com.googlecode.tinydi;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
 * Scans the java packages for annotated, injectable classes, and registers those
 * in the repository. 
 * 
 * <p>Note: the underlying repository is not thread safe.</p>
 * 
 * @author Richard Pal
 */
public class ClassfileDependencyScanner {
  
  protected Logger logger = LoggerFactory.getLogger(getClass());
  
  private DependencyRepository repository = DependencyRepository.getInstance();
  
  @SuppressWarnings("unchecked")
  public void scan(String  packageRoot) {
    long time = System.currentTimeMillis();
    logger.info(MessageFormat.format(
        "TinyDi is going to be initialised ... parsing java package: {0}", packageRoot));
    
    try {
      Iterable<Class> classes = getClasses(packageRoot);

      collectNamedEntities(classes,
          repository.getNamedBeans(),
          repository.getSingletons());

    } catch (Exception e) {
      throw new RuntimeException("failed to carry out DI", e);
    }
    time = System.currentTimeMillis() - time;
    logger.info(MessageFormat.format("TinyDi finished java package parsing. Found {0} managed objects (of which {1} singletons). Took {2} ms.",
        repository.getNamedBeans().keySet().size(), 
        repository.getSingletons().keySet().size(),
        time));
  }

  /** Collects classes which can be injected with DI */
  @SuppressWarnings("unchecked")
  private void collectNamedEntities(
      Iterable<Class> classes, 
      Map<String, Class> namedEntities,
      Map<Class, Object> singletons) {
    
    for (Class clazz : classes) {
      if (clazz.isAnnotationPresent(Named.class) || clazz.isAnnotationPresent(Singleton.class)) {
        Named named = (Named) clazz.getAnnotation(Named.class);
        String name = named != null ? named.value() : null;
        if (name == null || name.length() == 0) {
          name = clazz.getSimpleName();
        }
        if (namedEntities.get(name) != null) {
          throw new RuntimeException(
              MessageFormat
                  .format(
                      "Named entity {0} is double defined. Found at classes {1} and {2}",
                      name, namedEntities.get(name).getCanonicalName(), clazz
                          .getCanonicalName()));
        }
        namedEntities.put(name, clazz);
        logger.info(MessageFormat.format(
            "{0} class is registered with alias >{1}<", clazz.getCanonicalName(), name));
        
        // register singletons:
        if (clazz.isAnnotationPresent(Singleton.class)) {
          singletons.put(clazz, null);
          logger.info(MessageFormat.format(
              "{0} class (alias >{1}<) is singleton", clazz.getCanonicalName(), name));
        }
        
        // register the class' interfaces:
        Class[] interfaces = clazz.getInterfaces();
        Map<Class, Class> interfaceMappings = repository.getInterfaces();
        for (Class iface: interfaces) {
          Class alreadyBound = interfaceMappings.get(iface);
          if (alreadyBound == null) {
            interfaceMappings.put(iface, clazz);
          } else {
            logger.debug(MessageFormat.format("Interface >{0}< is already bound to class >{1}<, so it won't be additionally bound to class >{2}<",
                iface.getCanonicalName(), alreadyBound.getCanonicalName(), clazz.getCanonicalName()));
          }
        }
        
      }
    }
  }

  /**
   * Scans all classes accessible from the context class loader which belong to
   * the given package and subpackages.
   * 
   * @param packageName
   *          The base package
   * @return The classes
   * @throws ClassNotFoundException
   * @throws IOException
   * @see http://stackoverflow.com/questions/862106/how-to-find-annotated-methods-in-a-given-package
   */
  @SuppressWarnings("unchecked")
  private Iterable<Class> getClasses(String packageName)
      throws ClassNotFoundException, IOException {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    String path = packageName.replace('.', '/');
    Enumeration<URL> resources = classLoader.getResources(path);
    List<File> dirs = new ArrayList<File>();
    while (resources.hasMoreElements()) {
      URL resource = resources.nextElement();
      dirs.add(new File(resource.getFile()));
    }
    List<Class> classes = new ArrayList<Class>();
    for (File directory : dirs) {
      classes.addAll(findClasses(directory, packageName));
    }

    return classes;
  }

  /**
   * Recursive method used to find all classes in a given directory and subdirs.
   * 
   * @param directory
   *          The base directory
   * @param packageName
   *          The package name for classes found inside the base directory
   * @return The classes
   * @throws ClassNotFoundException
   * @see http://stackoverflow.com/questions/862106/how-to-find-annotated-methods-in-a-given-package
   */
  @SuppressWarnings("unchecked")
  private List<Class> findClasses(File directory, String packageName)
      throws ClassNotFoundException {
    List<Class> classes = new ArrayList<Class>();
    if (!directory.exists()) {
      return classes;
    }
    File[] files = directory.listFiles();
    for (File file : files) {
      if (file.isDirectory()) {
        classes.addAll(findClasses(file, packageName + "." + file.getName()));
      } else if (file.getName().endsWith(".class")) {
        classes.add(Class.forName(packageName + '.'
            + file.getName().substring(0, file.getName().length() - 6)));
      }
    }
    return classes;
  }
}
package net.thecodemaster.evd;

import java.util.List;

import net.thecodemaster.evd.analyzer.Analyzer;
import net.thecodemaster.evd.analyzer.SecurityVulnerabilityAnalyzer;
import net.thecodemaster.evd.constant.Constant;
import net.thecodemaster.evd.graph.CallGraph;
import net.thecodemaster.evd.helper.Creator;
import net.thecodemaster.evd.reporter.Reporter;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * This has to be a singleton class.
 * 
 * @author Luciano Sampaio
 */
public class Manager {

  // This object controls which analyzers are going to be executed to perform the security vulnerability
  // detection.
  private static volatile Manager instance = null;
  private List<Analyzer>          analyzers;
  private Reporter                reporter;

  private Manager() {
  }

  public static void resetManager() {
    instance = null;
  }

  public static Manager getInstance() {
    if (instance == null) {
      synchronized (Manager.class) {
        if (instance == null) {
          instance = new Manager();

          IPreferenceStore store = Activator.getDefault().getPreferenceStore();

          instance.addAnalyzers(store);
          instance.addOutputs(store);
        }
      }
    }
    return instance;
  }

  private void addAnalyzers(IPreferenceStore store) {
    // Get the options checked by the developer.
    boolean cookiePoisoning = store.getBoolean(Constant.PrefPageSecurityVulnerability.FIELD_COOKIE_POISONING);
    boolean crossSiteScripting = store.getBoolean(Constant.PrefPageSecurityVulnerability.FIELD_CROSS_SITE_SCRIPTING);
    boolean sqlInjection = store.getBoolean(Constant.PrefPageSecurityVulnerability.FIELD_SQL_INJECTION);
    boolean securityMisconfiguration = store.getBoolean(Constant.PrefPageSecurityVulnerability.FIELD_SECURITY_MISCONFIGURATION);

    // If at least one was selected, the analyzer is added to the list.
    if (cookiePoisoning || crossSiteScripting || sqlInjection || securityMisconfiguration) {
      addAnalyzer(new SecurityVulnerabilityAnalyzer(cookiePoisoning, crossSiteScripting, sqlInjection, securityMisconfiguration));
    }
  }

  private void addAnalyzer(Analyzer analyzer) {
    if (null == analyzers) {
      analyzers = Creator.newList();
    }

    analyzers.add(analyzer);
  }

  private void addOutputs(IPreferenceStore store) {
    boolean problemView = store.getBoolean(Constant.PrefPageSettings.FIELD_OUTPUT_PROBLEMS_VIEW);
    boolean textFile = store.getBoolean(Constant.PrefPageSettings.FIELD_OUTPUT_TEXT_FILE);
    boolean xmlFile = store.getBoolean(Constant.PrefPageSettings.FIELD_OUTPUT_XML_FILE);

    reporter = new Reporter(problemView, textFile, xmlFile);
  }

  /**
   * @param progressMonitor
   */
  public void setProgressMonitor(IProgressMonitor progressMonitor) {
    reporter.setProgressMonitor(progressMonitor);
  }

  public void run(List<IResource> resources, CallGraph callGraph) {
    for (Analyzer analyzer : analyzers) {
      analyzer.run(resources, callGraph, reporter);
    }
  }

}
package net.thecodemaster.sap.verifiers.security.vulnerabilities;

import net.thecodemaster.sap.ui.l10n.Messages;
import net.thecodemaster.sap.verifiers.Verifier;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;

/**
 * @author Luciano Sampaio
 */
public class SecurityMisconfigurationVerifier extends Verifier {

  public SecurityMisconfigurationVerifier() {
    super(Messages.Plugin.SECURITY_MISCONFIGURATION_VERIFIER);
  }

  @Override
  public boolean visit(MethodDeclaration node) {
    System.out.println("SecurityMisconfigurationVerifier - MethodDeclaration " + node.getName());

    return true;
  }

  @Override
  public boolean visit(MethodInvocation node) {
    System.out.println("SecurityMisconfigurationVerifier - MethodInvocation " + node.getName());

    return true;
  }
}

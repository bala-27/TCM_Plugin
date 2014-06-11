package net.thecodemaster.evd.verifier;

import java.util.List;
import java.util.Map;

import net.thecodemaster.evd.constant.Constant;
import net.thecodemaster.evd.graph.BindingResolver;
import net.thecodemaster.evd.graph.CallGraph;
import net.thecodemaster.evd.graph.DataFlow;
import net.thecodemaster.evd.graph.Parameter;
import net.thecodemaster.evd.helper.Creator;
import net.thecodemaster.evd.point.ExitPoint;
import net.thecodemaster.evd.reporter.Reporter;
import net.thecodemaster.evd.xmlloader.LoaderExitPoint;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;

/**
 * The verifier is the class that actually knows how to find the vulnerability and the one that performs this
 * verification. Each verifier can reimplement/override methods of add new behavior to them.
 * 
 * @author Luciano Sampaio
 */
public abstract class Verifier extends CodeAnalyzer {

	/**
	 * The id of the current verifier.
	 */
	private final int				id;
	/**
	 * The name of the current verifier.
	 */
	private final String		name;
	/**
	 * The object that know how and where to report the found vulnerabilities.
	 */
	private Reporter				reporter;
	/**
	 * List with all the ExitPoints of this verifier.
	 */
	private List<ExitPoint>	exitPoints;
	/**
	 * The rules that the current parameter must obey.
	 */
	private List<Integer>		rules;

	/**
	 * @param name
	 *          The name of the verifier.
	 * @param id
	 *          The id of the verifier.
	 * @param listEntryPoints
	 *          List with all the EntryPoints methods.
	 */
	public Verifier(int id, String name) {
		this.id = id;
		this.name = name;
	}

	protected int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	protected Reporter getReporter() {
		return reporter;
	}

	protected List<ExitPoint> getExitPoints() {
		if (null == exitPoints) {
			// Loads all the ExitPoints of this verifier.
			exitPoints = (new LoaderExitPoint(getId())).load();
		}

		return exitPoints;
	}

	public List<Integer> getRules() {
		return rules;
	}

	public void setRules(List<Integer> rules) {
		this.rules = rules;
	}

	/**
	 * Notifies that a subtask of the main task is beginning.
	 * 
	 * @param taskName
	 *          The text that will be displayed to the user.
	 */
	protected void setSubTask(String taskName) {
		if ((null != getReporter()) && (null != getReporter().getProgressMonitor())) {
			getReporter().getProgressMonitor().subTask(taskName);
		}
	}

	protected void reportVulnerability(DataFlow dataFlow) {
		if (null != getReporter()) {
			getReporter().addProblem(getCurrentResource(), getId(), dataFlow);
		}
	}

	protected ExitPoint getExitPointIfMethodIsOne(Expression method) {
		for (ExitPoint currentExitPoint : getExitPoints()) {
			if (BindingResolver.methodsHaveSameNameAndPackage(currentExitPoint, method)) {
				// 01 - Get the expected arguments of this method.
				Map<Parameter, List<Integer>> expectedParameters = currentExitPoint.getParameters();

				// 02 - Get the received parameters of the current method.
				List<Expression> receivedParameters = BindingResolver.getParameters(method);

				// 03 - It is necessary to check the number of parameters and its types
				// because it may exist methods with the same names but different parameters.
				if (expectedParameters.size() == receivedParameters.size()) {
					boolean isMethodAnExitPoint = true;
					int index = 0;
					for (Parameter expectedParameter : expectedParameters.keySet()) {
						ITypeBinding typeBinding = receivedParameters.get(index++).resolveTypeBinding();

						// Verify if all the parameters are the ones expected. However, there is a case
						// where an Object is expected, and any type is accepted.
						if (!BindingResolver.parametersHaveSameType(expectedParameter.getType(), typeBinding)) {
							isMethodAnExitPoint = false;
							break;
						}
					}

					if (isMethodAnExitPoint) {
						return currentExitPoint;
					}
				}
			}
		}

		return null;
	}

	/**
	 * An exit point might have more that one parameter and each of these parameter might have different rules (acceptable
	 * values). That is why we need to check.
	 * 
	 * @param rules
	 * @param parameter
	 * @return
	 */
	protected boolean matchRules(List<Integer> rules, Expression parameter) {
		if (null == parameter) {
			// There is nothing we can do to verify it.
			return true;
		}

		// -1 Anything is valid.
		// 0 Only sanitized values are valid.
		// 1 LITERAL and sanitized values are valid.
		for (Integer astNodeValue : rules) {
			if (astNodeValue == Constant.LITERAL) {
				switch (parameter.getNodeType()) {
					case ASTNode.STRING_LITERAL:
					case ASTNode.CHARACTER_LITERAL:
					case ASTNode.NUMBER_LITERAL:
					case ASTNode.NULL_LITERAL:
						return true;
				}
			} else if (astNodeValue == parameter.getNodeType()) {
				return true;
			}
		}

		return false;
	}

	/**
	 * The public run method that will be invoked by the Analyzer.
	 * 
	 * @param resources
	 * @param callGraph
	 * @param reporter
	 * @return
	 */
	public List<DataFlow> run(List<IResource> resources, CallGraph callGraph, Reporter reporter) {
		setCallGraph(callGraph);
		this.reporter = reporter;
		List<DataFlow> allVulnerablePaths = Creator.newList();

		setSubTask(getName());

		// 01 - Run the vulnerability detection on all the provided resources.
		for (IResource resource : resources) {
			// 02 - Some methods will need to have access to the resource that is currently being analyzed.
			// but we do not want to pass it to all these methods as a parameter.
			setCurrentResource(resource);

			run(allVulnerablePaths, resource);
		}

		return allVulnerablePaths;
	}

	/**
	 * Iterate over all the method declarations found in the current resource.
	 * 
	 * @param allVulnerablePaths
	 * @param resource
	 */
	protected void run(List<DataFlow> allVulnerablePaths, IResource resource) {
		// 01 - Get the list of methods in the current resource.
		Map<MethodDeclaration, List<Expression>> methods = null;// getCallGraph().getMethods(resource);

		// 02 - Get all the method invocations of each method declaration.
		for (List<Expression> invocations : methods.values()) {

			// 03 - Iterate over all method invocations to verify if it is a ExitPoint.
			for (Expression method : invocations) {
				ExitPoint exitPoint = getExitPointIfMethodIsOne(method);

				if (null != exitPoint) {
					setCurrentCompilationUnit(BindingResolver.getParentCompilationUnit(method));

					// 04 - This is an ExitPoint method and it needs to be verified.
					run(allVulnerablePaths, method, exitPoint);
				}
			}
		}
	}

	protected void run(List<DataFlow> allVulnerablePaths, Expression method, ExitPoint exitPoint) {
		// 01 - Get the parameters (received) from the current method.
		List<Expression> receivedParameters = BindingResolver.getParameters(method);

		// 02 - Get the expected parameters of the ExitPoint method.
		Map<Parameter, List<Integer>> expectedParameters = exitPoint.getParameters();

		int index = 0;
		int depth = 0;
		for (List<Integer> currentRules : expectedParameters.values()) {
			// If the rules are null, it means the expected parameter can be anything. (We do not care for it).
			if (null != currentRules) {
				setRules(currentRules);
				Expression expression = receivedParameters.get(index);
				DataFlow dataFlow = new DataFlow(expression);

				// 03 - Check if there is a marker, in case there is, we should BELIEVE it is not vulnerable.
				if (!hasMarkerAtPosition(expression)) {
					inspectNode(depth, dataFlow, expression);
					if (dataFlow.isVulnerable()) {
						allVulnerablePaths.add(dataFlow);
						reportVulnerability(dataFlow);
					}
				}
			}
			index++;
		}
	}

	@Override
	protected void inspectNode(int depth, DataFlow dataFlow, Expression node) {
		if (null == node) {
			return;
		}

		// 01 - Check if the expression matches the rules.
		if (matchRules(getRules(), node)) {
			return;
		}

		// 02 - Invoke the inspectNode from the superclass.
		super.inspectNode(depth, dataFlow, node);
	}

}

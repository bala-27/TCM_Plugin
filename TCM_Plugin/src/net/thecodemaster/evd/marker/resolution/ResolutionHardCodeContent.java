package net.thecodemaster.evd.marker.resolution;

import net.thecodemaster.evd.logger.PluginLogger;

import org.eclipse.core.resources.IMarker;

/**
 * @author Luciano Sampaio
 */
public class ResolutionHardCodeContent extends AbstractResolution {

	public ResolutionHardCodeContent(int position, ResolutionMessage resolutionMessage, IMarker marker) {
		super(position, marker);

		setLabel(resolutionMessage.getLabel());
		setDescription(resolutionMessage.getDescription());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run(IMarker marker) {
		try {
			// 01 - The comment that will be insert into the source code.
			String comment = String.format("// FIXME - %s", marker.getAttribute(IMarker.MESSAGE, ""));

			runInsertComment(marker, comment);
		} catch (Exception e) {
			PluginLogger.logError(e);
		}
	}
}

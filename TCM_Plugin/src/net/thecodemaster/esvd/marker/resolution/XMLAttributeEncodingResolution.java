package net.thecodemaster.esvd.marker.resolution;

import org.eclipse.core.resources.IMarker;

public class XMLAttributeEncodingResolution extends AbstractEncodingResolution {

	public XMLAttributeEncodingResolution(int position, IMarker marker) {
		super(position, marker);

		setLabel(generateLabel());
		setDescription(generateDescription());
	}

	private String generateLabel() {
		return "XML Attribute Encoder";
	}

	private String generateDescription() {
		StringBuffer buf = new StringBuffer();
		String instruction = "-- Double click selection to auto-generate encoding method --";
		String description = "";

		// FIXME Improve this description
		description = "Encode data for use in an XML attribute. The implementation should follow the XML Encoding Standard from the W3C. The use of a real XML parser is highly encouraged. However, in the hopefully rare case that you need to make sure that data is safe for inclusion in an XML document and cannot use a parse, this method provides a safe mechanism to do so.";

		buf.append(instruction);
		buf.append("<p><p>");
		buf.append(description);

		return buf.toString();
	}

	@Override
	protected String getEsapiEncoderMethodName() {
		return "encodeForXMLAttribute";
	}
}
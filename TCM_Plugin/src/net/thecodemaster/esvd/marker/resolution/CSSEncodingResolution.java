package net.thecodemaster.esvd.marker.resolution;


public class CSSEncodingResolution extends AbstractEncodingResolution {

	public CSSEncodingResolution(int position) {
		super(position);

		setLabel(generateLabel());
		setDescription(generateDescription());
	}

	private String generateLabel() {
		return "CSS Encoder";
	}

	private String generateDescription() {
		StringBuffer buf = new StringBuffer();
		String instruction = "-- Double click selection to auto-generate encoding method --";
		String description = "";

		// FIXME Improve this description
		description = "Encode data for use in Cascading Style Sheets (CSS) content.";

		buf.append(instruction);
		buf.append("<p><p>");
		buf.append(description);

		return buf.toString();
	}

	@Override
	protected String getEsapiEncoderMethodName() {
		return "encodeForCSS";
	}
}

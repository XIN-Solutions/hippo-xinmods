package nz.xinsolutions.servlet;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.imgscalr.Scalr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Author: Marnix Kok <marnix@xinsolutions.co.nz>
 *
 *     This servlet streams from the same hosts DAM and is able to apply one or multiple
 *     operations on it.
 *
 */
public class AssetModifierServlet extends HttpServlet {

	/**
	 * Logger
	 */
	private static final Logger LOG = LoggerFactory.getLogger(AssetModifierServlet.class);

	public static final int CROP_PARAM_WIDTH = 0;
	public static final int CROP_PARAM_HEIGHT = 1;

	public static final int SCALE_PARAM_X = 0;
	public static final int SCALE_PARAM_Y = 1;

	public static final String PATH_BINARIES = "binaries";
	public static final String PATH_ASSETMOD = "assetmod";

	private static int CACHE_TIME = 5 * 60;

	/**
	 * Mimetypes to write to response
	 */
	private static Map<String, String> s_mimeTypes = new LinkedHashMap<String, String>() {{
		put("jpg", "image/jpeg");
		put("jpeg", "image/jpeg");
		put("gif", "image/gif");
		put("png", "image/png");
	}};


	/**
	 * Stream the image and add the manipulations to it.
	 *
	 * @param req  	request instance
	 * @param resp	response instance
	 * @throws ServletException
	 * @throws IOException
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		String fullUrl = req.getRequestURI();
		String ext = getExtension(fullUrl);
		String host = getOriginHost(req.getRequestURL().toString());
		String context = req.getServletContext().getContextPath();

		String rawInstructions = getInstructionString(fullUrl);
		Instruction[] instruction = interpretInstruction(rawInstructions);

		String binaryLocation = getBinaryLocation(fullUrl);
		URL binaryUrl = new URL(host + context + binaryLocation);

		BufferedImage buffImg = ImageIO.read(binaryUrl);

		if (buffImg == null) {
			pageNotFound(resp);
			return;
		}

		Float quality = null;

		// go over all the instructions we found
		for (Instruction instr : instruction) {

			if (instr.getName().equals("scale")) {
				buffImg = scale(buffImg, instr);
			}
			else if (instr.getName().equals("crop")) {
				buffImg = crop(buffImg, instr);
			}
			else if (instr.getName().equals("filter")) {
				buffImg = filter(buffImg, instr);
			}
			else if (instr.getName().equals("quality")) {
				String qualityStr = instr.getParam(0);
				if (qualityStr != null) {
					quality = Float.parseFloat(qualityStr);
				}
			}
			else {
				LOG.info("Do not know about: {}", instr.getName());
			}

		}

		// set the response headers
		setImageResponseHeaders(resp, ext);

		if (shouldRenderWithQualitySettings(ext, quality)) {
			// create quality parameters
			JPEGImageWriteParam jpegParams = new JPEGImageWriteParam(null);
			jpegParams.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
			jpegParams.setCompressionQuality(Math.min(1, Math.max(0, quality)));

			// image writer instance, set output stream
			ImageWriter imgWriter = ImageIO.getImageWritersByFormatName("jpg").next();
			ImageOutputStream imgOutputStream = ImageIO.createImageOutputStream(resp.getOutputStream());
			imgWriter.setOutput(imgOutputStream);

			// do the magic
			IIOImage iioImage = new IIOImage(buffImg, null, null);
			imgWriter.write(null, iioImage, jpegParams);

		}
		else {
			// write the resulting image to the response
			ImageIO.write(buffImg, ext, resp.getOutputStream());
		}
	}

	private boolean shouldRenderWithQualitySettings(String ext, Float quality) {
		return (ext.equalsIgnoreCase("jpg") || ext.equalsIgnoreCase("jpeg")) && quality != null;
	}


	protected void pageNotFound(HttpServletResponse resp) {
		resp.setStatus(404);
		resp.setHeader("Content-Length", "0");
	}

	// ------------------------------------------------------------------------------------------------
	//		Image operations
	// ------------------------------------------------------------------------------------------------

	/**
	 * Apply a colour filter
	 *
	 * @param img		is the image to operate on
	 * @param instr		the instructions to execute
	 * @return a new buffered image
	 */
	protected BufferedImage filter(BufferedImage img, Instruction instr) {
		if (instr.isEmpty(0)) {
			LOG.error("Expecting a filter name");
			return img;
		}

		String type = instr.getParam(0);
		switch (type) {
			case "grayscale":
			case "greyscale":
				return Scalr.OP_GRAYSCALE.filter(img, null);

			case "darker":
				return Scalr.OP_DARKER.filter(img, null);

			case "brighter":
				return Scalr.OP_BRIGHTER.filter(img, null);

			default:
				LOG.error("Don't know about filter type: {}", type);
				return img;

		}

	}

	/**
	 * Scale to a certain size.
	 *
	 * @param img		is the buffered image to operate on
	 * @param instr		the instructions
	 * @return the buffered image
	 */
	protected BufferedImage scale(BufferedImage img, Instruction instr) {
		if (instr.isEmpty(SCALE_PARAM_X) && instr.isEmpty(SCALE_PARAM_Y)) {
			LOG.error("Cannot scale nothing");
			return img;
		}

		Integer size = 0;
		Scalr.Mode mode = null;

		// `x` param empty? fit to height.
		if (instr.isEmpty(SCALE_PARAM_X)) {
			size = instr.getIntParam(SCALE_PARAM_Y);
			mode = Scalr.Mode.FIT_TO_HEIGHT;
		}
		else if (instr.isEmpty(SCALE_PARAM_Y)) {
			size = instr.getIntParam(SCALE_PARAM_X);
			mode = Scalr.Mode.FIT_TO_WIDTH;
		} else {
			LOG.error("Cowardly refusing to stretch nastily");
			return img;
		}

		if (size == null) {
			LOG.error("Parameter was not a number.");
			return img;
		}

		return Scalr.resize(img, Scalr.Method.QUALITY, mode, size);
	}


	/**
	 * This operation will crop the buffered image to a decent size.
	 *
	 * @param img
	 * @param instr
	 * @return
	 */
	protected BufferedImage crop(BufferedImage img, Instruction instr) {

		// make sure instructions are proper
		if (instr.isEmpty(CROP_PARAM_WIDTH) || instr.isEmpty(CROP_PARAM_HEIGHT)) {
			LOG.error("Expecting two cropping parameters, skipping.");
			return img;
		}

		Integer
			cropW = instr.getIntParam(CROP_PARAM_WIDTH),
			cropH = instr.getIntParam(CROP_PARAM_HEIGHT);

		if (cropW == null || cropH == null) {
			LOG.error("Either width or height isn't a proper number.");
			return img;
		}

		int
			imgW = img.getWidth(),
			imgH = img.getHeight(),
			centerX = imgW / 2,
			centerY = imgH / 2,
			offsetX = centerX - cropW / 2,
			offsetY = centerY - cropH / 2
		;


		return
			Scalr.crop(img,
				// bound to 0, 0
				Math.max(0, offsetX), Math.max(0, offsetY),

				// bound to width, height
				Math.min(imgW, cropW), Math.min(imgH, cropH)
			);
	}


	// ------------------------------------------------------------------------------------------------
	//		Instruction parsing
	// ------------------------------------------------------------------------------------------------


	protected String getInstructionString(String fullUrl) {
		int
			startIdx = fullUrl.indexOf(PATH_ASSETMOD) + PATH_ASSETMOD.length(),
			endIdx = fullUrl.indexOf(PATH_BINARIES);

		return fullUrl.substring(startIdx, endIdx);
	}

	protected Instruction[] interpretInstruction(String raw) {
		return
			Arrays.stream(raw.split("/"))
				.filter(StringUtils::isNotBlank)
				.map(Instruction::new)
				.collect(Collectors.toList())
				.toArray(new Instruction[0])
			;
	}


	/**
	 * Set the headers
	 *
	 * @param resp	is the response object to put it on
	 */
	protected void setImageResponseHeaders(HttpServletResponse resp, String extension) {
		String mimeType = s_mimeTypes.getOrDefault(extension, "application/octet-stream");
		String cacheControl = String.format("max-age=%d", CACHE_TIME);

		resp.setHeader("Cache-Control", cacheControl);
		resp.setHeader("Content-Type", mimeType);
	}


	// ------------------------------------------------------------------------------------------------
	//		Query parsing
	// ------------------------------------------------------------------------------------------------



	/**
	 * @return part of the url where the actual thing lives
	 */
	protected String getBinaryLocation(String fullUrl) {
		int binariesPathIdx = fullUrl.indexOf("/" + PATH_BINARIES);
		if (binariesPathIdx == -1) {
			return null;
		}
		return fullUrl.substring(binariesPathIdx);
	}

	/**
	 * @return the origin host
	 */
	protected String getOriginHost(String fullUrl) throws MalformedURLException {
		URL url = new URL(fullUrl);
		return String.format("%s://%s:%d", url.getProtocol(), url.getHost(), getServerPort());
	}

	/**
	 * @return the port
	 */
	protected int getServerPort() {
		return 8080;
	}

	/**
	 * @return the extension from the url
	 */
	protected String getExtension(String fullUrl) {
		int lastPeriodIdx = fullUrl.lastIndexOf('.');
		if (lastPeriodIdx == -1) {
			return null;
		}
		return fullUrl.substring(lastPeriodIdx + 1);
	}


	// ------------------------------------------------------------------------------------------
	//		Instructions
	// ------------------------------------------------------------------------------------------

	/**
	 * Instruction container
	 */
	public class Instruction {

		/**
		 * Name
		 */
		private String name;

		/**
		 * Parameters
		 */
		private String[] params;

		/**
		 * Initialise data-members
		 *
		 * @param raw
		 */
		public Instruction(String raw) {
			int eqIndex = raw.indexOf("=");
			this.name = raw.substring(0, eqIndex);
			this.params = raw.substring(eqIndex + 1).split(",");
		}

		/**
		 * @return true if the parameter is empty
		 */
		public boolean isEmpty(int idx) {
			if (params.length <= idx) {
				return false;
			}
			return params[idx].equals("_");
		}

		/**
		 * @return the parameters
		 */
		public String[] getParams() {
			return params;
		}

		/**
		 * @return a specific parameter
		 */
		public String getParam(int idx) {
			return (
				params.length > idx
					? params[idx]
					: null
			);
		}

		/**
		 * @return a number parameter
		 */
		public Integer getIntParam(int idx) {
			return (
				params.length > idx
					? (NumberUtils.isDigits(params[idx]) ? Integer.parseInt(params[idx]) : null)
					: null
			);
		}

		public String getName() {
			return name;
		}

	}


}

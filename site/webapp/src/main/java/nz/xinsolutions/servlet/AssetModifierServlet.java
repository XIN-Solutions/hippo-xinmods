package nz.xinsolutions.servlet;

import nz.xinsolutions.config.SiteXinmodsConfig;
import nz.xinsolutions.core.jackrabbit.JcrSessionHelper;
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
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
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
	public static final int CROP_PARAM_X_FOCUS = 2;
	public static final int CROP_PARAM_Y_FOCUS = 3;

	public static final int SCALE_PARAM_X = 0;
	public static final int SCALE_PARAM_Y = 1;

	public static final String PATH_BINARIES = "binaries";
	public static final String PATH_ASSETMOD = "assetmod";

	private static int CACHE_TIME = 5 * 60;

	/**
	 * Session
	 */
	private static Session adminSession = null;

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

			switch (instr.getName()) {
				case "scale":
					buffImg = scale(buffImg, instr);
					break;

				case "crop":
					buffImg = crop(buffImg, instr);
					break;

				case "filter":
					buffImg = filter(buffImg, instr);
					break;

				case "quality":
					String qualityStr = instr.getParam(0);
					if (qualityStr != null) {
						quality = Float.parseFloat(qualityStr);
					}
					break;

				// just a cache-busting version.
				case "v":
					break;
				default:
					LOG.info("Do not know about: {}", instr.getName());
					break;
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

	/**
	 * @return true if we should do some quality manipulation before returning the image.
	 */
	protected boolean shouldRenderWithQualitySettings(String ext, Float quality) {
		return (ext.equalsIgnoreCase("jpg") || ext.equalsIgnoreCase("jpeg")) && quality != null;
	}


	/**
	 * Page not found returning.
	 *
	 * @param resp
	 */
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

		Float
			xFocus = instr.getFloatParam(CROP_PARAM_X_FOCUS),
			yFocus = instr.getFloatParam(CROP_PARAM_Y_FOCUS)
		;

		if (xFocus == null) {
			xFocus = 0f;
		}

		if (yFocus == null) {
			yFocus = 0f;
		}

		if (cropW == null || cropH == null) {
			LOG.error("Either width or height isn't a proper number.");
			return img;
		}

		int
			imgW = img.getWidth(),
			imgH = img.getHeight(),
			centerX = imgW / 2 + (int)(imgW * xFocus),
			centerY = imgH / 2 + (int)(imgH * yFocus);

		int halfCropW = cropW / 2;
		int halfCropH = cropH / 2;

		// if crop would go past end up image, decrease centerX
		if (centerX + halfCropW > imgW) {
			centerX -= (centerX + halfCropW) - imgW;
		}
		// if crop would go past height, decrease centerY
		if (centerY + halfCropH > imgH) {
			centerY -= (centerY + halfCropH) - imgH;
		}

		// if center - half crop is less than 0, adjust centerX to the right
		if (centerX - halfCropW < 0) {
			centerX = -(centerX - halfCropW);
		}
		// if center y - crop is less than 0, adjust centerY to the right
		if (centerY - halfCropH < 0) {
			centerY = -(centerY - halfCropH);
		}

		// starting point
		int
			offsetX = centerX - halfCropW,
			offsetY = centerY - halfCropH
		;

		int clippedOffsetX = Math.max(0, offsetX);
		int clippedOffsetY = Math.max(0, offsetY);

		return
			Scalr.crop(img,
				// bound to 0, 0
				clippedOffsetX,
				clippedOffsetY,

				// bound to width, height
				Math.min(imgW - clippedOffsetX, cropW),
				Math.min(imgH - clippedOffsetY, cropH)
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
		resp.setHeader("Content-Type", mimeType);

		Session adminSession = null;

		try {
			adminSession = getAdminJcrSession();
			SiteXinmodsConfig xmCfg = new SiteXinmodsConfig(adminSession);
			long cacheLength = xmCfg.getAssetCacheLength(CACHE_TIME);

			// set header
			String cacheControl = String.format("max-age=%d", cacheLength);
			resp.setHeader("Cache-Control", cacheControl);
		}
		catch (Exception ex) {
			LOG.error("Couldn't set response header, caused by: ", ex);

		}
	}

	/**
	 * @return the administrative session
	 * @throws RepositoryException
	 */
	protected static synchronized Session getAdminJcrSession() throws RepositoryException {

		if (adminSession == null) {
			return (adminSession = JcrSessionHelper.loginAdministrative());
		}

		if (!adminSession.isLive()) {
			adminSession.refresh(false);
		}

		return adminSession;
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
		public Float getFloatParam(int idx) {
			return (
				params.length > idx
					? (NumberUtils.isNumber(params[idx]) ? Float.parseFloat(params[idx]) : null)
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

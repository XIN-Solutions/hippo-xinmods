document.addEventListener('DOMContentLoaded', async () => {

	try {

		const ui = await UiExtension.register();
		const extConfig = JSON.parse(ui.extension.config || "{}");
		const options = await ui.dialog.options();
		const values = JSON.parse(options.value);
		let value = values.value? JSON.parse(values.value) : {x:0, y:0};

		let zoomValue = 1;

		const elImage = document.querySelector('[data-image]');
		const elTarget = document.querySelector('[data-target]');
		const elBox = document.querySelector('[data-box]');
		const elBody = document.querySelector('body');
		const elDone = document.querySelector("#done");


		/**
		 * Retrieve image information by reading the iframe's parent path, it should include
		 * that `path` parameter with the current gallery element. We then use that to retrieve
		 * the actual image from `/site/binaries` and get width and height from it.
		 *
		 * @returns {Promise<{width:number,height:number,imageUrl:string}>}
		 */
		async function getImageInfo() {

			return new Promise((resolve, reject) => {

				const windowParams = parent.window.document.location.search;
				const pathEl = windowParams.split("&").filter((el) => el.indexOf("path=") === 0);
				if (pathEl.length === 0) {
					reject("No path found in location object");
					return;
				}

				const path = pathEl[0].split("=")[1];
				const pathPrefix = (extConfig.siteUrl || "/site/binaries");

				const img = new Image();
				img.onload = (evt) => {

					resolve({
						width: img.width,
						height: img.height,
						imageUrl: pathPrefix + path
					});

				};

				img.src = pathPrefix + path;

			});
		}

		// get image information
		const {width, height, imageUrl} = await getImageInfo();


		/**
		 * When the wheel has been scrolled do some math and redraw the scene.
		 *
		 * @param evt	{MouseEvent} the mouse event
		 * @returns {boolean} false to prevent more event stuff.
		 */
		function zoom(evt) {
			evt.preventDefault();

			zoomValue += evt.deltaY * -0.005;
			zoomValue = Math.min(Math.max(.125, zoomValue), 4);

			drawImage();
			drawTarget();

			return false;
		}


		/**
		 * This projects the x and y coordinates from the mouse click even to a number between
		 * -0.5 and 0.5 where 0 is the center for both x and y coordinates. This is the value
		 * stored in the JCR to indicate the focus location.
		 *
		 * @param x	{number} the x position
		 * @param y {number} the y position
		 * @returns {{x: number, y: number}}
		 */
		function targetValues(x, y) {

			// center of screen.
			const centerX = (elBox.clientWidth / 2);
			const centerY = (elBox.clientHeight / 2);

			// size of image with zoom factor
			const imgWidth = width * zoomValue;
			const imgHeight = height * zoomValue;

			// where does image start?
			const xStart = (centerX - (imgWidth / 2));
			const yStart = (centerY - (imgHeight / 2));

			const realX = Math.min(xStart + imgWidth, Math.max(xStart, x));
			const realY = Math.min(yStart + imgHeight, Math.max(yStart, y));

			return {
				x: ((realX - centerX) / imgWidth),
				y: ((realY - centerY) / imgHeight)
			};

		}

		/**
		 * Draw the image at the correct location based on the zoom state.
		 */
		function drawImage() {

			const newWidth = width * zoomValue;
			const newHeight = height * zoomValue;

			elImage.style.left = ((elBox.clientWidth / 2) - (newWidth / 2)) + "px";
			elImage.style.top = ((elBox.clientHeight / 2) - (newHeight / 2)) + "px";
			elImage.style.width = newWidth + "px";
			elImage.style.height = newHeight + "px";
		}


		/**
		 * Draw a target at the location of the last click, or in the middle of no
		 * selection was made.
		 */
		function drawTarget() {

			// center of screen.
			const centerX = (elBox.clientWidth / 2);
			const centerY = (elBox.clientHeight / 2);

			// size of image with zoom factor
			const imgWidth = width * zoomValue;
			const imgHeight = height * zoomValue;

			const targetX = imgWidth * value.x + centerX;
			const targetY = imgHeight * value.y + centerY;

			elTarget.style.width = (50 * zoomValue) + "px";
			elTarget.style.height = (50 * zoomValue) + "px";
			elTarget.style.borderWidth = (5 * zoomValue) + "px";
			elTarget.style.left = `${targetX - 25 * zoomValue}px`;
			elTarget.style.top = `${targetY - 25 * zoomValue}px`;

		}


		// set the background.
		elImage.src = imageUrl;

		// wheel scrolled? zoomage.
		elBox.addEventListener('wheel', zoom);

		// screen resized? REDRAW
		elBody.onresize = (evt) => {
			drawImage();
			drawTarget();
		};

		// click on screen? change target values.
		elBox.onclick = (evt) => {
			value = targetValues(evt.clientX, evt.clientY);
			drawTarget();
		};

		// clicked the done button?
		elDone.onclick = (evt) => {
			ui.dialog.close(JSON.stringify(value));
		};

		// initial drawing.
		drawImage();
		drawTarget();
	}
	catch(error) {
		console.error('Failed to register extension:', error.message);
		console.error('- error code:', error.code);
		console.error(error);
	}

});

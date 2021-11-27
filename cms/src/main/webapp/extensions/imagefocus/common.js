/**
 * Common service
 */
window.Common = Object.assign(new function() {} (), {


	/**
	 * Retrieve image information by reading the iframe's parent path, it should include
	 * that `path` parameter with the current gallery element. We then use that to retrieve
	 * the actual image from `/site/binaries` and get width and height from it.
	 *
	 * @returns {Promise<{width:number,height:number,imageUrl:string}>}
	 */
	getImageInfo: async function(extConfig) {

		return new Promise((resolve, reject) => {

			// delicious way to find the name of the document we're editing.
			const windowParams = parent.parent.window.document.location.pathname;
			const pathEl = windowParams.split("/path");
			if (pathEl.length === 1) {
				reject("No path found in location object");
				return;
			}

			const path = pathEl[1];
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


});

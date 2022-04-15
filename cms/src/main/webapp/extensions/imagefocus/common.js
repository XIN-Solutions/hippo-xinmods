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

			const windowParams = parent.window.document.location.search;
			const pathElv1 = windowParams.split("&").filter((el) => el.indexOf("path=") === 0);
			let path = null;

			if (pathElv1.length === 0) {

				// delicious way to find the name of the document we're editing.
				const windowParams = parent.parent.window.document.location.pathname;
				const pathElv2 = windowParams.split("/path");
				if (pathElv2.length === 1) {
					reject("No path found in location object");
					return;
				}
				else {
					path = pathElv2[1];
				}
			}
			else {
				path = pathElv1[0].split("=")[1];
			}

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

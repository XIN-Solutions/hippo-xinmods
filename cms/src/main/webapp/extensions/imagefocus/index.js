document.addEventListener('DOMContentLoaded', async () => {

	const button = document.querySelector("#open");
	const labels = document.querySelector("#labels")
	const loading = document.querySelector("[data-state='loading']");
	const notSet = document.querySelector("[data-state='none']");
	const valueSet = document.querySelector("[data-state='set']");
	const unavailable = document.querySelector('[data-state="unavailable"]');

	const imageThumbnail = document.querySelector('[data-image]');
	const imageContainer = document.querySelector("[data-image-container]");
	const imageTarget = document.querySelector('[data-target]');


	try {
		const ui = await UiExtension.register();
		const brDocument = await ui.document.get();
		let value = await ui.document.field.getValue() || '{"x":0, "y":0}';
		const extConfig = JSON.parse(ui.extension.config || "{}");
		const { mode } = brDocument;


		/**
		 * If in 'view' mode, add the current state label
		 */
		function initialiseLabels() {
			if (!value) {
				notSet.classList.remove('hide');
			}
			else {
				valueSet.classList.remove('hide');
			}
		}

		/**
		 * Code to initialise the button behaviour.
		 */
		function initialiseButton() {

			// show the button
			button.classList.remove('hide');

			// add click behaviour to the button
			button.addEventListener('click', async () => {
				try {

					const dialogOptions = {
						title: 'Select Image Crop Focus',
						url: './dialog.html',
						size: 'large',
						value: JSON.stringify({
							uuid: brDocument.id,
							value
						}),
					};

					const response = await ui.dialog.open(dialogOptions);
					await ui.document.field.setValue(response);
					value = response;
					setTarget();
				}
				catch (error) {
					if (error.code === 'DialogCanceled') {
						return;
					}

					console.error('Error after open dialog: ', error.code, error.message);
				}
			});
		}

		function setTarget() {
			const {width: w, height: h} = imageThumbnail;
			const {x: xPerc, y: yPerc} = JSON.parse(value);

			const cw = w / 2, ch = h / 2;
			const tX = Math.floor(cw + (w * xPerc));
			const tY = Math.floor(ch + (h * yPerc));

			imageTarget.style.left = `${tX}px`;
			imageTarget.style.top = `${tY}px`;
		}


		async function initialisePreview() {
			const imgInfo = await Common.getImageInfo(extConfig);

			if (imgInfo.imageUrl.indexOf(".svg") !== -1) {
				return false;
			}

			return new Promise((resolve, reject) => {

				const maxHeight = mode === 'edit' ? 100 : 130;
				const url = imgInfo.imageUrl.replace("/binaries", `/assetmod/scale=_,${maxHeight}/binaries`)

				imageThumbnail.onload = () => {
					setTarget();
					imageContainer.classList.remove('hide');
					loading.classList.add('hide');
					resolve(true);
				};

				imageThumbnail.src = url;
			});
		}


		labels.classList.remove('hide');

		const canPreview = await initialisePreview();
		if (canPreview) {

			if (mode === 'edit') {
				initialiseButton();
			}
			initialiseLabels();
		}
		else {
			unavailable.classList.remove('hide');
			loading.classList.add('hide');
		}


	} catch (error) {
		console.error('Failed to register extension:', error.message);
		console.error('- error code:', error.code);
	}

});


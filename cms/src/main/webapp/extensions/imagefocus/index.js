
document.addEventListener('DOMContentLoaded', async () => {

	const button = document.querySelector("#open");
	const labels = document.querySelector("#labels")
	const notSet = document.querySelector("[data-state='none']");
	const valueSet = document.querySelector("[data-state='set']");


	try {
		const ui = await UiExtension.register();
		const brDocument = await ui.document.get();
		const value = await ui.document.field.getValue();
		const extConfig = JSON.parse(ui.extension.config || "{}");
		const { mode } = brDocument;


		/**
		 * If in 'view' mode, add the current state label 
		 */
		function initialiseLabels() {
			labels.classList.remove('hide');
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
				} 
				catch (error) {
					if (error.code === 'DialogCanceled') {
						return;
					}

					console.error('Error after open dialog: ', error.code, error.message);
				}
			});
		}


		if (mode === 'edit') {
			initialiseButton()
		}
		else {
			initialiseLabels();
		}

		
	} catch (error) {
		console.error('Failed to register extension:', error.message);
		console.error('- error code:', error.code);
	}





});



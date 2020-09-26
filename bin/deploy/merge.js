/*

	Merge tool grabs configuration and a file configuration and interpolates it.

 */

const fs = require('fs');

const [interp, script, configFile, templateFile] = (process.argv);
const configStr = fs.readFileSync(configFile);
const templateStr = fs.readFileSync(templateFile);

const cfg = JSON.parse(configStr.toString());
console.log(eval("`" + templateStr.toString() + "`"));

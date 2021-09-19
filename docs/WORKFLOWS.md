# Workflow Restriction ACL descriptions

Oftentimes you want to put additional protections in place for pieces of your content that may not be moved,
renamed or deleted. This mechanism that allows additional such logic to be applied in the Bloomreach XM are the
workflow plugins. 

The XIN Mods are supplied with an enhanced version of the Documents and Folders workflow implementations. While 
not changing any of the built-in functionality, they wrap the functions with additional guards to
prevent operations from executing if workflow rules evaluate to "Deny".

To configure your workflows you must do at least two things.

1. Add a `workflow` node underneath the XIN mods module configurations at `/hippo:configuration/hippo:modules/xinmods/hippo:moduleconfig`; and
2. Change the class that is being used by the workflow plugins for the operations you wish to govern more precisely. 

## Configure the `workflow` node

The decision making process to understand whether a user can do the thing they requested works as follows:

* for the action that is being performed (rename, delete, move etc.); find the configuration node and interpret the 
rules to come to a workflow outcome: grant or deny access.
* if no matching rules were found for this document or folder, return the default outcome: grant or deny.


## Setting up a default workflow outcome

To setup a default outcome make sure this node exists:

    /hippo:configuration/hippo:modules/xinmods/hippo:moduleconfig/workflow

And create a property called `defaultOutcome` of type String with a value of 'grant' or 'deny'.

default outcome = grant/deny

## Setting up per action workflow rules

There are a number of workflow actions that you can specify a number of workflow action rules for. Ones marked with 
an asterisk can also be used for the folder workflow plugin variations. 

* delete `*`
* rename `*`
* displayName 
* copy 
* move
* archive.

A workflow action rule is expressed as a property. The name of the property is subject to certain conventions:

	<grant|deny>.<type|mixin|path>.<description>
		
		if mixin? mixinname,..
		if type? yourproject:homepage,..
		if path? regex string

Your property name will consist of two or three parts (you can leave out the description) separated by a period.

1. The first part determines the rule's workflow outcome if it matches the current document: grant or deny access
2. the second part indicates the type of evaluation that is applied to the document or folder:
    - `type`: the rule applies if any of the property values matches the primary type of the document
    - `mixin`: the rule applies if any of the mixins of the document are mentioned in the property value
    - `path`: the rule applies if any of the regular expressions in the property value match the document's path
3. The third is an optional description that is used for logging purposes.

For example if we want to make sure any folder with the mixin `xinmods:rigidfolder` cannot be deleted, our workflow
node will have the following structure:

    /hippo:configuration/hippo:modules/xinmods/hippo:moduleconfig
        /workflow
            - defaultOutcome = grant

            /delete
                - deny.mixin.folders = xinmods:rigidfolder

Note that the property value can either be singular or a multiple. 

## Enable plugins

To enable the workflow plugins to be used by the CMS go into the CMS Console and adjust the paths mentioned below.

### Document workflow

Paths: 

* /hippo:configuration/hippo:workflows/core/handle 
* /hippo:configuration/hippo:workflows/core/default

Replace classname:

	org.hippoecm.repository.standardworkflow.DefaultWorkflowImpl

.. with:

	nz.xinsolutions.workflow.XinDefaultWorkflowImpl

### Folder workflow

Paths:

* Editor's folder workflows: /hippo:configuration/hippo:workflows/embedded/folder-extended
* Author's folder workflows: /hippo:configuration/hippo:workflows/embedded/folder
* Gallery folder workflows: /hippo:configuration/hippo:workflows/embedded/gallery

Replace classname:

    org.hippoecm.repository.standardworkflow.FolderWorkflowImpl

.. with:

    nz.xinsolutions.workflow.XinFolderWorkflowImpl


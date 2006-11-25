package org.eclipse.wst.common.componentcore.internal;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.wst.common.componentcore.ModuleCoreNature;
import org.eclipse.wst.common.componentcore.internal.impl.WTPModulesResource;
import org.eclipse.wst.common.internal.emf.resource.TranslatorResource;
import org.eclipse.wst.common.internal.emfworkbench.WorkbenchResourceHelper;
import org.eclipse.wst.common.project.facet.core.internal.FacetedProjectNature;
import org.eclipse.wst.validation.internal.core.Message;
import org.eclipse.wst.validation.internal.core.ValidationException;
import org.eclipse.wst.validation.internal.provisional.core.IMessage;
import org.eclipse.wst.validation.internal.provisional.core.IReporter;
import org.eclipse.wst.validation.internal.provisional.core.IValidationContext;
import org.eclipse.wst.validation.internal.provisional.core.IValidatorJob;


public class ModuleCoreValidator implements IValidatorJob {
		protected IReporter _reporter;

		public ModuleCoreValidator() {
			super();
			// TODO Auto-generated constructor stub
		}
		
		public Resource getPrimaryResource(IProject project) {
			// Overriden to handle loading the .component resource in new and old forms
			// First will try to load from .settings/org.eclipse.wst.common.component
			// Second will try to load from the old location(s) .settings/.component or .component

			URI uri = (URI) URI.createURI(StructureEdit.MODULE_META_FILE_NAME);
			WTPModulesResource res = (WTPModulesResource)WorkbenchResourceHelper.getOrCreateResource(uri, getResourceSet(project));
			if (res == null || !res.isLoaded()) {
				uri = (URI) URI.createURI(".settings/.component");
				res = (WTPModulesResource)WorkbenchResourceHelper.getOrCreateResource(uri, getResourceSet(project));
				if (res == null || !res.isLoaded()) {
					uri = (URI) URI.createURI(".wtpmodules");
					res = (WTPModulesResource)WorkbenchResourceHelper.getOrCreateResource(uri, getResourceSet(project));
					if (res == null || !res.isLoaded()) {
						res = null;
					}
				}
			}
			return res;
		}

		private ResourceSet getResourceSet(IProject proj) {
			ModuleCoreNature nature = ModuleCoreNature.getModuleCoreNature(proj);
			return nature.getResourceSet();
		}

		public IStatus validateInJob(IValidationContext helper, IReporter reporter)
				throws ValidationException {
			_reporter = reporter;
			//Remove all markers related to this validator
			_reporter.removeAllMessages(this);
			//Using the helper class, load the module model
			IProject proj = (IProject) helper
					.loadModel(ModuleCoreValidatorHelper.MODULECORE, null);
			try {
				if (proj.isAccessible() && proj.hasNature(ModuleCoreNature.MODULE_NATURE_ID)) {
					if (needsMigrating(proj))
						_reporter.addMessage(this, new Message("modulecore",
								IMessage.HIGH_SEVERITY,
								"NEEDSMIGRATING", null, proj));
				}
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return Status.OK_STATUS;
		}
		public ISchedulingRule getSchedulingRule(IValidationContext helper) {
			return null;
		}

		public void cleanup(IReporter reporter) {
			_reporter = null;

		}
		private boolean needsMigrating(IProject project) throws CoreException {
			XMIResource res = (XMIResource)getPrimaryResource(project);
			boolean multiComps = false;
			if (project==null)
				return false;
			boolean needsMigrating = (!project.hasNature(FacetedProjectNature.NATURE_ID)) || res == null || ((res != null) && ((WTPModulesResource)res).getRootObject() == null); //|| (res!=null && !res.isLoaded() && ((WTPModulesResource)res).getRootObject() != null);
			if (!needsMigrating) {
				if (res instanceof TranslatorResource && ((TranslatorResource)res).getRootObject() instanceof ProjectComponents) {
					ProjectComponents components = (ProjectComponents) ((WTPModulesResource)res).getRootObject();
					if (components.getComponents() != null) {
						multiComps = components.getComponents().size() > 1;
						return multiComps;
					}
				}
			}
			return needsMigrating;
		}

		public void validate(IValidationContext helper, IReporter reporter)
				throws ValidationException {
			// Forwarding to job method
			validateInJob(helper, reporter);
		}
	}
/***************************************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 **************************************************************************************************/
package org.eclipse.wst.common.modulecore;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.wst.common.internal.emfworkbench.EMFWorkbenchContext;
import org.eclipse.wst.common.internal.emfworkbench.integration.EditModel;
import org.eclipse.wst.common.modulecore.internal.impl.WTPModulesResource;
import org.eclipse.wst.common.modulecore.internal.impl.WTPModulesResourceFactory;
/**
 * Manages the underlying Module Structural Metamodel.
* <a name="module-structural-model"/>
* <p>
* Each ModuleCoreNature from a given project can provide access to the
* {@see org.eclipse.wst.common.modulecore.ModuleStructuralModel}&nbsp; of the project.
* {@see org.eclipse.wst.common.modulecore.ModuleStructuralModel}&nbsp; is a subclass of
* {@see org.eclipse.wst.common.internal.emfworkbench.integration.EditModel}&nbsp;that manages
* resources associated with the Module Structural Metamodel. As an EditModel, the
* {@see org.eclipse.wst.common.modulecore.ModuleStructuralModel}&nbsp; references EMF resources,
* that contain EMF models -- in this case, the EMF model of <i>.wtpmodules </i> file.
* </p>
* <p>
* Clients are encouraged to use the Edit Facade pattern (via
* {@see org.eclipse.wst.common.modulecore.ArtifactEdit}&nbsp; or one if its relevant subclasses)
* to work directly with the Module Structural Metamodel.
* </p> 
* <p>
* The following diagram highlights the relationships of these subclasses of EditModel, and the
* relationship of the EditModel to the EMF resources. In the diagram, "MetamodelResource" and
* "MetamodelObject" are used as placeholders for the specific subclasses of
* {@see org.eclipse.emf.ecore.resource.Resource}and {@see org.eclipse.emf.ecore.EObject}
* respectively.
* </p>
* <table cellspacing="10" cellpadding="10">
* <tr>
* <td>
* <p>
* <img src="../../../../../overview/metamodel_components.jpg" />
* </p>
* </td>
* </tr>
* <tr>
* <td>
* <p>
* <i>Figure 1: A component diagram of the Module Edit Models. </i>
* </p>
* </td>
* </tr>
* </table> 
* <a name="accessor-key"/>
* <p>
* All EditModels have a lifecycle that must be enforced to keep the resources loaded that are in
* use, and to unload resources that are not in use. To access an EditModel, clients are required to
* supply an object token referred to as an accessor key. The accessor key allows the framework to
* better track which clients are using the EditModel, and to ensure that only a client which has
* accessed the EditModel with an accessor key may invoke save*()s on that EditModel.
* </p>
*/ 
public class ModuleStructuralModel extends EditModel implements IAdaptable {
	
	public static final String MODULE_CORE_ID = "moduleCoreId"; //$NON-NLS-1$ 
  

    public ModuleStructuralModel(String editModelID, EMFWorkbenchContext context, boolean readOnly) {
        super(editModelID, context, readOnly);
    }
    
    /* (non-Javadoc)
	 * @see org.eclipse.wst.common.internal.emfworkbench.integration.EditModel#getPrimaryRootObject()
	 */
	public EObject getPrimaryRootObject() {
		if(getPrimaryResource().getContents().size() == 0)
			prepareProjectModulesIfNecessary();
		return super.getPrimaryRootObject();
	}
       
	public WTPModulesResource  makeWTPModulesResource() {
		return (WTPModulesResource) createResource(WTPModulesResourceFactory.WTP_MODULES_URI_OBJ);
	}

	public Resource prepareProjectModulesIfNecessary() {
		XMIResource res = makeWTPModulesResource();		
		addProjectModulesIfNecessary(res);
		return res;
	}
	
	public Object getAdapter(Class anAdapter) {
		return Platform.getAdapterManager().getAdapter(this, anAdapter); 
	}
	
	protected void addProjectModulesIfNecessary(XMIResource aResource) { 
		if (aResource != null && aResource.getContents().isEmpty()) {
			ProjectModules projectModules = ModuleCorePackage.eINSTANCE.getModuleCoreFactory().createProjectModules();
			aResource.getContents().add(projectModules); 
			aResource.setID(projectModules, MODULE_CORE_ID);
		}
	}
}

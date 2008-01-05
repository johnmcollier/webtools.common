package org.eclipse.wst.validation;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;

/**
 * The result of running a validate operation.
 * @author karasiuk
 *
 */
public class ValidationResult {
	
	private List<ValidatorMessage> _messages;
	
	private static ValidatorMessage[] _noMessages = new ValidatorMessage[0];
	
	private boolean		_canceled;
	
	/** 
	 * The resources that the validated resource depends on. This can be left null.
	 * For example, an XML file may depend on a XSD in order to know if it is valid or not.
	 * It would pass back the XSD file. 
	 */
	private IResource[]	_dependsOn;
	
	/** 
	 * The objects that you validated as a side effect of validating this object. The framework will not
	 * call you with any of these objects (in this validation run). This can be left null.
	 */
	private IResource[] _validated;
	
	private int			_severityError;
	private int			_severityWarning;
	private int			_severityInfo;
	
	/**
	 * This is an optional method, that a validator can use to return error messages. When the validation framework
	 * is invoking the validator these will be converted into IMarkers. If the validator is being called directly then
	 * the caller is responsible for using the messages.
	 * <p>
	 * The scenario that motivated this method, is when a validator is used both for as-you-type validation and batch
	 * validation. In this scenario the validator when called in as-you-type mode doesn't want to directly create IMarkers,
	 * because the resource hasn't even been saved. It needs to return something other than an IMarker. But when called
	 * in batch mode, it does ultimately want IMarkers. By returning ValidatorMessages, it only needs to return one type
	 * of message, and those messages can be either be directly used by the caller, or automatically converted into IMarkers by
	 * the validation framework.   
	 * 
	 * @param message a message
	 */
	public void add(ValidatorMessage message){
		getMessageList().add(message);
	}
	
	/**
	 * Merge the message counts and messages from an individual validator into this result.
	 * @param result it can be null, in which case it is ignored.
	 */
	public void mergeResults(ValidationResult result){
		if (result == null)return;
		
		ValidatorMessage[] msgs = result.getMessages();
		for (int i=0; i<msgs.length; i++){
			ValidatorMessage m = msgs[i];
			add(m);
			int severity = m.getAttribute(IMarker.SEVERITY, 0);
			switch (severity){
			case IMarker.SEVERITY_ERROR:
				incrementError(1);
				break;
			case IMarker.SEVERITY_WARNING:
				incrementWarning(1);
				break;
			case IMarker.SEVERITY_INFO:
				incrementInfo(1);
				break;
					
			}
		}
		
		incrementError(result.getSeverityError());
		incrementWarning(result.getSeverityWarning());
		incrementInfo(result.getSeverityInfo());
		
	}


	public IResource[] getDependsOn() {
		return _dependsOn;
	}
	
	/**
	 * Answer any validation messages that were added by the validator. 
	 * @return an array is returned even if there are no messages.
	 */
	public ValidatorMessage[] getMessages(){
		if (_messages == null)return _noMessages;
		ValidatorMessage[] msgs = new ValidatorMessage[_messages.size()];
		_messages.toArray(msgs);
		return msgs;
	}

	/**
	 * Update the resources that the validated resource depends on. This can be left null.
	 * For example, an XML file may depend on a XSD in order to know if it is valid or not.
	 * It would pass back the XSD file. 
	 * 
	 * @param dependsOn if this is null then the dependency information is not updated. To remove the
	 * dependency information, an empty array needs to be supplied. A non null parameter, <b>replaces</b> all the
	 * dependency information for this resource, for this validator. 
	 */
	public void setDependsOn(IResource[] dependsOn) {
		_dependsOn = dependsOn;
	}
	
	private List<ValidatorMessage> getMessageList(){
		if (_messages == null)_messages = new LinkedList<ValidatorMessage>();
		return _messages;
	}

	/**
	 * Answer all the resources that were validated as a side-effect of validating the main resource.
	 */
	public IResource[] getValidated() {
		return _validated;
	}
	
	/**
	 * Indicate that additional resources have been validated as part of this validate operation. 
	 * Sometimes in the course of performing a validation on one one resource it is
	 * necessary to validate other resources as well. This method is used to let the framework know about these 
	 * additional validated resources, to possibly save them being validated redundantly.
	 * 
	 * @param validated
	 */
	public void setValidated(IResource[] validated) {
		_validated = validated;
	}

	/**
	 * Answer the number of error messages that were generated as part of this validation operation.
	 */
	public int getSeverityError() {
		return _severityError;
	}

	/**
	 * Set the number of error messages that were generated as part of this validation operation.
	 * <p>
	 * Messages added through the add(ValidationMessage) method should not be included here, as this
	 * information will be determined from the ValidationMessage.
	 */
	public void setSeverityError(int severityError) {
		_severityError = severityError;
	}
	

	/**
	 * Increment the number of error messages that were generated as part of this validation operation.
	 * <p>
	 * Messages added through the add(ValidationMessage) method should not be included here, as this
	 * information will be determined from the ValidationMessage.
	 * 
	 * @return the current number of errors.
	 */
	public int incrementError(int errors){
		_severityError += errors;
		return _severityError;
	}

	/**
	 * Answer the number of warning messages that were generated as part of this validation operation.
	 */
	public int getSeverityWarning() {
		return _severityWarning;
	}

	/**
	 * Set the number of warning messages that were generated as part of this validation operation.
	 * <p>
	 * Messages added through the add(ValidationMessage) method should not be included here, as this
	 * information will be determined from the ValidationMessage.
	 */
	public void setSeverityWarning(int severityWarning) {
		_severityWarning = severityWarning;
	}

	/**
	 * Increment the number of warning messages that were generated as part of this validation operation.
	 * <p>
	 * Messages added through the add(ValidationMessage) method should not be included here, as this
	 * information will be determined from the ValidationMessage.
	 * 
	 * @return the current number of warnings.
	 */
	public int incrementWarning(int warnings){
		_severityWarning += warnings;
		return _severityWarning;
	}

	/**
	 * Answer the number of informational messages that were generated as part of this validation operation.
	 */
	public int getSeverityInfo() {
		return _severityInfo;
	}

	/**
	 * Set the number of informational messages that were generated as part of this validation operation.
	 * <p>
	 * Messages added through the add(ValidationMessage) method should not be included here, as this
	 * information will be determined from the ValidationMessage.
	 */
	public void setSeverityInfo(int severityInfo) {
		_severityInfo = severityInfo;
	}

	/**
	 * Increment the number of informational messages that were generated as part of this validation operation.
	 * <p>
	 * Messages added through the add(ValidationMessage) method should not be included here, as this
	 * information will be determined from the ValidationMessage.
	 * 
	 * @return the current number of informational message.
	 */
	public int incrementInfo(int info){
		_severityInfo += info;
		return _severityInfo;
	}

	/**
	 * Was the operation canceled before it completed? For example if the validation is being run through the
	 * UI, the end user can cancel the operation through the progress monitor.
	 * 
	 * @return true if the operation was canceled
	 */
	public boolean isCanceled() {
		return _canceled;
	}

	/**
	 * Indicate if the operation was canceled.
	 * @param canceled
	 */
	public void setCanceled(boolean canceled) {
		_canceled = canceled;
	}

}
// VariationGenerator.java

// PACKAGE

package com.prophetvs.editor;

// IMPORTS

import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

// CLASS

public class VariationGenerator
{
	// PUBLIC STATIC METHODS
	
	public static VariationGenerator
	getInstance ()
		throws VSException
	{
		// save the cost of a sync block here
		if (sInstance == null)
		{
			synchronized (sSynchronizer)
			{
				if (sInstance == null)
				{	
					sInstance = new VariationGenerator ();
				}
			}
		}

		return sInstance;
	}
	
	// PRIVATE CONSTRUCTOR
	
	private
	VariationGenerator ()
		throws VSException
	{
		try
		{
			setupTransformMap ();
		}
		catch (Throwable inThrowable)
		{
			throw new VSException (inThrowable.toString ());
		}
	}
	
	// PUBLIC METHODS
	
	public Bank
	generate (Patch inPatch, List<TransformOperation> inTransformOperations)
		throws Exception
	{
		// make a new bank using our template as... the template
		Bank	bank = new Bank (inPatch);

		// apply the transform map to each patch in turn
		for (int i = 0; i < 100; i++)
		{
			// getting a reference here, not a copy
			Patch	patch = bank.getPatch (i);

			applyTransforms (patch, i, inTransformOperations);
		}

		return bank;
	}
	
	public List<String>
	getMetaParameter (String inMetaParameterName)
	{
		return metaParameterMap.get (inMetaParameterName);
	}
	
	public String[]
	getMetaParameterNames ()
	{
		return (String[]) this.metaParameterMap.keySet ().toArray (new String [0]);
	}
	
	public Transform
	getTransform (String inName)
		throws Exception
	{
		return this.transformMap.get (inName);
	}

	public String[]
	getTransformNames ()
	{
		return (String[]) this.transformMap.keySet ().toArray (new String [0]);
	}
	
	// PRIVATE METHODS
	
	private void
	applyTransforms (Patch ioPatch, int inPatchNumber,
		List<TransformOperation> inTransformOperations)
		throws VSException
	{
		for (TransformOperation transformOperation : inTransformOperations)
		{
			String	transformName = transformOperation.getTransform ();
			
			// look up the class for this name
			Transform	transform = this.transformMap.get (transformName);
			
			if (transform == null)
			{
				throw new VSException ("transform name not found: " + transformName);
			}
			
			String	patchParameterName = transformOperation.getPatchParameter ();
			
			// check for meta parameter!
			List<String>	patchParameterNames = getMetaParameter (patchParameterName);
			
			if (patchParameterNames == null)
			{
				patchParameterNames = new ArrayList<String> ();
				patchParameterNames.add (patchParameterName);
			}
			
			for (int i = 0; i < patchParameterNames.size (); i++)
			{
				patchParameterName = patchParameterNames.get (i);
				
				Patch.ParameterSpec	parameterSpec = ioPatch.getParameterSpec (patchParameterName);
	
				if (parameterSpec == null)
				{
					throw new VSException
						("transform parameter name not found: " + patchParameterName);
				}
				
				// apply the transform
				int	newParameterValue = transform.transformParameter (patchParameterName,
					transformOperation.getTransformParameters (), inPatchNumber, parameterSpec.size);
					
				ioPatch.setParameterValue (patchParameterName, newParameterValue);
			}
		}
	}

	private void
	setupTransformMap ()
		throws ClassNotFoundException, IllegalAccessException, InstantiationException, IOException
	{
		// load the transform map config file
		URL	propertiesURL = ControlWindow.getResource ("transforms.properties");
		
		InputStream	uis = null;
		Properties	properties = new Properties ();
		
		try
		{
			uis = propertiesURL.openStream ();

			properties.load (uis);
		}
		finally
		{
			if (uis != null)
			{
				try
				{
					uis.close ();
				}
				catch (Throwable inThrowable)
				{
				}
			}
		}

		this.transformMap = new HashMap<String, Transform> ();

		Enumeration	propertyNames = properties.propertyNames ();

		while (propertyNames.hasMoreElements ())
		{
			String	transformName = (String) propertyNames.nextElement ();
			String	transformClassName = properties.getProperty (transformName);

			Class	transformClass = Class.forName (transformClassName);

			Transform	transform = (Transform) transformClass.newInstance ();
			this.transformMap.put (transformName, transform);
		}

		// load the metaparameter map config file
		propertiesURL = ControlWindow.getResource ("metaparameters.properties");
		
		properties = new Properties ();
		
		try
		{
			uis = propertiesURL.openStream ();

			properties.load (uis);
		}
		finally
		{
			if (uis != null)
			{
				try
				{
					uis.close ();
				}
				catch (Throwable inThrowable)
				{
				}
			}
		}

		this.metaParameterMap = new HashMap<String, List<String>> ();

		Enumeration	metaParameterNames = properties.propertyNames ();

		while (metaParameterNames.hasMoreElements ())
		{
			String	metaParameterName = (String) metaParameterNames.nextElement ();
			String	parameterNames = properties.getProperty (metaParameterName);

			String[]	parameterNamesArray = parameterNames.split (",", -1);
			
			List<String>	parameterNamesList = new ArrayList<String> ();
			
			for (int i = 0; i < parameterNamesArray.length; i++)
			{
				parameterNamesList.add (parameterNamesArray [i].trim ());
			}

			this.metaParameterMap.put (metaParameterName, parameterNamesList);
		}
	}

	// PRIVATE STATIC DATA
	
	private static VariationGenerator
	sInstance = null;
	
	private static Object
	sSynchronizer = new Object ();
	
	// PRIVATE DATA

	private Map<String, List<String>>
	metaParameterMap = null;
	
	private Map<String, Transform>
	transformMap = null;
	
}


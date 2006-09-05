#######################################################
################## Utility functions ##################
#######################################################

#### OOP Utils ####
createMethod = function(methodName, objectName, fn) {
	if(!isGeneric(methodName)) {
		# Create generic for this method
		if(is.null(getGeneric(methodName))) { #Check if exists as Primitive
			if(exists(methodName, mode="function")) { #Check if exists as function
				fun = eval(parse(text=methodName))
				fun = makeStandardGeneric(methodName, fun)
			}
			else  {#If nothing is defined yet, create a standardGeneric function
				fun = function(x, ...) standardGeneric(methodName)
			}
			setGeneric(methodName, fun)
		} else setGeneric(methodName) #Exists as Primitive, set as Generic
	}
	
	setMethod(methodName, objectName, fn)
}

createReplaceMethod = function(methodName, objectName, fn) {
	createMethod(paste(methodName, "<-", sep = ""), objectName, fn)
}


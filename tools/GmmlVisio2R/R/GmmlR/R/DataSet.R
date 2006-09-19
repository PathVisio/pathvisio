#######################################################
################## DataSet class ##################
#######################################################

##########################
#### Class Definition ####
##########################
setClass("DataSet", 
	representation(
		name = "character",
		data = "list"
	),
	prototype = list(
		data = list()
	)
)

## Validity:
## all elements in @data must be instance of class GeneProductData
setValidity("DataSet", function(x) {
	for(p in getData(x))
		if(!isClass("GeneProductData", p)) return("list @data contains non-GeneProductData objects")
	TRUE
})

######################
#### Constructors ####
######################
setGeneric("DataSet", 
	function(name, data = matrix()) {
		new("DataSet", name = name, data = data)
	}
)

#################
#### Methods ####
#################
## Getters ##
createMethod("getName", "DataSet", function(x) x@name)
createMethod("getData", "DataSet", function(x) x@data)

## Setters ##)
createReplaceMethod("setName", "Pathway", function(x, value) {
	x@name = value
	x
})
createReplaceMethod("setData", "Pathway", function(x, value) {
	x@data = value
	if(!validObject(x)) stop("given data matrix is not valid (check dims)")
	x
})

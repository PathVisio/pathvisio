#######################################################
################## DataSet class ##################
#######################################################

##########################
#### Class Definition ####
##########################
setClass("DataSet", contains = "matrix",
	representation(
		name = "character"
	),
	prototype(
		name = character()
	)
)

## Validity:
## all elements in @data must be instance of class GeneProductData
setValidity("DataSet", function(object) {
	TRUE
})

######################
#### Constructors ####
######################
setGeneric("DataSet", 
	function(name, reporters = NULL, data) {
	if(!is.null(reporters)) rownames(data) = reporters
	new("DataSet", data, name = name)
})

#################
#### Methods ####
#################
## Getters ##
createMethod("name", "DataSet", function(x, ...) x@name)
createMethod("samples", "DataSet", function(x, ...) colnames(x))
createMethod("reporters", "DataSet", function(x, ...) rownames(x))

## Setters ##)
createReplaceMethod("name", "DataSet", function(x, value, ...) {
	x@name = value
	validObject(x, test=TRUE);
	x
})
createReplaceMethod("samples", "DataSet", function(x, value, ...) {
	colnames(x) = value
	validObject(x, test=TRUE);
	x
})
createReplaceMethod("reporters", "DataSet", function(x, value, ...) {
	rownames(x) = value
	validObject(x, test=TRUE);
	x
})

## Primitive or Generic
setMethod("print", "DataSet", function(x, ...) {
	cat("DataSet:", "\t@name", paste("\t\t", name(x)),
	"\tdata", paste("\t\tsamples: ", names(x)),
	"\treporters", paste("\t\tNumber of reporters: ", length(rownames(x))), sep="\n")
})

## Other
createMethod("dataByReporter", c("DataSet", "character"), function(obj, reporter) {
	obj[which(names(obj) == reporter),]
})
	
	

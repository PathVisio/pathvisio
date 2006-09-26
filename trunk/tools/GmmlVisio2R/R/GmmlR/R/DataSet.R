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
	for(p in datalist(x))
		if(!isClass("GeneProductData", p)) return("list @data contains non-GeneProductData objects")
	TRUE
})

######################
#### Constructors ####
######################
setGeneric("DataSet", 
	function(name, data = matrix()) {
		ds = new("DataSet", name = name, data = data)
		names(datalist(ds)) = sapply(datalist(ds), function(x) name(geneProduct(x)))
	}
)

#################
#### Methods ####
#################
## Getters ##
createMethod("name", "DataSet", function(x) x@name)
createMethod("datalist", "DataSet", function(x) x@data)

## Setters ##)
createReplaceMethod("name", "DataSet", function(x, value) {
	x@name = value
	x
})
createReplaceMethod("datalist", "DataSet", function(x, value) {
	x@data = value
	if(!validObject(x)) stop("given data matrix is not valid (check dims)")
	x
})

## Primitive or Generic
createMethod("print", "DataSet", function(x, ...) {
	cat("DataSet:", "\t@name", paste("\t\t", name(x)), 
	"\t@data", paste("\t\t", length(datalist(x)), " GeneProductData instances"), sep="\n")
})

createMethod("as.list", "DataSet", function(x, ...) { datalist(x) }

## Other ##
createMethod("addSet", "DataSet", function(x, set, ...) {
	dta = datalist(x)
	ndta = list()
	for(i in 1:length(dta)) ndta[[i]] = addSet(dta[[i]], set[i])
	datalist(x) = ndta
	x
})

createMethod("calcSet", "DataSet", function(dataSet, name, fun) {
	set = lapply(datalist(dataSet), fun)
	names(set) = rep(name, length(set))
	addSet(dataSet, set)
})
		
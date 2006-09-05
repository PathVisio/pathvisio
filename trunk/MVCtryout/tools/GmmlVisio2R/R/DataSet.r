source("util.r")

#######################################################
################## DataSet class ##################
#######################################################

##########################
#### Class Definition ####
##########################
setClass("DataSet", 
	representation(
		name = "character",
		geneProducts = "list",
		data = "matrix",
		subsets = "matrix"
	),
	prototype = list(
		geneProducts = list(),
		data = matrix(),
		subsets = matrix()
	)
)

## Validity:
## 1: length(@geneProducts) == nrow(data) == nrow(subsets)
## 2: all elements in @geneProducts must be instance of class GeneProduct
setValidity("DataSet", function(x) {
	if(	length(getGeneProducts(x)) != nrow(getData(x)) ||
		length(getGeneProducts(x)) != nrow(getSubsets(x)))
		return("number of gene products does not match number of rows in @data or @subsets")
	for(p in getGeneProducts(x))
		if(!isClass("GeneProduct", p)) return("list @geneProducts contains non-GeneProduct objects")
	TRUE
})

######################
#### Constructors ####
######################
setGeneric("DataSet", 
	function(name, geneProducts = list(), data = matrix(), subsets = matrix()) {
		new("DataSet", name = name, geneProducts = geneProducts, data = data, subsets = subsets)
	}
)

#################
#### Methods ####
#################
## Getters ##
createMethod("getName", "DataSet", function(x) x@name)
createMethod("getGeneProducts", "DataSet", function(x) x@geneProducts)
createMethod("getData", "DataSet", function(x) x@data)
createMethod("getSubsets", "DataSet", function(x) x@subsets)

## Setters ##)
createReplaceMethod("setName", "Pathway", function(x, value) {
	x@name = value
	x
})
createReplaceMethod("setGeneProducts", "Pathway", function(x, value) {
	x@geneProducts = value
	if(!validObject(x)) stop("given GeneProducts list is not valid (check length and class of elements)")
	x
})
createReplaceMethod("setData", "Pathway", function(x, value) {
	x@data = value
	if(!validObject(x)) stop("given data matrix is not valid (check dims)")
	x
})
createReplaceMethod("setsubSet", "Pathway", function(x, value) {
	x@subsets = value
	if(!validObject(x)) stop("given subsets matrix list is not valid (check dims)")
	x
})

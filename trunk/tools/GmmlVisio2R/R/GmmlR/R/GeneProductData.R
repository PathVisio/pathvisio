###########################################################
################## GeneProductData class ##################
###########################################################

##########################
#### Class Definition ####
##########################
setClass("GeneProductData",
	representation(
		geneProduct = "GeneProduct",
		data = "list",
		sets = "list"
	),
	prototype = list(
		data = list(),
		sets = list()
	)
)

## Validity:
## 1: length(ids) == length(systems)
setValidity("GeneProductData", function(object) {
	if(!isClass("GeneProduct", geneProduct))
		return("No object of class 'GeneProduct' for slot geneProduct")
	if((length(object@data) != 0 && length(object@sets) != 0) && (length(object@data) != length(object@sets)))
		return("length of 'data' and 'sets' does not match")
})

######################
#### Constructors ####
######################
GeneProductData <- function(geneProduct, data = list(), sets = list()) {
		new("GeneProductData", geneProduct = geneProduct, data = data, sets = sets)
}

#################
#### Methods ####
#################
## Getters ##
createMethod("geneProduct", "GeneProductData", function(x) x@geneProduct)
createMethod("datalist", "GeneProductData", function(x) x@data)
createMethod("setlist", "GeneProductData", function(x) x@sets)

## Setters ##)
createReplaceMethod("geneProduct", "GeneProductData", function(x, value) {
	x@geneProduct = value
	x
})
createReplaceMethod("datalist", "GeneProductData", function(x, value) {
	x@data = value
	x
})
createReplaceMethod("setlist", "GeneProductData", function(x, value) {
	x@sets = value
	x
})

## Generic and Primitive implementations ##
createMethod("print", "GeneProductData", function(x, ...) {
	cat("GeneProductData", "\t@GeneProduct", paste("\t\t", name(geneProduct(x))),
	"\t@data", sep="\n")
	dta = datalist(x)
	sts = setlist(x)
	for(i in 1:length(dta)) cat("\t\t", names(dta)[i], "\t", dta[[i]], "\n")
	if(length(sts) > 0) {
		cat("\t@sets\n")
		for(i in 1:length(sts)) cat("\t\t", names(sts)[i], "\t", sts[[i]], "\n")
	}
})

## Other ##
createMethod("addSet", "GeneProductData", function(x, set, ...) {
	setlist(x) = append(setlist(x), set)
	x
})
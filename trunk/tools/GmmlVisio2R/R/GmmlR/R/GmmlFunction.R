#######################################################
################## GmmlFunction class #################
#######################################################

##########################
#### Class Definition ####
##########################
setClass("GmmlFunction", contains = "function",
	representation(
		name = "character",
		description = "character",
		arg_descr = "character",
		arg_class = "character"),
	prototype(
		name = character(),
		description = character(),
		arg_descr = character(),
		arg_class = character())
)

## Validity:
setValidity("GeneProduct", function(object) {
	TRUE
})

######################
#### Constructors ####
######################
GmmlFunction = function(fn, name = character(), description = character(), 
		arg_descr = character(length(formals(fn))), arg_class = character(length(formals(fn)))) {
	new("GmmlFunction", fn, name=name, description=description, 
		arg_descr=arg_descr, arg_class=arg_class)
}

#################
#### Methods ####
#################
## Getters ##
createMethod("name", "GmmlFunction", function(x) x@name)
createMethod("description", "GmmlFunction", function(x) x@description)
createMethod("arg_descr", "GmmlFunction", function(x) x@arg_descr)
createMethod("arg_class", "GmmlFunction", function(x) x@arg_class)

## Other ##
createMethod("getArgs", "GmmlFunction", function(x) {
	names(formals(x))
})

createMethod("getDefaults", "GmmlFunction", function(x) {
	as.character(formals(x))
})

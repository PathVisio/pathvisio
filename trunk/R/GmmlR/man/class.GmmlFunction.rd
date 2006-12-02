\name{GmmlFunction-class}
\docType{class}
\alias{GmmlFunction-class}
\alias{class:GmmlFunction}
\alias{GmmlFunction}
\alias{name,GmmlFunction-method}
\alias{description,GmmlFunction-method}
\alias{arg_descr,GmmlFunction-method}
\alias{arg_class,GmmlFunction-method}
\alias{getArgs,GmmlFunction-method}
\alias{getDefaults,GmmlFunction-method}

\title{Class "GmmlFunction"}
\description{A class that contains a function that can be used for pathway
statistics within GmmlR. The class contains meta-information used by Gmml-Visio. The function has to return an instance of the \code{\link{ResultSet}} class in order to be runned from within Gmml-Visio.} 

\section{Extends}{
   Directly extends class \code{\link{function}}.
}

\section{Creating objects}{
Objects can be created by calls of the form 
\code{GmmlFunction(fn, name, description, arg_descr, arg_class)} where\cr
\code{fn} is the \code{\link{function}} that performs some form of pathway statistics and returns a \code{\link{ResultSet}} and the further arguments are slot values
}

\section{Slots}{
  \describe{
    \item{name}{ the name of the function }
    \item{description}{ a short description of the function }
    \item{arg_descr}{ a short description for every argument of the function }
    \item{arg_class}{ the class of every argument }
  }
}

\section{Methods}{
  \describe{
    \item{name}{
    	\code{signature(x = "GmmlFunction")}:
    	accessor for the slot 'name'
    }
    \item{description}{
    	\code{signature(x = "GmmlFunction")}:
    	accessor for the slot 'description'
    }
    \item{arg_descr}{
    	\code{signature(x = "GmmlFunction")}:
    	accessor for the slot 'arg_descr'
    }
    \item{arg_class}{
    	\code{signature(x = "GmmlFunction")}:
    	accessor for the slot 'arg_descr'
    }
    \item{getArgs}{
    	\code{signature(x = "GmmlFunction")}:
    	get the arguments of this function (equivalent to \code{names(formals(x))})
    }
    \item{getDefaults}{
    	\code{signature(x = "GmmlFunction")}:
    	get the default values of the arguments of this function (equivalent to \code{as.character(formals(x))})
     }
    }
}
\author{Thomas Kelder (BiGCaT)}

\keyword{methods}
\keyword{classes} 

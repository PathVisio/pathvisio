\name{VisioFunction-class}
\docType{class}
\alias{VisioFunction-class}
\alias{class:VisioFunction}
\alias{VisioFunction}
\alias{name,VisioFunction-method}
\alias{description,VisioFunction-method}
\alias{arg_descr,VisioFunction-method}
\alias{arg_class,VisioFunction-method}
\alias{getArgs,VisioFunction-method}
\alias{getDefaults,VisioFunction-method}

\title{Class "VisioFunction"}
\description{A class that contains a function that can be used for pathway
statistics within PathVisio. The class contains meta-information used by Path-Visio. The function has to return an instance of the \code{\link{ResultSet}} class in order to be runned from within Path-Visio.} 

\section{Extends}{
   Directly extends class \code{\link{function}}.
}

\section{Creating objects}{
Objects can be created by calls of the form 
\code{VisioFunction(fn, name, description, arg_descr, arg_class)} where\cr
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
    	\code{signature(x = "VisioFunction")}:
    	accessor for the slot 'name'
    }
    \item{description}{
    	\code{signature(x = "VisioFunction")}:
    	accessor for the slot 'description'
    }
    \item{arg_descr}{
    	\code{signature(x = "VisioFunction")}:
    	accessor for the slot 'arg_descr'
    }
    \item{arg_class}{
    	\code{signature(x = "VisioFunction")}:
    	accessor for the slot 'arg_descr'
    }
    \item{getArgs}{
    	\code{signature(x = "VisioFunction")}:
    	get the arguments of this function (equivalent to \code{names(formals(x))})
    }
    \item{getDefaults}{
    	\code{signature(x = "VisioFunction")}:
    	get the default values of the arguments of this function (equivalent to \code{as.character(formals(x))})
     }
    }
}
\author{Thomas Kelder (BiGCaT)}

\keyword{methods}
\keyword{classes} 

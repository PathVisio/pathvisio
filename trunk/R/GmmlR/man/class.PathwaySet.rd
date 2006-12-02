\name{PathwaySet-class}
\docType{class}
\alias{PathwaySet-class}
\alias{class:PathwaySet}
\alias{PathwaySet}
\alias{name,PathwaySet-method}
\alias{name<-,PathwaySet-method}
\alias{print,PathwaySet-method}

\title{Class "PathwaySet"}
\description{ } 

\section{Extends}{
   Directly extends class \code{\link{list}}.
}

\section{Creating objects}{
Objects can be created by calls of the form 
\code{PathwaySet(name, pathway)} where \code{pathways} is a \code{\link{list}} of \code{\link{Pathway}} objects and the other arguments are slot values
}

\section{Slots}{
  \describe{
    \item{name}{ the name of the pathway }
  }
}

\section{Methods}{
  \describe{
    \item{name and name<-}{
    	\code{signature(x = "PathwaySet")}:
    	accessor for the slot 'name'
    }
    \item{print}{
    	\code{signature(x = "PathwaySet")}:
    	prints this object in readable form
    }
}
\author{Thomas Kelder (BiGCaT)}

\keyword{methods}
\keyword{classes} 
